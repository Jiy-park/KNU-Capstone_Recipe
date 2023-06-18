package com.example.capstone_recipe.search.fragment

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.capstone_recipe.R
import com.example.capstone_recipe.data_class.Filter
import com.example.capstone_recipe.data_class.Ingredient
import com.example.capstone_recipe.data_class.RecipeBasicInfo
import com.example.capstone_recipe.databinding.FragmentSearchTabViewerBinding
import com.example.capstone_recipe.search.adapter.SearchApiRecipeAdapter
import com.example.capstone_recipe.search.adapter.SearchRecipeAdapter
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await

class SearchRecipeFragment : Fragment() {
    private var binding: FragmentSearchTabViewerBinding? = null
    private lateinit var context: Context
    private var searchRecipeAdapter = SearchRecipeAdapter()

    private val db = Firebase.database
    private val storage = Firebase.storage

    private var isSearching = false
    private var noResult = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSearchTabViewerBinding.inflate(inflater, container, false)
        binding?.let {
            context = it.root.context

            Glide.with(context)
                .load(R.drawable.progress)
                .into(it.ivProgressImage)

            it.recyclerview.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                adapter = searchRecipeAdapter
            }

            lifecycleScope.launch { updateSearchingView(isSearching, noResult) }
        }

        return binding!!.root
    }

    suspend fun startSearch(searchTarget: String, searchFilter: Filter, callback: (size: Int) -> Unit){
        updateSearchingView(_isSearching = true)
        val recipeIdList = searchRecipeIdByTitleWithFilter(searchTarget, searchFilter)
        val recipeBasicInfoList = getRecipesBasicInfo(recipeIdList)
        val creatorList = getCreatorList(recipeBasicInfoList)
        val mainImageUriList = getRecipeMainImage(recipeBasicInfoList)
        withContext(Dispatchers.Main) {
            searchRecipeAdapter.updateAdapterList(recipeBasicInfoList, creatorList, mainImageUriList)
            updateSearchingView(_isSearching = false, _noResult = recipeBasicInfoList.isEmpty())
            callback(recipeBasicInfoList.size)
        }
    }

    private suspend fun updateSearchingView(_isSearching: Boolean, _noResult: Boolean = false) = withContext(Dispatchers.Main){
        binding?.let {
            if(_isSearching) {
                it.recyclerview.visibility = View.GONE
                it.tvResultSearch.visibility = View.GONE
                it.progress.visibility = View.VISIBLE
            }
            else {
                it.recyclerview.visibility = View.VISIBLE
                it.progress.visibility = View.GONE
                if(_noResult) { it.tvResultSearch.visibility = View.VISIBLE }
                else { it.tvResultSearch.visibility = View.GONE }
            }
        }?: run{
            isSearching = _isSearching
            noResult = _noResult
        }
    }


    /** * 입력받은 타이틀 필터 옵션에 맞는 레시피의 아이디를 리스트 형태로 반납*/
//    TODO: basicInfoFilterCheck() 구현해야 함
    private suspend fun searchRecipeIdByTitleWithFilter(searchTitle: String, filter: Filter): List<String>{
        val mutex = Mutex()
        val recipeIdList = mutableListOf<String>()
        withContext(Dispatchers.IO){
            db.getReference("recipes").get().await().children.map { recipe ->
                async {
                    val recipeBasicInfo = recipe.child("basicInfo").getValue(RecipeBasicInfo::class.java)!!
                    val recipeTitle = recipeBasicInfo.title
                    if((recipeTitle.contains(searchTitle) && searchTitle.isNotEmpty())) { // 해당 레시피의 타이틀에 검색어가 포함돼 있으면 다음 작업 시작
                        val ingredient = recipe.child("ingredient").getValue<List<Ingredient>>()!!
                        val ingredientPass = ingredientFilterCheck(ingredient, filter)
                        val basicInfoPass = basicInfoFilterCheck(recipeBasicInfo, filter)
                        if(ingredientPass && basicInfoPass){
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
                            this.cancel()
                        }
                    }
                }
            }
        }.await()
        return (excludeOk && includeOk)
    }

    /** * 레시피 정보가 필터 옵션에 맞는지 체크, 맞다면 true, 맞지 않으면 false 반납*/
    private fun basicInfoFilterCheck(recipeBasicInfo: RecipeBasicInfo, filter:Filter): Boolean{
        var timeOk = (filter.timeLimit == null)
        var calorieOk = (filter.calorieLimit == null)
        var levelOk = (filter.levelLimit == null)

        filter.timeLimit?.let {
            timeOk = (it.toInt() >= recipeBasicInfo.time.toInt())
        }

        filter.calorieLimit?.let {
//            TODO("칼로리가 RecipeBasicInfo -> RecipeSupplement 이동하면서 " +
//                    "RecipeSupplement 도 가져와야 함 ㅎ..." +
//                    "근데 이거 할거면 다른 영양 성분도 가능하지 않나?")
//            calorieOk = (it.toInt() > recipeBasicInfo.)
            calorieOk = true
        }

        filter.levelLimit?.let {
            levelOk = (it == recipeBasicInfo.level)
        }

        return (timeOk && calorieOk && levelOk)
    }

    private suspend fun getRecipesBasicInfo(recipeIdsList: List<String>): List<RecipeBasicInfo>{
        val recipeBasicInfoList = mutableListOf<RecipeBasicInfo>()
        val recipeRef = db.getReference("recipes")
        val mutex = Mutex()

        withContext(Dispatchers.IO){
            recipeIdsList.map { id->
                async {
                    val basicInfo = recipeRef.child(id).child("basicInfo").get().await().getValue(RecipeBasicInfo::class.java)!!
                    mutex.withLock{
                        recipeBasicInfoList.add(basicInfo)
                    }
                }
            }.awaitAll()
        }
        return recipeBasicInfoList
    }

    /** * 각 레시피 제작자의 이름, 아이디를 가져와 "이름 @아이디" 형태로 만들어 리스트 형태로 반납*/
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
                    creatorList.add(index, "$creatorName @$creatorId")
                }
            }
        }.awaitAll()
        return creatorList
    }

    /** * 각 레시피 메인 이미지를 가져와  리스트 형태로 반납*/
    private suspend fun getRecipeMainImage(recipeBasicInfoList: List<RecipeBasicInfo>): List<Uri>{
        val mainImageList = MutableList(recipeBasicInfoList.size) { Uri.EMPTY }
        val recipeImageRef = storage.getReference("recipe_image")
        withContext(Dispatchers.IO){
            recipeBasicInfoList.mapIndexed { index, recipeBasicInfo ->
                async {
                    val imagePath = recipeBasicInfo.mainImagePath
                    if(imagePath.isNotEmpty()){
                        val recipeId = recipeBasicInfo.id
                        val imageUri = recipeImageRef
                            .child(recipeId)
                            .child("main_image")
                            .child(imagePath)
                            .downloadUrl
                            .await()
                        mainImageList[index] = imageUri
                    }
                }
            }.awaitAll()
        }
        return mainImageList
    }
}