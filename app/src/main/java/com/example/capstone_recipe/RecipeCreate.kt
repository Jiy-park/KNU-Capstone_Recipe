package com.example.capstone_recipe

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import androidx.fragment.app.Fragment
import com.example.capstone_recipe.create_fragments.RecipeCreateComplete
import com.example.capstone_recipe.create_fragments.RecipeCreateStepFirst
import com.example.capstone_recipe.create_fragments.RecipeCreateStepSecond
import com.example.capstone_recipe.create_fragments.RecipeCreateStepThird
import com.example.capstone_recipe.data_class.*
import com.example.capstone_recipe.databinding.ActivityRecipeCreateBinding
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat

class RecipeCreate : AppCompatActivity() {
    private val binding by lazy { ActivityRecipeCreateBinding.inflate(layoutInflater) }
    private val storage = FirebaseStorage.getInstance()
    private val db = Firebase.database("https://knu-capstone-f9f55-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private lateinit var context:Context

    private var ingredientList = mutableListOf<RecipeIngredient>()               // 첫번째 - 재료 리스트
    private var createStepList = mutableListOf<RecipeStep>(                     // 두번째 - 단계 리스트
        RecipeStep("", null, 0, 0, 0) // 텍스트 설명,  이미지 uri, 타이머 유무
    )
    private var recipeBasicInfo = RecipeBasicInfo(                              // 첫번째, 세번째 - 레시피 기본 정보
        "",
        "",
        "",
        "",
        LEVEL.EASY,
        null,
        SHARE.ONLY_ME
    )


    private var currentStep = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        context = binding.root.context

        binding.topPanel.btnBack.setOnClickListener { finish() }

        supportFragmentManager
            .beginTransaction()
            .add(R.id.mainFrame, RecipeCreateStepFirst(ingredientList, recipeBasicInfo))
            .addToBackStack(null)
            .commit()


        binding.btnNext.setOnClickListener {
            currentStep++
            checkCurrentStep(currentStep)
        }

        binding.btnPrev.setOnClickListener {
            currentStep--
            if(currentStep == 1) { binding.btnPrev.visibility = View.GONE }
            supportFragmentManager.popBackStack()
        }


    }
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return if (supportFragmentManager.backStackEntryCount > 1) {
                currentStep--
                if(currentStep == 1) { binding.btnPrev.visibility = View.GONE }
                supportFragmentManager.popBackStack()
                true
            }
            else{
                finish()
                true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(
                R.anim.animation_enter_from_right,
                R.anim.animation_exit_to_left,
                R.anim.animation_enter_from_left,
                R.anim.animation_exit_to_right
            )
            .add(R.id.mainFrame, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun checkCurrentStep(currentStep:Int){
        when(currentStep){
            1 -> {
                binding.btnPrev.visibility = View.GONE
                replaceFragment(RecipeCreateStepFirst(ingredientList, recipeBasicInfo))
            }
            2 -> {
                binding.btnPrev.visibility = View.VISIBLE
                binding.btnNext.visibility = View.VISIBLE
                replaceFragment(RecipeCreateStepSecond(createStepList))
            }
            3 -> {
                binding.btnPrev.visibility = View.VISIBLE
                binding.btnNext.visibility = View.VISIBLE
                replaceFragment(RecipeCreateStepThird(recipeBasicInfo, createStepList))
            }
            4 -> {
                binding.topPanel.root.visibility = View.GONE
                binding.btnPrev.visibility = View.GONE
                binding.btnNext.visibility = View.GONE
                uploadNewRecipe()
                replaceFragment(RecipeCreateComplete())
            }
        }
    }
    @SuppressLint("SimpleDateFormat")
    private fun makeRecipeId(userId: String): String{
        val currentTime = SimpleDateFormat("yyyyMMddHHmmss")
            .format(System.currentTimeMillis()) // 2023 04 09 22 48  형식으로 변경
        return userId + "_" + currentTime
    }

    private fun uploadRecipeToRecipeDB(recipeId: String){
        val recipePath = db.getReference("recipes").child(recipeId)
        val basicInfoPath = recipePath.child("basic_info")
        val ingredientPath = recipePath.child("ingredient")
        val stepPath = recipePath.child("step")

        Log.d("LOG_CHECK", "success__33")
        basicInfoPath.setValue(recipeBasicInfo)
        Log.d("LOG_CHECK", "success__1")
        ingredientPath.setValue(ingredientList)
        Log.d("LOG_CHECK", "success__2")
        stepPath.setValue(createStepList)
        Log.d("LOG_CHECK", "success__3")
    }

    private fun uploadNewRecipe(){
        val userId: String = "q" // TODO:: 나중에 sharedPreference 통해 유저의 아이디 가져 오는 것으로 변경할 것
        val userPath = db.getReference("users") // 유저가 올린 레시피들
            .child(userId)
            .child("recipes")   // root/users/$userid/recipes/...

        val recipeId = makeRecipeId(userId)

        userPath
            .child("recipe_id")
            .push()
            .setValue(recipeId)                      // 유저가 업로드하는 레시피의 id를 유저 정보에 추가
            .addOnCompleteListener {     // 추가 후 처리 = 레시피 정보 등록
                Log.d("LOG_CHECK", "success__0")
                uploadRecipeToRecipeDB(recipeId)
            }
            .addOnFailureListener {
                Log.d("LOG_CHECK", "fail :: $it")
            }
    }



        /***
         * 레시피 저장해야 하는거
         * RecipeBasicInfo(                             // 레시피 기본 정보
            var title: String = "",                     // 레시피 제목
            var intro: String = "",                     // 레시피 한 줄 소개
            var time: String = "",                      // 레시피 조리 시간
            var amount:String = "",                     // 레시피 요리 양
            var level: LEVEL = LEVEL.EASY,              // 레시피 난이도
            var mainImage:Uri? = null,                  // 레시피 대표 이미지
            var shareTarget: SHARE = SHARE.ONLY_ME      // 레시피 공개 대상
            )
         *
         */




//        path.setValue()
//
//        val fullPath = makeFilePath("images", "temp", uri)
//        val imageRef = storage.getReference(fullPath)
//        val uploadTask = imageRef.putFile(uri)

        // Show progress dialog while uploading
//        val progressDialog = ProgressDialog(this)
//        progressDialog.setMessage("Uploading image...")
//        progressDialog.setCancelable(false)
//        progressDialog.show()
//
//        uploadTask
//            .addOnFailureListener {
//                progressDialog.dismiss()
//                Log.d("LOG_CHECK", "MainActivity :: uploadImage() called :: 실패 : ${it.message}")
//            }
//            .addOnSuccessListener {
//                progressDialog.dismiss()
//                Log.d("LOG_CHECK", "MainActivity :: uploadImage() called :: 성공 : $fullPath")
//            }
//
//    private fun makeFilePath(path: String, userId: String, uri: Uri): String {
//        val mimeType = contentResolver.getType(uri) ?: "/none" //마임타입 ex) images/jpeg
//        val ext = mimeType.split("/")[1] //확장자 ex) jpeg
//        val timeSuffix = System.currentTimeMillis() // 시간값 ex)123123123
//        return "${path}/${userId}_${timeSuffix}.$ext" // 파일 경로
//    }
}