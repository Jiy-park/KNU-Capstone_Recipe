package com.example.capstone_recipe.test_______
//
//import android.annotation.SuppressLint
//import android.content.Intent
//import android.os.Bundle
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.ViewGroup
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.net.toUri
//import androidx.lifecycle.lifecycleScope
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.bumptech.glide.Glide
//import com.example.capstone_recipe.data_class.LEVEL
//import com.example.capstone_recipe.data_class.RecipeBasicInfo
//import com.example.capstone_recipe.data_class.RecipeStep
//import com.example.capstone_recipe.databinding.ActivityRetrofitTestBinding
//import com.example.capstone_recipe.databinding.ItemLockerRecipeViewerBinding
//import com.example.capstone_recipe.post_viewer.PostViewer
//import com.example.capstone_recipe.test_convert.RecipeInfoFromApi
//import com.example.capstone_recipe.test_convert.test
//import kotlinx.coroutines.*
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//import retrofit2.http.GET
//import retrofit2.http.Path
//
//
//const val KEY = "9b7fe38c011d460499c7"
//const val RECIPE_SEARCH_URL = "https://openapi.foodsafetykorea.go.kr/api/"
//
//interface RecipeApiService {
//    @GET("$KEY/{serviceId}/{dataType}/{startIdx}/{endIdx}")
//    fun getRecipes1(
//        @Path("serviceId") serviceId: String,
//        @Path("dataType") dataType: String,
//        @Path("startIdx") startIdx: String,
//        @Path("endIdx") endIdx: String
//    ):Call<test>
//}
//
//class RetrofitTest: AppCompatActivity() {
//    private lateinit var service: RecipeApiService
//    lateinit var adapter: SearchApiRecipeAdapter
//    val binding by lazy { ActivityRetrofitTestBinding.inflate(layoutInflater) }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(binding.root)
//        adapter = SearchApiRecipeAdapter()
//        binding.recyclerview.layoutManager = LinearLayoutManager(binding.root.context, LinearLayoutManager.VERTICAL, false)
//        binding.recyclerview.adapter = adapter
//
//        val retrofit = Retrofit.Builder()
//            .baseUrl(RECIPE_SEARCH_URL)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//        service = retrofit.create(RecipeApiService::class.java)
//
//        binding.btn.setOnClickListener {
//            val startIdx = binding.fromPage.text.toString()
//            val endIdx = binding.toPage.text.toString()
//            searchFromApi(startIdx, endIdx)
//        }
//    }
//    private fun searchFromApi(startIdx: String, endIdx: String){
//        val call = service.getRecipes1("COOKRCP01", "json", startIdx, endIdx)
//        call.enqueue(object: Callback<test>{
//            @SuppressLint("NotifyDataSetChanged")
//            override fun onResponse(call: Call<test>, response: Response<test>) {
//                if(response.isSuccessful){
//                    var beforeList = response.body()?.COOKRCP01?.row ?: emptyList()
//                    val filter = binding.filer.text.toString()
//                    val recipeBasicInfoList = mutableListOf<RecipeBasicInfo>()
//                    val recipeStepLists = mutableListOf<List<RecipeStep>>()
//                    val recipeIngredientList = mutableListOf<String>()
//                    if(filter.isNotEmpty()){
//                        beforeList = beforeList.filter {
//                            it.title.contains(filter)
//                        }
//                    }
//                    val index = 1
//                    val ingredientList = beforeList[index].ingredientList.split(", ")
//                    val id = beforeList[index].recipeId
//                    val title = beforeList[index].title
//                    Log.d("LOG_CHECK", "RetrofitTest :: onResponse() -> id : $id title : $title")
//                    Log.d("LOG_CHECK", "RetrofitTest :: onResponse() -> \ningredientList : $ingredientList")
//                    if(beforeList.isNotEmpty()) {
//                        lifecycleScope.launch(Dispatchers.Default) {
//                            recipeBasicInfoList += getRecipeBasicInfoListFromApi(beforeList)
//                            recipeStepLists += getRecipeStepListFromApi(beforeList)
//                            recipeIngredientList += getRecipeIngredientListFromApi(beforeList)
//                            withContext(Dispatchers.Main){
//                                adapter.recipeBasicInfoList = recipeBasicInfoList
//                                adapter.recipeList = beforeList
//                                adapter.notifyDataSetChanged()
//                            }
//                        }
//                    }
//                    else { Toast.makeText(binding.root.context, "검색 겳과 X", Toast.LENGTH_SHORT).show() }
//                }
//                else{
//                    Log.e("ERROR", "RetrofitTest :: onResponse() -> response is fail")
//                }
//            }
//            override fun onFailure(call: Call<test>, t: Throwable) {
//                Log.e("ERROR", "RetrofitTest :: onFailure() -> call is fail : $t")
//            }
//        })
//    }
//
//
//
//    /** * API 결과로 받은 레시피 목록에서 각 레시피에 대해서 RecipeBasicInfo 를 만들어 리스트 형태로 반납*/
//    private suspend fun getRecipeBasicInfoListFromApi(beforeList: List< RecipeInfoFromApi>): List<RecipeBasicInfo>{
//        val afterList = MutableList<RecipeBasicInfo>(beforeList.size) { RecipeBasicInfo() }
//        withContext(Dispatchers.Default){
//            beforeList.mapIndexed { index, recipeInfoFromApi ->
//                async {
//                    afterList[index].apply {
//                        id = recipeInfoFromApi.recipeId
//                        title = recipeInfoFromApi.title
//                        intro = recipeInfoFromApi.intro
//                        mainImagePath = recipeInfoFromApi.mainImageUri
//                        time = "-"
//                        amount = "1"
//                        level = LEVEL.NONE
//                    }
//                }
//            }.awaitAll()
//        }
//        return afterList
//    }
//
//    /** * API 결과로 받은 레시피 목록에서 각 레시피에 대하여 RecipeStep 리스트를 만들고, 모든 레시피의 step list를 하나의 리스트로 반납*
//     *- 레시피_1_스텝_리스트   /  레시피_2_스텝_리스트 /... <= 이러한 형태*/
////    private suspend fun getRecipeStepListFromApi(beforeList: List< RecipeInfoFromApi>): List<List<RecipeStep>>{
////        val afterList = MutableList<List<RecipeStep>>(beforeList.size) { listOf<RecipeStep>() }
////        withContext(Dispatchers.Default){
////            beforeList.mapIndexed { index, recipeInfoFromApi ->
////                async {
////                    val stepList = mutableListOf<RecipeStep>()
////                    for(i in 1..20){
////                        val recipeStep = getStepFromApi(recipeInfoFromApi, i)
////                        if(recipeStep.explanation.isEmpty() && recipeStep.imagePath.isEmpty()) { break }
////                        stepList.add(recipeStep)
////                    }
////                    afterList[index] = stepList
////                }
////            }.awaitAll()
////        }
////        return afterList
////    }
//
//    /** * API 결과로 받은 레시피의 단계 중 index 번째 단계의 설명, 이미지를 RecipeStep 형태로 반납, 없을 경우 "" 반납*/
////    private fun getStepFromApi(recipeInfoFromApi: RecipeInfoFromApi, index: Int): RecipeStep{
////        return when(index){
////            1 ->  { RecipeStep(recipeInfoFromApi.MANUAL01, recipeInfoFromApi.MANUAL_IMG01)  }
////            2 ->  { RecipeStep(recipeInfoFromApi.MANUAL02, recipeInfoFromApi.MANUAL_IMG02)  }
////            3 ->  { RecipeStep(recipeInfoFromApi.MANUAL03, recipeInfoFromApi.MANUAL_IMG03)  }
////            4 ->  { RecipeStep(recipeInfoFromApi.MANUAL04, recipeInfoFromApi.MANUAL_IMG04)  }
////            5 ->  { RecipeStep(recipeInfoFromApi.MANUAL05, recipeInfoFromApi.MANUAL_IMG05)  }
////            6 ->  { RecipeStep(recipeInfoFromApi.MANUAL06, recipeInfoFromApi.MANUAL_IMG06)  }
////            7 ->  { RecipeStep(recipeInfoFromApi.MANUAL07, recipeInfoFromApi.MANUAL_IMG07)  }
////            8 ->  { RecipeStep(recipeInfoFromApi.MANUAL08, recipeInfoFromApi.MANUAL_IMG08)  }
////            9 ->  { RecipeStep(recipeInfoFromApi.MANUAL09, recipeInfoFromApi.MANUAL_IMG09)  }
////            10 -> { RecipeStep(recipeInfoFromApi.MANUAL10, recipeInfoFromApi.MANUAL_IMG10)  }
////            11 -> { RecipeStep(recipeInfoFromApi.MANUAL11, recipeInfoFromApi.MANUAL_IMG11)  }
////            12 -> { RecipeStep(recipeInfoFromApi.MANUAL12, recipeInfoFromApi.MANUAL_IMG12)  }
////            13 -> { RecipeStep(recipeInfoFromApi.MANUAL13, recipeInfoFromApi.MANUAL_IMG13)  }
////            14 -> { RecipeStep(recipeInfoFromApi.MANUAL14, recipeInfoFromApi.MANUAL_IMG14)  }
////            15 -> { RecipeStep(recipeInfoFromApi.MANUAL15, recipeInfoFromApi.MANUAL_IMG15)  }
////            16 -> { RecipeStep(recipeInfoFromApi.MANUAL16, recipeInfoFromApi.MANUAL_IMG16)  }
////            17 -> { RecipeStep(recipeInfoFromApi.MANUAL17, recipeInfoFromApi.MANUAL_IMG17)  }
////            18 -> { RecipeStep(recipeInfoFromApi.MANUAL18, recipeInfoFromApi.MANUAL_IMG18)  }
////            19 -> { RecipeStep(recipeInfoFromApi.MANUAL19, recipeInfoFromApi.MANUAL_IMG19)  }
////            20 -> { RecipeStep(recipeInfoFromApi.MANUAL20, recipeInfoFromApi.MANUAL_IMG20)  }
////            else -> { RecipeStep() }
////        }
////    }
//
////    private suspend fun getRecipeIngredientListFromApi(beforeList: List< RecipeInfoFromApi>): List<String>{
////        val afterList = MutableList<String>(beforeList.size) { "" }
////
////        withContext(Dispatchers.Default){
////            beforeList.mapIndexed { index, recipeInfoFromApi ->
////                async {
////                    afterList[index] = recipeInfoFromApi.ingredientList
////                }
////            }.awaitAll()
////        }
////        return afterList
////    }
//}
