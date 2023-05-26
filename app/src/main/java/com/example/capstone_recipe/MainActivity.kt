package com.example.capstone_recipe

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.capstone_recipe.create_test.RecipeCreateT
import com.example.capstone_recipe.data_class.RecipeBasicInfo
import com.example.capstone_recipe.databinding.ActivityMainBinding
import com.example.capstone_recipe.dialog.DialogFunc
import com.example.capstone_recipe.recipe_create.RecipeCreate
import com.example.capstone_recipe.recipe_locker.RecipeLocker
import com.example.capstone_recipe.search.Search
import com.example.capstone_recipe.test_______.TestActivity3
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val db = Firebase.database("https://knu-capstone-f9f55-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private val storage = Firebase.storage
    private lateinit var context: Context
    private var pressTime = 0L //뒤로가기 키 두번 누르는거
    private val timeInterval = 1000L
    private var userId = ""

    private fun testFunction(){ // 테스트 용
        binding.tvTop.setOnClickListener {
            val intent = Intent(context, TestActivity3::class.java)
            startActivity(intent)
        }
    }

//     사용자가 해당 액티비티를 처음 시작할때,
//     해당 액티비티를 벗어나고 다시 들어올때 실행
//     onCreate -> onStart -> onStop(액티비티가 더 이상 보이지 않음) -> onRestart -> onStart
    override fun onStart() {
        super.onStart()
        lifecycleScope.launch(Dispatchers.IO) {
            val recentRecipeBasicInfo = getRecentRecipe(userId)
            withContext(Dispatchers.Main){
                setRecentRecipe(recentRecipeBasicInfo)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        context = binding.root.context
        userId = Preference(context).getUserId()

        testFunction() // 테스트용

        binding.tvSearchTrigger.setOnClickListener {
            val intent = Intent(context, Search::class.java)
            startActivity(intent)
        }

        binding.ivRecipeLocker.setOnClickListener {
            val intent = Intent(context, RecipeLocker::class.java)
            intent.putExtra("lockerOwnerId", userId)
            intent.putExtra("page", 2)
            startActivity(intent)
        }

        binding.ivRecipeCreate.setOnClickListener {
            val intent = Intent(context, RecipeCreate::class.java)
            startActivity(intent)
        }

        binding.ivUserInfo.setOnClickListener {
            val intent = Intent(context, RecipeLocker::class.java)
            intent.putExtra("lockerOwnerId", userId)
            intent.putExtra("page", 0)
            startActivity(intent)
        }

        binding.ivSetting.setOnClickListener {
            DialogFunc.settingDialog(context)
        }

        binding.layerRecentRecipe.root.setOnClickListener {
//            Toast.makeText(context, "@@", Toast.LENGTH_SHORT).show()
            db.getReference("users")
                .child(Preference(context).getUserId())
                .child("recentRecipe")
                .get()
                .addOnSuccessListener {
                    Toast.makeText(context, it.value.toString(), Toast.LENGTH_SHORT).show()
                    val intent = Intent(context, PostViewer::class.java)
                    intent.putExtra("recipeId", it.value.toString())
                    startActivity(intent)
                }
                .addOnFailureListener {
                    Log.e("LOG_CHECK", "MainActivity :: onCreate() -> $it")
                }
        }

    }

    private suspend fun getRecentRecipe(userId: String): RecipeBasicInfo? {
        val recentRecipeId = db.getReference("users")
            .child(userId)
            .child("recentRecipe")
            .get()
            .await()
            .value
            .toString()

        if(recentRecipeId == "" ) { return null }
        val recipeBasicInfo = db.getReference("recipes")
            .child(recentRecipeId)
            .child("basicInfo")
            .get()
            .await()
        return if(recipeBasicInfo.exists()) { recipeBasicInfo.getValue(RecipeBasicInfo::class.java)!! }
                else { null }
    }

    @SuppressLint("SetTextI18n")
    private suspend fun setRecentRecipe(recentRecipeBasicInfo: RecipeBasicInfo?){
//        TODO("recipeBasicInfo -> 사용자가 최근에 본 레시피가 없을 때 빈 인포가 들어와 널값을 참조할 수 없음")
        if(recentRecipeBasicInfo == null){
            binding.layerRecentRecipe.root.visibility = View.GONE
            binding.tvRecentRecipe.visibility = View.GONE
            return
        }

        var imageUri = Uri.parse("android.resource://$packageName/${R.drawable.default_recipe_main_image}")
        if(recentRecipeBasicInfo.mainImagePath != ""){ // main 이미지가 디폴트 이미지일 경우 TODO("이미지 업로드 할 떄 이미지 경로 바꾸는 함수 고쳐야 할듯")
            imageUri = storage.getReference("recipe_image")
                .child(recentRecipeBasicInfo.id)
                .child("main_image")
                .child(recentRecipeBasicInfo.mainImagePath!!)
                .downloadUrl
                .await()!!
        }

        val userId = recentRecipeBasicInfo.id.split("_")[1] //yyyyMMddHHmmss_유저아이디
        val userName = db.getReference("users")
            .child(userId)
            .child("name")
            .get()
            .await()
            .value
            .toString()

        Glide.with(context)
            .load(imageUri)
            .into(binding.layerRecentRecipe.ivRecipeMainImage)
        binding.layerRecentRecipe.run {
            tvRecipeTitle.text = recentRecipeBasicInfo.title
            tvRecipeIntro.text = recentRecipeBasicInfo.intro
            tvRecipeCreator.text = "$userName @$userId"
            recipeLike.text = recentRecipeBasicInfo.score.toString()
            recipeTime.text = recentRecipeBasicInfo.time + "분"
            recipeLevel.text = recentRecipeBasicInfo.level.toKor
        }

        binding.layerRecentRecipe.root.visibility = View.VISIBLE
        binding.tvRecentRecipe.visibility = View.VISIBLE
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean { // 뒤로가기 버튼 액션
        val tempTime = System.currentTimeMillis()
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            if(tempTime - pressTime in 0..timeInterval) { finish() }
            else {
                Toast.makeText(context, "한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()
                pressTime = System.currentTimeMillis()
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}