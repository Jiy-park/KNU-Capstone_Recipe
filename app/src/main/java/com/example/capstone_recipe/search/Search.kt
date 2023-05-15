package com.example.capstone_recipe.search

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.capstone_recipe.Preference
import com.example.capstone_recipe.R
import com.example.capstone_recipe.data_class.Filter
import com.example.capstone_recipe.data_class.Ingredient
import com.example.capstone_recipe.data_class.RecipeBasicInfo
import com.example.capstone_recipe.data_class.UserInfo
import com.example.capstone_recipe.databinding.ActivitySearchBinding
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await

class Search : AppCompatActivity() {
    private val binding by lazy { ActivitySearchBinding.inflate(layoutInflater) }
    private lateinit var context: Context
    private val db = Firebase.database("https://knu-capstone-f9f55-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private val storage = Firebase.storage
    private lateinit var mutex: Mutex
    private lateinit var searchUserAdapter: SearchUserAdapter
    private lateinit var searchRecipeAdapter: SearchRecipeAdapter
    private lateinit var searchFilterAdapter: SearchFilterAdapter
    private var searchFilter = Filter()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
        if(result.resultCode == Activity.RESULT_OK){
            searchFilter = result.data?.getSerializableExtra("filter", Filter::class.java)!!
//            searchFilterAdapter.filterOptionList = makeFilterOptionList(searchFilter)
            Log.d("LOG_CHECK", "Search :: onCreate() -> after searchFilter : $searchFilter")
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        context = binding.root.context

        searchUserAdapter = SearchUserAdapter() // 유저 검색 리사이클러뷰 세팅
        binding.recyclerviewUserResult.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.recyclerviewUserResult.adapter = searchUserAdapter

        searchRecipeAdapter = SearchRecipeAdapter() // 레시피 검색 리사이클러뷰 세팅
        binding.recyclerviewRecipeResult.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.recyclerviewRecipeResult.adapter = searchRecipeAdapter

        searchFilterAdapter = SearchFilterAdapter(searchFilter)
        binding.recyclerviewFilters.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerviewFilters.adapter = searchFilterAdapter

        binding.btnSetFilter.setOnClickListener {
            val intent = Intent(context, SetFilter::class.java)
            intent.putExtra("filter", searchFilter)
            resultLauncher.launch(intent)
        }

        binding.editSearch.setOnEditorActionListener { _, actionId, _ ->
            if(actionId == EditorInfo.IME_ACTION_SEARCH){
                val searchTarget = binding.editSearch.text.toString()
                if(searchTarget.isNotEmpty()){
                    lifecycleScope.launch(Dispatchers.IO) {
                        async{// 유저 검색
                            val userIdList = searchUserByName(searchTarget)
                            val userInfoList = getUsersInfo(userIdList)
                            Log.d("LOG_CHECK", "Search :: onCreate() -> \nuserIdList : $userIdList\nuserInfoList : $userInfoList")
                            withContext(Dispatchers.Main){
                                searchUserAdapter.updateUserList(userInfoList)
                            }
                        }
                        async {// 레시피 검색
                            val recipeIdList = searchRecipeIdByTitleWithFilter(searchTarget, searchFilter)
    //                        TODO("아래 리스트 두개 구하는 함수 제작할 것")
                            val recipeBasicInfoList = getRecipesBasicInfo(recipeIdList)
                            val creatorList = getCreatorList(recipeBasicInfoList)
                            val mainImageUriList = getRecipeMainImage(recipeBasicInfoList)
                            Log.d("LOG_CHECK", "Search :: onCreate() -> \n" +
                                    "recipeIdList : $recipeIdList\n" +
                                    "recipeBasicInfoList : $recipeBasicInfoList\n" +
                                    "creatorList : $creatorList\n" +
                                    "mainImageUriList : $mainImageUriList")
                            withContext(Dispatchers.Main){
                                searchRecipeAdapter.updateAdapterList(recipeBasicInfoList, creatorList, mainImageUriList)
                            }
                        }
                    }
                }
                else { Toast.makeText(context, "검색어를 입력해주세요.", Toast.LENGTH_SHORT).show() }
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

    /** * 입력받은 name을 갖는 유저의 아이디를 리스트 형태로 반환*/
    private suspend fun searchUserByName(name: String): List<String>{
        if(name.isEmpty()){ return emptyList() }
        val foundList = mutableListOf<String>()
        mutex = Mutex()
        withContext(Dispatchers.IO){
            db.getReference("users").get().await().children.map { userId ->
                async {
                    val userName = userId.child("name").value.toString()
                    if(userName.contains(name)) {
                        Log.d("LOG_CHECK", "Search :: searchUserByName() -> 서치 nmae : $name -> 발견 name : $userName")
                        mutex.withLock {
                            Log.d("LOG_CHECK", "Search :: searchUserByName() -> name에 해당하는 유저 ${userId.child("id").value.toString()} 추가 \n$foundList")
                            foundList.add(userId.child("id").value.toString())
                        }
                    }
                }
            }.awaitAll()
            Log.d("LOG_CHECK", "Search :: searchUserByName() -> 완료")
        }
        Log.d("LOG_CHECK", "Search :: searchUserByName() -> 완료 반환 리스트 : foundList : $foundList")
        return foundList
    }

    /** *유저 아이디 리스트를 받아 해당 아이디에 맞는 유저들 정보 (: 이름, 아이디, 프로필 이미지, 친구 확인) 를 찾아 리스트 형태로 반납 */
    private suspend fun getUsersInfo(userIdsList: List<String>):List<UserInfo>{
        val userInfoList = mutableListOf<UserInfo>()
        val userRef = db.getReference("users")
        val userId = Preference(context).getUserId() // 디바이스 주인 == 검색한 주체
        mutex = Mutex()

        withContext(Dispatchers.IO){
            userIdsList.map { id->
                async {
                    val name = userRef.child(id).child("name").get().await().value.toString()
                    val profilePath = userRef.child(id).child("profileImagePath").get().await().value.toString()
                    val profileUri = getFriendImageByPath(id, profilePath)
                    mutex.withLock{
                        userInfoList.add(UserInfo(id, name, profileUri))
                        Log.d("LOG_CHECK", "Search :: getUsersInfo() -> 추가 완료  userInfoList : $userInfoList")
                    }
                }
            }.awaitAll()
        }
        Log.d("LOG_CHECK", "Search :: getUsersInfo() -> 완료 userInfoList : $userInfoList")
        return userInfoList
    }

    /** *유저의 아이디와, 프로필 이미지 경로를 받아옴-> 해당 이미지를 반납 */
    private suspend fun getFriendImageByPath(userId: String, imagePath: String): Uri {
        val defaultImageUri by lazy { Uri.parse("android.resource://${context.packageName}/${R.drawable.default_user_profile_image}")!! }
        val userImageRef = storage.getReference("user_image")
        return if(imagePath.isNotEmpty()){ userImageRef.child(userId).child("profile").child(imagePath).downloadUrl.await() }
                 else { defaultImageUri }
    }


    /** * 테스트용 필터*/
    val textFilter = Filter(
        includeIngredient = mutableListOf("아침햇살"),
        excludeIngredient = mutableListOf("닥터페퍼"),
    )

    /** * 입력받은 타이틀 필터 옵션에 맞는 레시피의 아이디를 리스트 형태로 반납*/
//    TODO: 타이틀 검색이 우선인가??? 재료 검색이 우선인가?? 툴이 동시에 비교 해봐야할듯??
    private suspend fun searchRecipeIdByTitleWithFilter(searchTitle: String, filter: Filter): List<String>{
        mutex = Mutex()
        val recipeIdList = mutableListOf<String>()
        withContext(Dispatchers.IO){
            db.getReference("recipes").get().await().children.map { recipe ->
                async {
                    val recipeBasicInfo = recipe.child("basicInfo").getValue(RecipeBasicInfo::class.java)!!
                    val recipeTitle = recipeBasicInfo.title
                    if((recipeTitle.contains(searchTitle) && searchTitle.isNotEmpty())) { // 해당 레시피의 타이틀에 검색어가 포함돼 있으면 다음 작업 시작
                        Log.d("LOG_CHECK", "Search :: searchRecipeIdByTitleWithFilter() -> title : $recipeTitle")
                        val ingredient = recipe.child("ingredient").getValue<List<Ingredient>>()!!
                        val ingredientPass = ingredientFilterCheck(ingredient, filter)
//                        val basicInfoPass = basicInfoFilterCheck(recipeBasicInfo, filter)
                        Log.d("LOG_CHECK", "Search :: searchRecipeByTitleWithIngredient() -> ingredientPass : $ingredientPass")
    //                    val basicInfoPass = basicInfoFilterCheck(recipeBasicInfo, filter)
                        if(ingredientPass){
                            mutex.withLock {
                                recipeIdList.add(recipeBasicInfo.id)
                            }
                        }

                    }
                }
            }
        }
        return recipeIdList
    }

    /** * 레시피 재료가 필터 옵션에 맞는지 체크, 맞다면 true, 맞지 않으면 false 반납*/
    private suspend fun ingredientFilterCheck(recipeIngredientList: List<Ingredient>, filter:Filter): Boolean{
        var excludeOk = true // 검색 옵션으로 제외한 재료가 ingredient에 존재할 경우 false -> return false
        var includeOk = (filter.includeIngredient == null)
        val include = filter.includeIngredient
        val exclude = filter.excludeIngredient

        withContext(Dispatchers.Default){
            async {
                include?.let {
                    recipeIngredientList.forEach {
                        if(include.contains(it.name)){
                            includeOk = true
                            Log.d("LOG_CHECK", "Search :: ingredientFilterCheck() -> ${it.name}은 포함 목록에 포함돼 있다. includeOk : $includeOk")
                            cancel()
                        }
                    }
                }
            }
            async {
                exclude?.let {
                    recipeIngredientList.forEach {
                        if(exclude.contains(it.name)) {
                            excludeOk = false
                            Log.d("LOG_CHECK", "Search :: ingredientFilterCheck() -> ${it.name}은 제외 목록에 포함돼 있다. excludeOk : $excludeOk")
                            this.cancel()
                        }
                    }
                }
            }
        }.await()
        Log.d("LOG_CHECK", "Search :: ingredientFilterCheck() -> done excludeOk : $excludeOk , includeOk : $includeOk")
        return (excludeOk && includeOk)
    }

    /** * 레시피 정보가 필터 옵션에 맞는지 체크, 맞다면 true, 맞지 않으면 false 반납*/
    private suspend fun basicInfoFilterCheck(recipeBasicInfo: RecipeBasicInfo, filter:Filter): Boolean{
        var isOk = false
        withContext(Dispatchers.Default){

        }
        return isOk
    }

    private suspend fun getRecipesBasicInfo(recipeIdsList: List<String>): List<RecipeBasicInfo>{
        val recipeBasicInfoList = mutableListOf<RecipeBasicInfo>()
        val recipeRef = db.getReference("recipes")
        mutex = Mutex()

        withContext(Dispatchers.IO){
            recipeIdsList.map { id->
                async {
                    val basicInfo = recipeRef.child(id).child("basicInfo").get().await().getValue(RecipeBasicInfo::class.java)!!
                    mutex.withLock{
                        recipeBasicInfoList.add(basicInfo)
                        Log.d("LOG_CHECK", "Search :: getUsersInfo() -> 추가 완료  recipeBasicInfoList : $recipeBasicInfoList")
                    }
                }
            }.awaitAll()
        }
        Log.d("LOG_CHECK", "Search :: getUsersInfo() -> 완료 userInfoList : $recipeBasicInfoList")
        return recipeBasicInfoList
    }

    private suspend fun getRecipeMainImage(recipeBasicInfoList: List<RecipeBasicInfo>): List<Uri>{
        val mainImageList = MutableList(recipeBasicInfoList.size) { Uri.EMPTY }
        val recipeImageRef = storage.getReference("recipe_image")
        val defaultImageUri = Uri.parse("android.resource://$packageName/${R.drawable.default_recipe_main_image}")
        withContext(Dispatchers.IO){
            recipeBasicInfoList.mapIndexed { index, recipeBasicInfo ->
                async {
                    val imagePath = recipeBasicInfo.mainImagePath
                    if(imagePath != null){
                        val recipeId = recipeBasicInfo.id
                        val imageUri = recipeImageRef
                            .child(recipeId)
                            .child("main_image")
                            .child(imagePath)
                            .downloadUrl
                            .await()
                        mainImageList.add(index, imageUri)
                    }
                    else{
                        mainImageList.add(index, defaultImageUri)
                    }
                }
            }.awaitAll()
        }
        return mainImageList
    }

    private suspend fun getCreatorList(recipeBasicInfoList: List<RecipeBasicInfo>): List<String>{
        val creatorList = MutableList(recipeBasicInfoList.size) { "" }
        val userRef = db.getReference("users")
        withContext(Dispatchers.IO){
            recipeBasicInfoList.mapIndexed { index, recipeBasicInfo ->
                async {
                    val creatorId = recipeBasicInfo.id.split("_")[1]
                    val creatorName = userRef
                        .child(creatorId)
                        .child("name")
                        .get()
                        .await()
                        .value
                        .toString()
                    Log.d("LOG_CHECK", "Search :: getCreatorList() -> " +
                            "id : $creatorId" +
                            "name : $creatorName")
                    creatorList.add(index, "$creatorName @$creatorId")
                }
            }
        }.awaitAll()
        return creatorList
    }
}