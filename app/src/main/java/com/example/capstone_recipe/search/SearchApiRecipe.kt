package com.example.capstone_recipe.search

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import com.example.capstone_recipe.data_class.Filter
import com.example.capstone_recipe.data_class.LEVEL
import com.example.capstone_recipe.data_class.RecipeBasicInfo
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path


const val KEY = "9b7fe38c011d460499c7"
const val RECIPE_SEARCH_URL = "https://openapi.foodsafetykorea.go.kr/api/"

const val SERVICE_ID = "COOKRCP01"
const val DATA_TYPE = "json"
const val START_INDEX = "1"
const val END_INDEX = "1000"

interface RecipeApiService {
    @GET("$KEY/{serviceId}/{dataType}/{startIdx}/{endIdx}")
    fun getRecipes1(
        @Path("serviceId") serviceId: String,
        @Path("dataType") dataType: String,
        @Path("startIdx") startIdx: String,
        @Path("endIdx") endIdx: String
    ): Call<ResultApi>
}

class SearchApiRecipe {
    private val retrofit = Retrofit.Builder()
        .baseUrl(RECIPE_SEARCH_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service = retrofit.create(RecipeApiService::class.java)
    private lateinit var filteredRecipeList: List<RecipeInfoFromApi>
    private lateinit var recipeBasicInfoList: List<RecipeBasicInfo>

    companion object{
        private var originApiRecipeList:  List<RecipeInfoFromApi> = emptyList()
    }

    fun searchFromApi(searchTitle: String, filter: Filter?, callback: (recipeList: List<RecipeInfoFromApi>, recipeBasicInfoList: List<RecipeBasicInfo>) -> Unit){
        Log.d("LOG_CHECK", "SearchApiRecipe :: searchFromApi() -> originApiRecipeList size = ${originApiRecipeList.size}")
        if(originApiRecipeList.isEmpty()){
            val call = service.getRecipes1(SERVICE_ID, DATA_TYPE, START_INDEX, END_INDEX)
            call.enqueue(object: Callback<ResultApi> {
                @SuppressLint("NotifyDataSetChanged")
                override fun onResponse(call: Call<ResultApi>, response: Response<ResultApi>) {
                    if(response.isSuccessful){
                        originApiRecipeList = response.body()?.resultApi?.recipeList ?: emptyList()

                        filteredRecipeList =
                            if(searchTitle.isNotEmpty()) { applyFilter(originApiRecipeList, searchTitle, filter) }
                            else { originApiRecipeList }
                        recipeBasicInfoList = getRecipeBasicInfoList(filteredRecipeList)


                        callback(filteredRecipeList, recipeBasicInfoList)
                    }
                    else{
                        Log.e("ERROR", "RetrofitTest :: onResponse() -> api 검색 실패 response is null")
                    }
                }
                override fun onFailure(call: Call<ResultApi>, t: Throwable) {
                    Log.e("ERROR", "RetrofitTest :: onFailure() -> api 검색 실패 : $t")
                }
            })
        }
        else {
            filteredRecipeList =
                if(searchTitle.isNotEmpty()) { applyFilter(originApiRecipeList, searchTitle, filter) }
                else { originApiRecipeList }
            recipeBasicInfoList = getRecipeBasicInfoList(filteredRecipeList)


            callback(filteredRecipeList, recipeBasicInfoList)
        }
    }

    /** * api 로 받아온 레시피 리스트를 필터링 작업을 아래의 순서대로 함.
     * 1. 제목 필터
     * 2. 레시피 기본 정보 필터 -> api로부터 받은 레시피 이므로 칼로리만 필터링 됨.
     * 3. 재료 관련 필터*/
    private fun applyFilter(sourceList: List<RecipeInfoFromApi>, title: String, filter: Filter?): List<RecipeInfoFromApi>{
        var filteredList =  listOf<RecipeInfoFromApi>()
        filteredList = sourceList.filter { recipe ->
            recipe.title.contains(title)
        }
        filter?.let { _filter ->
            // 칼로리 필터
            _filter.calorieLimit?.let { calorieLimit ->
                filteredList = filteredList.filter { recipe ->
                    recipe.calorie.toDouble() <= calorieLimit.toDouble()
                }
            }
            // 불포함 재료 필터
            _filter.excludeIngredient?.let { excludeIngredientList ->
                excludeIngredientList.forEach { ingredient ->
                    filteredList = filteredList.filterNot { recipe ->
                        recipe.ingredients.contains(ingredient)
                    }
                }
            }

            // 포함 재료 필터
            _filter.includeIngredient?.let { includeIngredientList ->
                includeIngredientList.forEach { ingredient ->
                    filteredList = filteredList.filter { recipe ->
                        recipe.ingredients.contains((ingredient))
                    }
                }
            }
        }
        return filteredList
    }

    /** * 필터링 된 api 레시피의 정보를 바탕으로 각 레시피의 RecipeBasicInfo 리스트를 만들어 반납*/
    private fun getRecipeBasicInfoList(sourceList: List<RecipeInfoFromApi>): List<RecipeBasicInfo>{
        val list = List<RecipeBasicInfo> (sourceList.size) { RecipeBasicInfo() }
        sourceList.mapIndexed { index, recipeInfoFromApi ->
            list[index].apply {
                id = recipeInfoFromApi.recipeId
                title = recipeInfoFromApi.title
                intro = recipeInfoFromApi.intro
                mainImagePath = recipeInfoFromApi.mainImageUri
                time = "-"
                amount = "1"
                level = LEVEL.NONE
            }
        }
        return list
    }
}

data class ResultApi(
    @SerializedName("COOKRCP01") val resultApi: ApiRecipeList
)


data class ApiRecipeList(
    @SerializedName("row") val recipeList: List<RecipeInfoFromApi>,
)

data class RecipeInfoFromApi(
    @SerializedName("RCP_SEQ") val recipeId: String,
    @SerializedName("ATT_FILE_NO_MAIN") val mainImageUri: String,
    @SerializedName("RCP_NM") val title: String,
    @SerializedName("HASH_TAG") val intro: String,
    @SerializedName("RCP_PARTS_DTLS") val ingredients: String,
    @SerializedName("INFO_ENG") val calorie: String,
    @SerializedName("INFO_CAR") val carbohydrate: String,
    @SerializedName("INFO_PRO") val protein: String,
    @SerializedName("INFO_FAT") val fat: String,
    @SerializedName("INFO_NA") val sodium: String,
    @SerializedName("INFO_WGT") val amount: String,
    @SerializedName("MANUAL01") val MANUAL01: String,
    @SerializedName("MANUAL02") val MANUAL02: String,
    @SerializedName("MANUAL03") val MANUAL03: String,
    @SerializedName("MANUAL04") val MANUAL04: String,
    @SerializedName("MANUAL05") val MANUAL05: String,
    @SerializedName("MANUAL06") val MANUAL06: String,
    @SerializedName("MANUAL07") val MANUAL07: String,
    @SerializedName("MANUAL08") val MANUAL08: String,
    @SerializedName("MANUAL09") val MANUAL09: String,
    @SerializedName("MANUAL10") val MANUAL10: String,
    @SerializedName("MANUAL11") val MANUAL11: String,
    @SerializedName("MANUAL12") val MANUAL12: String,
    @SerializedName("MANUAL13") val MANUAL13: String,
    @SerializedName("MANUAL14") val MANUAL14: String,
    @SerializedName("MANUAL15") val MANUAL15: String,
    @SerializedName("MANUAL16") val MANUAL16: String,
    @SerializedName("MANUAL17") val MANUAL17: String,
    @SerializedName("MANUAL18") val MANUAL18: String,
    @SerializedName("MANUAL19") val MANUAL19: String,
    @SerializedName("MANUAL20") val MANUAL20: String,
    @SerializedName("MANUAL_IMG01") val MANUAL_IMG01: String,
    @SerializedName("MANUAL_IMG02") val MANUAL_IMG02: String,
    @SerializedName("MANUAL_IMG03") val MANUAL_IMG03: String,
    @SerializedName("MANUAL_IMG04") val MANUAL_IMG04: String,
    @SerializedName("MANUAL_IMG05") val MANUAL_IMG05: String,
    @SerializedName("MANUAL_IMG06") val MANUAL_IMG06: String,
    @SerializedName("MANUAL_IMG07") val MANUAL_IMG07: String,
    @SerializedName("MANUAL_IMG08") val MANUAL_IMG08: String,
    @SerializedName("MANUAL_IMG09") val MANUAL_IMG09: String,
    @SerializedName("MANUAL_IMG10") val MANUAL_IMG10: String,
    @SerializedName("MANUAL_IMG11") val MANUAL_IMG11: String,
    @SerializedName("MANUAL_IMG12") val MANUAL_IMG12: String,
    @SerializedName("MANUAL_IMG13") val MANUAL_IMG13: String,
    @SerializedName("MANUAL_IMG14") val MANUAL_IMG14: String,
    @SerializedName("MANUAL_IMG15") val MANUAL_IMG15: String,
    @SerializedName("MANUAL_IMG16") val MANUAL_IMG16: String,
    @SerializedName("MANUAL_IMG17") val MANUAL_IMG17: String,
    @SerializedName("MANUAL_IMG18") val MANUAL_IMG18: String,
    @SerializedName("MANUAL_IMG19") val MANUAL_IMG19: String,
    @SerializedName("MANUAL_IMG20") val MANUAL_IMG20: String,
)