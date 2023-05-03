package com.example.capstone_recipe.test_______

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.capstone_recipe.Preference
import com.example.capstone_recipe.R
import com.example.capstone_recipe.data_class.*
import com.example.capstone_recipe.databinding.ActivityTestBinding
import com.example.capstone_recipe.recipe_create.create_fragments.RecipeCreateComplete
import com.example.capstone_recipe.recipe_create.create_fragments.RecipeCreateStepFirst
import com.example.capstone_recipe.recipe_create.create_fragments.RecipeCreateStepSecond
import com.example.capstone_recipe.recipe_create.create_fragments.RecipeCreateStepThird
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import kotlin.system.measureTimeMillis

class TestActivity : AppCompatActivity() {
    val binding by lazy { ActivityTestBinding.inflate(layoutInflater) }
    private val storage = FirebaseStorage.getInstance()
    private val db = Firebase.database("https://knu-capstone-f9f55-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private val pref by lazy { Preference(context) }
    private lateinit var context: Context

    private var recipeId = ""
    private var userId = ""

    private var recipeBasicInfo = RecipeBasicInfo(
        "",
        "",
        "",
        null,
        "10",
        "5",
        LEVEL.EASY,
        SHARE.ONLY_ME,
        0
    )
    private var ingredientList = mutableListOf<Ingredient>(
        Ingredient("재료 1", "1"),
        Ingredient("재료 2", "2"),
        Ingredient("재료 3", "3"),
        Ingredient("재료 4", "4"),
        Ingredient("재료 5", "5"),
        Ingredient("재료 6", "6")
    )
    private var stepExplanationList = mutableListOf<String>("")
    private var stepImageList = mutableListOf<Uri?>(null)
    private var selectedMainImage: Uri? = Uri.parse("android.resource://$packageName/${R.drawable.default_user_profile_image}")!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        context = binding.root.context
        userId = pref.getUserId()
        recipeId = makeRecipeId() + "_for_test"
        var recipeCount = 0

        binding.btn.setOnClickListener {
            lifecycleScope.launch {
                db.getReference("users")
                    .child(userId)
                    .child("uploadRecipe")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if(snapshot.exists()){
                                recipeCount = snapshot.childrenCount.toInt() + 1
                                recipeBasicInfo.id = recipeId
                                recipeBasicInfo.title = "제목 $recipeCount"
                                recipeBasicInfo.intro = "소개 $recipeCount"
                                recipeBasicInfo.mainImagePath = "q_main.none"

                                uploadToUserDB()

                            }
                        }
                        override fun onCancelled(error: DatabaseError) {}
                    })

            }
        }

    }

    @SuppressLint("SimpleDateFormat")
    private fun makeRecipeId(): String{
        val currentTime = SimpleDateFormat("yyyyMMddHHmmss")
            .format(System.currentTimeMillis()) // 2023 04 09 22 48  형식으로 변경
        return currentTime + "_" + userId
    }


    private fun uploadToUserDB(){ // 유저 정보 업데이트 -> 레시피 아이디 추가
        db.getReference("users") // 유저가 올린 레시피들
            .child(userId)
            .child("uploadRecipe")   // root/users/$userid/recipes/...
            .push()
            .setValue(recipeId)
            .addOnCompleteListener {     // 추가 후 처리 = 레시피 정보 등록
                uploadToRecipeDB()
            }
            .addOnFailureListener {
                Log.d("LOG_CHECK", "fail :: $it")
            }
    }

    private fun uploadToRecipeDB(){ // 레시피 정보 업데이트
        val recipePath = db.getReference("recipes").child(recipeId)
        val basicInfoPath = recipePath.child("basicInfo")
        val ingredientPath = recipePath.child("ingredient")
        val stepPath = recipePath.child("step")
        val favoritePeople = recipePath.child("favoritePeople")

        basicInfoPath.setValue(recipeBasicInfo)
        ingredientPath.setValue(ingredientList)
        favoritePeople.setValue("")

        val stepList = mutableListOf<RecipeStep>()
        for(i in 0 until stepExplanationList.size){ // 단계 설명 + 이미지 (null 가능)
            stepList.add(
                RecipeStep(
                    explanation = stepExplanationList[i],       // null 불가
                    imagePath = uriToPath(stepImageList[i], i)) // null 가능
            )
        }
        stepPath.setValue(stepList)
            .addOnCompleteListener {
                uploadToStorage(stepList)
            }

    }

    @SuppressLint("SimpleDateFormat")
    fun uriToPath(uri: Uri?, step:Int = -1): String? { // 스텝 별 이미지
        if(uri == null) { return null }
        Log.d("LOG_CHECK", "RecipeCreate :: uriToPath() -> uri : $uri")
        val mimeType = contentResolver?.getType(uri) ?: "/none" //마임타입 ex) images/jpeg
        val ext = mimeType.split("/")[1] //확장자 ex) jpeg

        return if(step == -1) { "${userId}_main.$ext" }  // step이 -1인 경우 메인 이미지로 간주
        else { "${userId}_step_$step.$ext" }             // step 이미지인 경우 각 스텝 번호를 이미지 경로에 부여
    }

    private fun uploadToStorage(stepList: List<RecipeStep>){ // 이미지 정보 업데이트
        val recipeRef = storage.getReference("recipe_image").child(recipeId)
        Log.d("LOG_CHECK", "test1111111111111111111111111111111111111111111111111")
        val stepRef = recipeRef.child("step")
        for(i in 0 until stepImageList.size){
            if(stepImageList[i] != null){
                stepRef
                    .child(stepList[i].imagePath!!)
                    .putFile(stepImageList[i]!!)
                    .addOnSuccessListener { Log.d("LOG_CHECK", "RecipeCreate :: uploadToStorage() -> complete step $i upload") }
                    .addOnFailureListener { Log.d("LOG_CHECK", "RecipeCreate :: uploadToStorage() -> fail $it") }
            }
        }
        Log.d("LOG_CHECK", "222222222222222222222222222222222222222222222222")
        if(selectedMainImage != null){
            recipeRef
                .child("main_image")
                .child(recipeBasicInfo.mainImagePath!!)
                .putFile(selectedMainImage!!)
                .addOnSuccessListener {
                    Log.d("LOG_CHECK", "RecipeCreate :: uploadToStorage() -> complete main upload ")
                }
                .addOnFailureListener { Log.d("LOG_CHECK", "RecipeCreate :: uploadToStorage() -> fail $it") }
        }
    }
}
