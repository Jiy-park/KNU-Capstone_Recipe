package com.example.capstone_recipe.create_test

import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

const val CLIENT_ID = "Go_XjbV2R0rHCIZjXRes"
const val CLIENT_SECRET = "h53sIKc0Fk"

data class ResultTransferPapago (
    var message: Message
)

data class Message(
    var result: Result
)

data class Result (
    var srcLangType: String = "",
    var tarLangType: String = "",
    var translatedText: String = ""
)

interface NaverApi{
    @FormUrlEncoded
    @POST("v1/papago/n2mt")
    fun transferPapago(
        @Header("X-Naver-Client-Id") clientId: String,
        @Header("X-Naver-Client-Secret") clientSecret: String,
        @Field("source") source: String,
        @Field("target") target: String,
        @Field("text") text: String
    ): Call<ResultTransferPapago>
}

class TextTranslate {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://openapi.naver.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val service = retrofit.create(NaverApi::class.java)

    fun translate(originText: String, translatedResult: (text: String)-> Unit){
        val call = service.transferPapago(CLIENT_ID, CLIENT_SECRET, "ko", "en", originText)
        call.enqueue(object : Callback<ResultTransferPapago> {
            override fun onResponse(call: Call<ResultTransferPapago>, response: Response<ResultTransferPapago>) {
                if (response.isSuccessful) {
                    val translatedText = response.body()?.message?.result?.translatedText?: ""
                    if(translatedText.isNotEmpty()){
                        Log.d("LOG_CHECK", "TextTranslate :: onResponse() -> 변환 성공 : $originText -> $translatedText")
                        translatedResult(translatedText.replace(", ", " and "))
                    }
                }
                else {
                    Log.e("ERROR", "TextTranslate :: onResponse() -> 변환 실패 : response is null")
                }
            }

            override fun onFailure(call: Call<ResultTransferPapago>, t: Throwable) {
                Log.e("ERROR", "TextTranslate :: onFailure() -> 변환 실패 $t")
            }
        })
    }
}

//class TestActivity3 : AppCompatActivity() {
//    private val binding by lazy { ActivityTest3Binding.inflate(layoutInflater) }
//    private lateinit var service: NaverApi
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(binding.root)
//
//        val retrofit = Retrofit.Builder()
//            .baseUrl("https://openapi.naver.com/")
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//
//
//        service = retrofit.create(NaverApi::class.java)
//
//        binding.btn.setOnClickListener {
//            val from = binding.from.text.toString()
//            lifecycleScope.launch {
//                val eee = search(from)
//                Log.d("LOG_CHECK", "TestActivity3 :: onCreate() -> eee : $eee")
//            }
//        }
//    }
//    suspend fun search(from: String): String{
//        var vvv = ""
//        withContext(Dispatchers.Default) {
//            async {
//                val call = service.transferPapago(CLIENT_ID, CLIENT_SECRET, "ko", "en", "테스트입니다. 이거 번역해주세요.")
//                call.enqueue(object : Callback<ResultTransferPapago> {
//                    override fun onResponse(call: Call<ResultTransferPapago>, response: Response<ResultTransferPapago>) {
//                        if (response.isSuccessful) {
//                            vvv = response.body()?.message?.result?.translatedText!!
//                            Log.d("LOG_CHECK", "TestActivity3 :: onResponse() -> success response : $vvv")
//                        }
//                        else {
//                            Log.d("LOG_CHECK", "TestActivity3 :: onResponse() -> fail1 : ${response.errorBody()}"
//                            )
//                        }
//                    }
//
//                    override fun onFailure(call: Call<ResultTransferPapago>, t: Throwable) {
//                        Log.d("LOG_CHECK", "TestActivity3 :: onFailure() -> fail2 : \n\n${t.message}\n\n")
//                    }
//                })
//            }.await()
//
//        }
//        return vvv
//    }
//}