package com.example.capstone_recipe.create_test

import android.annotation.SuppressLint
import android.util.Log
import com.example.capstone_recipe.data_class.RecipeSupplement
import com.google.gson.annotations.SerializedName
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import kotlin.math.roundToInt

const val APP_ID = "5120b2f5"
const val APP_KEY = "7247cf0f92497c42e69b74f266714b8a"
const val SUPPLEMENT_BASE_URL = "https://api.edamam.com"

data class SupplementResult(
    @SerializedName("healthLabels") val healthLabels: List<String>,
    @SerializedName("totalNutrients") val totalNutrients: DetailNutrients
)

data class DetailNutrients(
    @SerializedName("ENERC_KCAL") val calorie: Nutrient,     // 칼로리
    @SerializedName("FAT") val fat: Nutrient,                // 지방
    @SerializedName("CHOCDF") val carbohydrate: Nutrient,    // 탄수화물
    @SerializedName("PROCNT") val protein: Nutrient,         // 단백질
    @SerializedName("NA") val sodium: Nutrient,              // 나트륨
)

data class Nutrient(
    val label: String,
    val quantity: Double,
    val unit: String
)

//    https://api.edamam.com/api/nutrition-data?app_id=5120b2f5&app_key=7247cf0f92497c42e69b74f266714b8a&ingr=1 potato
interface SupplementApi{
    @GET("/api/nutrition-data")
    fun getSupplement(
        @Query("app_id") appId: String,
        @Query("app_key") appKey: String,
        @Query("ingr") ingredient: String
    ): Call<SupplementResult>
}

class SupplementCalculate{
    private val retrofit = Retrofit.Builder()
        .baseUrl(SUPPLEMENT_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val service = retrofit.create(SupplementApi::class.java)
    // 재료 여러개면 and로 연결

    fun calculateSupplement(ingredient: String, result: (RecipeSupplement)->Unit){
        val call = service.getSupplement(APP_ID, APP_KEY, ingredient)
        call.enqueue(object: Callback<SupplementResult>{
            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call<SupplementResult>, response: Response<SupplementResult>) {
                if(response.isSuccessful){
                    val supplement = response.body()?.totalNutrients
                    val calorie = supplement?.calorie?.quantity?.roundToInt()?: 0
                    val fat = supplement?.fat?.quantity?.roundToInt()?: 0
                    val carbohydrate = supplement?.carbohydrate?.quantity?.roundToInt()?: 0
                    val protein = supplement?.protein?.quantity?.roundToInt()?: 0
                    val sodium = supplement?.sodium?.quantity?.roundToInt()?: 0

                    val calorieUnit = supplement?.calorie?.unit?: ""
                    val fatUnit = supplement?.fat?.unit?: ""
                    val carbohydrateUnit = supplement?.carbohydrate?.unit?: ""
                    val proteinUnit = supplement?.protein?.unit?: ""
                    val sodiumUnit = supplement?.sodium?.unit?: ""

//                    Log.d("LOG_CHECK", "SupplementCalculate :: onResponse() -> \n" +
//                            "calorie : $calorie $calorieUnit\n" +
//                            "fat : $fat $fatUnit\n" +
//                            "carbohydrate : $carbohydrate $carbohydrateUnit\n" +
//                            "protein : $protein $proteinUnit\n" +
//                            "sodium : $sodium $sodiumUnit\n")
                    val recipeSupplement = RecipeSupplement(
                        calorie = "$calorie $calorieUnit",
                        fat = "$fat $fatUnit",
                        carbohydrate = "$carbohydrate $carbohydrateUnit",
                        protein = " $protein $proteinUnit",
                        sodium = "$sodium $sodiumUnit"
                    )
                    result(recipeSupplement)
                }
                else{
                    Log.e("ERROR", "SupplementCalculate :: onResponse() -> 영양 성분 계산 실패 : response is null")
                }
            }
            override fun onFailure(call: Call<SupplementResult>, t: Throwable) {
                Log.e("ERROR", "SupplementCalculate :: onFailure() -> 영양 성분 계산 실패 : $t")
            }
        })
    }

}

//
//class SupplementCalculate: AppCompatActivity() {
//    private val binding by lazy { ActivityTest3Binding.inflate(layoutInflater) }
//    private lateinit var service: SupplementApi
//    private lateinit var retrofit: Retrofit
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(binding.root)
//
//        retrofit = Retrofit.Builder()
//            .baseUrl(SUPPLEMENT_BASE_URL)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//        service = retrofit.create(SupplementApi::class.java)
//
//
//        binding.btn.setOnClickListener {
//            val ingredient = binding.from.text.toString()
//            calculateSupplement(ingredient)
//        }
//    }
//
//
//    // 재료 여러개면 and로 연결
//    fun calculateSupplement(ingredient: String){
//        val call = service.getSupplement(APP_ID, APP_KEY, ingredient)
//        call.enqueue(object: Callback<SupplementResult>{
//            @SuppressLint("SetTextI18n")
//            override fun onResponse(call: Call<SupplementResult>, response: Response<SupplementResult>) {
//                if(response.isSuccessful){
//                    val supplement = response.body()?.totalNutrients
//                    val calorie = supplement?.calorie?.quantity?.roundToInt()?: 0
//                    val fat = supplement?.fat?.quantity?.roundToInt()?: 0
//                    val carbohydrate = supplement?.carbohydrate?.quantity?.roundToInt()?: 0
//                    val protein = supplement?.protein?.quantity?.roundToInt()?: 0
//                    val sodium = supplement?.sodium?.quantity?.roundToInt()?: 0
//
//                    val calorieUnit = supplement?.calorie?.unit?: ""
//                    val fatUnit = supplement?.fat?.unit?: ""
//                    val carbohydrateUnit = supplement?.carbohydrate?.unit?: ""
//                    val proteinUnit = supplement?.protein?.unit?: ""
//                    val sodiumUnit = supplement?.sodium?.unit?: ""
//
//                    binding.to.text = "\ncalorie : $calorie $calorieUnit\n" +
//                            "fat : $fat $fatUnit\n" +
//                            "carbohydrate : $carbohydrate $carbohydrateUnit\n" +
//                            "protein : $protein $proteinUnit\n" +
//                            "sodium : $sodium $sodiumUnit\n"
//
//
//                    Log.d("LOG_CHECK", "SupplementCalculate :: onResponse() -> \n" +
//                            "calorie : $calorie $calorieUnit\n" +
//                            "fat : $fat $fatUnit\n" +
//                            "carbohydrate : $carbohydrate $carbohydrateUnit\n" +
//                            "protein : $protein $proteinUnit\n" +
//                            "sodium : $sodium $sodiumUnit\n")
//
//                }
//                else{
//                    Log.d("LOG_CHECK", "TestActivity5 :: onResponse() -> fail 1")
//                }
//            }
//
//            override fun onFailure(call: Call<SupplementResult>, t: Throwable) {
//                Log.d("LOG_CHECK", "TestActivity5 :: onFailure() -> fail 2 error $t")
//            }
//        })
//    }
//
//}