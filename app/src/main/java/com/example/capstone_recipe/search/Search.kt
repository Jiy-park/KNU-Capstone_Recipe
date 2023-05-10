package com.example.capstone_recipe.search

import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.capstone_recipe.Preference
import com.example.capstone_recipe.R
import com.example.capstone_recipe.data_class.Ingredient
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
    private val foundByIngredientList = mutableListOf<String>()

    private val test_containIngredient = mutableListOf<String>("아침햇살", "계란")
    private val test_withoutIngredient = mutableListOf<String>("1")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        context = binding.root.context

        searchUserAdapter = SearchUserAdapter()
        binding.recyclerviewUserResult.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.recyclerviewUserResult.adapter = searchUserAdapter

        binding.editSearch.setOnEditorActionListener { _, actionId, _ ->
            if(actionId == EditorInfo.IME_ACTION_SEARCH){
                val searchTarget = binding.editSearch.text.toString()
                lifecycleScope.launch(Dispatchers.IO) {
                    val userIdList = searchUserByName(searchTarget)
                    val userInfoList = getUsersInfo(userIdList)
                    Log.d("LOG_CHECK", "Search :: onCreate() -> \nuserIdList : $userIdList\nuserInfoList : $userInfoList")
                    withContext(Dispatchers.Main){
                        searchUserAdapter.updateUserList(userInfoList)
                    }
                }
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

    /**
     * containIngredient -> 포함할 재료들
     * withoutIngredient -> 제외할 재료들
     * 위의 두 인자를 받아 조건을 만족하는 레시피의 아이디를 foundByIngredientList 에 저장*/
    private suspend fun searchRecipeByIngredient(containIngredient: List<String>, withoutIngredient: List<String>) {
        foundByIngredientList.clear()
        mutex = Mutex()
        withContext(Dispatchers.IO) {
            db.getReference("recipes").get().await().children.map { recipe ->
                async {
                    val recipeId = recipe.child("basicInfo").child("id").value.toString()
                    val ingredientList = recipe.child("ingredient").getValue<List<Ingredient>>()!! // 모든 레시피의 재료들을 Ingredient List 형태로 받아냄
                    Log.d("LOG_CHECK", "Search :: searchByIngredient() -> ingredientList: $ingredientList")
                    var isContain = "" // 포함 재료 목록에 해당하는 재료가 있을 시, 해당 레시피의 아이디를 임시 저장후, 재외 재료 체크가 끝나면, 리스트에 추가해줌
                    var isContainWithoutItem = false
                    ingredientList.map { ingredient ->
                        async {
                            Log.d("LOG_CHECK", "Search :: searchByIngredient() -> ingredient : ${ingredient.name}")
                            if (withoutIngredient.contains(ingredient.name)) {
                                isContainWithoutItem = true
                                Log.d("LOG_CHECK", "Search :: searchByIngredient() -> ${ingredient.name}은 제외 목록에 포함 돼있다. isContainWithoutItem : $isContainWithoutItem")
                                cancel()
                            }
                            if (containIngredient.contains(ingredient.name)) {
                                isContain = recipeId
                                Log.d("LOG_CHECK", "Search :: searchByIngredient() -> ${ingredient.name}은 포함 목록에 포함 돼있다. isContainWithoutItem : $isContainWithoutItem isContain : $isContain")
                            }
                        }
                    }.awaitAll()
                    if(!isContainWithoutItem && isContain.isNotEmpty()) {
                        mutex.withLock {
                            foundByIngredientList.add(isContain)
                        }
                    }
                    Log.d("LOG_CHECK", "Search :: searchByIngredient() -> 내부 async 완료 $foundByIngredientList")
                }
            }
            Log.d("LOG_CHECK", "Search :: searchByIngredient() -> 외부 async 완료 $foundByIngredientList")
        }
        Log.d("LOG_CHECK", "Search :: searchByIngredient() ->foundRecipeIdList : $foundByIngredientList ")
    }
}