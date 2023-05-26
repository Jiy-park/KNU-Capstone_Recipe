package com.example.capstone_recipe.test_______
import android.annotation.SuppressLint
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.capstone_recipe.databinding.ActivityTest2Binding
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.*
import kotlin.collections.Map.Entry


class TestActivity2 : AppCompatActivity() {
    private val binding by lazy { ActivityTest2Binding.inflate(layoutInflater) }
    private val context by lazy { binding.root.context }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.searchButton.setOnClickListener { // EditText에서 입력한 음식 원재료 이름을 가져옵니다.
            val foodName = binding.foodEditText.text.toString()

            // TranslateTask를 실행하여 입력된 음식 원재료를 영어로 번역합니다.
            val translateTask = TranslateTask()
            translateTask.execute(foodName)
        }
    }

    private inner class TranslateTask : AsyncTask<String?, Void?, String?>() {
        override fun onPostExecute(translatedText: String?) {
            Log.d("LOG_CHECK", "TranslateTask :: onPostExecute() -> 순서 1")
            if (translatedText == null || translatedText.isEmpty()) {
                Log.d("LOG_CHECK", "TranslateTask :: onPostExecute() -> 123123translatedText : $translatedText")
                Toast.makeText(context, "번역 중 오류가 발생했습니다1.", Toast.LENGTH_SHORT).show()
            } else {
                Log.d("LOG_CHECK", "TranslateTask :: onPostExecute() ->455555translatedText : $translatedText ")
                // 번역된 영어 음식재료 이름을 사용하여 NutritionAnalysisTask를 실행합니다.
                val parameters: MutableMap<String, String> = HashMap()
                parameters["app_id"] = "5120b2f5"
                parameters["app_key"] = "7247cf0f92497c42e69b74f266714b8a"
                parameters["ingr"] = translatedText



                val task= NutritionAnalysisTask()
                task.execute(parameters.toMap())
            }
        }

        override fun doInBackground(vararg params: String?): String? {
            Log.d("LOG_CHECK", "TranslateTask :: onPostExecute() -> 순서 2")
            val clientId = "Go_XjbV2R0rHCIZjXRes"
            val clientSecret = "h53sIKc0Fk"
            var translatedText = ""
            try {
                val text = URLEncoder.encode(params[0], "UTF-8")
                val apiURL = "https://openapi.naver.com/v1/papago/n2mt"
                val url = URL(apiURL)
                val con = url.openConnection() as HttpURLConnection
                Log.d("LOG_CHECK", "TranslateTask :: doInBackground() -> 344444444444444444433333333")

                con.requestMethod = "POST"
                con.setRequestProperty("X-Naver-Client-Id", clientId)
                con.setRequestProperty("X-Naver-Client-Secret", clientSecret)
                val postParams = "source=ko&target=en&text=$text"
                con.doOutput = true
                con.outputStream.write(postParams.toByteArray(charset("UTF-8")))
                con.outputStream.flush()
                con.outputStream.close()
                Log.d("LOG_CHECK", "TranslateTask :: doInBackground() -> 2222222222222222222")

                val responseCode = con.responseCode
                Log.d("LOG_CHECK", "TranslateTask :: doInBackground() -> responseCode : $responseCode")
                val br: BufferedReader = if (responseCode == 200) { // 정상 호출
                    BufferedReader(InputStreamReader(con.inputStream))
                } else {  // 에러 발생
                    BufferedReader(InputStreamReader(con.errorStream))
                }
                val sb = StringBuilder()
                var line = br.readLine()
                Log.d("LOG_CHECK", "TranslateTask :: doInBackground() -> line : ${
                    line
                        .split("translatedText")[1]
                        .split("engineType")[0]
                    
                }")
                val d = line.split("translatedText")[1].split("engineType")[0]
                Log.d("LOG_CHECK", "TranslateTask :: doInBackground() -> asasdadasdasdasdasdasdas : $d")
                val dd = d.substring(3,d.length -3)
                Log.d("LOG_CHECK", "TranslateTask :: doInBackground() -> dd : $dd")
                br.close()
                con.disconnect()
                translatedText = dd
            } catch (e: Exception) {
                Log.d("LOG_CHECK", "번역 중 오류가 발생했습니다2. e : $e" )
            }
//            translatedText = "potato"
            return translatedText
        }
    }

    private inner class NutritionAnalysisTask : AsyncTask<Map<String?, String?>?, Void?, String?>() {
        @SuppressLint("SetTextI18n")
        override fun onPostExecute(result: String?) {

            Log.d("LOG_CHECK", "TranslateTask :: onPostExecute() -> 순서 3")
            if (result == null) {
                binding.allergyTextView.text = "API 요청 중 오류가 발생했습니다3."
            } else {

                val calories =
                    if(result.isNotEmpty()) { result.split("Energy:")[1].split("\n")[0] }
                    else { "null" }

                Log.d("LOG_CHECK", "NutritionAnalysisTask :: onPostExecute() -> result : $calories")

                binding.allergyTextView.text  = result
            }
        }

        override fun doInBackground(vararg params: Map<String?, String?>?): String? {

            Log.d("LOG_CHECK", "TranslateTask :: onPostExecute() -> 순서 4")
            return try {
                // API endpoint URL을 설정합니다.
                val url = "https://api.edamam.com/api/nutrition-data"

                // 파라미터를 URL-encoded 문자열로 변환합니다.
                val postData = StringBuilder()
                params[0]?.forEach { (key, value) ->
                    if (postData.length != 0) {
                        postData.append('&')
                    }
                    postData.append(URLEncoder.encode(key, "UTF-8"))
                    postData.append('=')
                    postData.append(URLEncoder.encode(value, "UTF-8"))
                }

                // API endpoint와 파라미터를 결합하여 URL 객체를 생성합니다.
                val apiEndpoint = URL("$url?$postData")

                // HTTP 연결을 수립하고 요청을 전송합니다.
                val conn = apiEndpoint.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"

                // API 응답을 수신합니다.
                val `in` = BufferedReader(InputStreamReader(conn.inputStream))
                var inputLine: String?
                val response = StringBuilder()
                while (`in`.readLine().also { inputLine = it } != null) {
                    response.append(inputLine)
                }
                `in`.close()

                Log.d("LOG_CHECK", "NutritionAnalysisTask :: doInBackground() -> -----------------------\nresponse : $response")

                // API 응답으로부터 유의항목 정보를 추출합니다.
                val json = JSONObject(response.toString())
                val healthLabels = json.getJSONArray("healthLabels")
                val totalNutrients = json.getJSONObject("totalNutrients")
                val keys = totalNutrients.keys()
                val nutritionInfo = StringBuilder()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val nutrient = totalNutrients.getJSONObject(key)
                    val label = nutrient.getString("label")
                    var quantity = nutrient.getDouble("quantity")
                    quantity = Math.round(quantity * 100.0) / 100.0
                    nutritionInfo.append(label).append(": ").append(quantity).append(" ")
                        .append("\n")
                }


                // 알러지 유의항목 검사를 수행합니다.
                val allergies: MutableList<String> = ArrayList()
                if (!healthLabels.toString().uppercase(Locale.getDefault())
                        .contains("CELERY_FREE")
                ) {
                    allergies.add("셀러리")
                }
                if (!healthLabels.toString().uppercase(Locale.getDefault())
                        .contains("CRUSTACEAN_FREE")
                ) {
                    allergies.add("갑각류")
                }
                if (!healthLabels.toString().uppercase(Locale.getDefault())
                        .contains("DAIRY_FREE")
                ) {
                    allergies.add("유제품")
                }
                if (!healthLabels.toString().uppercase(Locale.getDefault()).contains("EGG_FREE")) {
                    allergies.add("달걀")
                }
                var containsFishFree = false
                for (i in 0 until healthLabels.length()) {
                    if (healthLabels[i].toString().uppercase(Locale.getDefault()) == "FISH_FREE") {
                        containsFishFree = true
                        break
                    }
                }
                if (!containsFishFree) {
                    allergies.add("생선")
                }
                if (!healthLabels.toString().uppercase(Locale.getDefault())
                        .contains("LUPINE_FREE")
                ) {
                    allergies.add("루피넛")
                }
                if (!healthLabels.toString().uppercase(Locale.getDefault())
                        .contains("MOLLUSK_FREE")
                ) {
                    allergies.add("연체동물류")
                }
                if (!healthLabels.toString().uppercase(Locale.getDefault())
                        .contains("MUSTARD_FREE")
                ) {
                    allergies.add("겨자")
                }
                if (!healthLabels.toString().uppercase(Locale.getDefault())
                        .contains("PEANUT_FREE")
                ) {
                    allergies.add("땅콩")
                }
                if (!healthLabels.toString().uppercase(Locale.getDefault())
                        .contains("SESAME_FREE")
                ) {
                    allergies.add("참깨")
                }
                if (!healthLabels.toString().uppercase(Locale.getDefault())
                        .contains("SHELLFISH_FREE")
                ) {
                    allergies.add("조개류")
                }
                if (!healthLabels.toString().uppercase(Locale.getDefault()).contains("SOY_FREE")) {
                    Log.d("LOG_CHECK", "NutritionAnalysisTask :: doInBackground() -> healthLabels: ${healthLabels.toString()}")
                    allergies.add("대두")
                }
                if (!healthLabels.toString().uppercase(Locale.getDefault())
                        .contains("SULPHITE_FREE")
                ) {
                    allergies.add("황산염")
                }
                if (!healthLabels.toString().uppercase(Locale.getDefault())
                        .contains("TREE_NUT_FREE")
                ) {
                    allergies.add("견과류")
                }
                if (!healthLabels.toString().uppercase(Locale.getDefault()).contains("PORK_FREE")) {
                    allergies.add("꿀꿀이")
                }


                // 결과 문자열 생성
                var result: String
                if (allergies.size > 0) {
                    result = "알러지 유의항목\n\n"
                    for (allergy in allergies) {
                        result += "- $allergy\n"
                    }
                } else {
                    result = "해당 음식은 알러지 유의항목이 없습니다."
                }
                result += nutritionInfo
                result
            } catch (e: Exception) {
                Log.e("LOG_CHECK", "에러 발생", e)
                null
            }
        }
    }
}