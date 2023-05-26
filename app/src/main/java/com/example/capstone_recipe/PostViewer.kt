package com.example.capstone_recipe

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.capstone_recipe.create_test.RecipeCreateT
import com.example.capstone_recipe.data_class.Ingredient
import com.example.capstone_recipe.data_class.LEVEL
import com.example.capstone_recipe.data_class.RecipeStep
import com.example.capstone_recipe.databinding.ActivityPostViewerBinding
import com.example.capstone_recipe.dialog.DialogFunc
import com.example.capstone_recipe.post_adapter.RecipeIngredientAdapter
import com.example.capstone_recipe.post_adapter.RecipeStepAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class PostViewer : AppCompatActivity() {
    private val binding by lazy { ActivityPostViewerBinding.inflate(layoutInflater) }
    private val storage = FirebaseStorage.getInstance()
    private val db = Firebase.database("https://knu-capstone-f9f55-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private lateinit var context: Context
    private lateinit var recipeIngredientAdapter: RecipeIngredientAdapter
    private lateinit var recipeStepAdapter: RecipeStepAdapter
    private var ingredientList = mutableListOf<Ingredient>()
    private var stepList = mutableListOf<RecipeStep>()
    private var recipeId: String? = null
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        context = binding.root.context
        userId = Preference(context).getUserId()
        recipeId = intent.getStringExtra("recipeId")!!
        intent.removeExtra("recipeId")
        if(recipeId == null) {
            Toast.makeText(context, "인텐트 안됨", Toast.LENGTH_SHORT).show()
            finish()
        }


        lifecycleScope.launch(Dispatchers.IO) {
            if(checkRecipeOwner(userId, recipeId!!)){
                binding.layerPostTitle.ivSymbolFavoriteOff.visibility = View.GONE
                binding.layerPostTitle.ivSymbolSaveToLockerOff.visibility = View.GONE
                binding.layerPostTitle.btnModifyRecipe.visibility = View.VISIBLE
                binding.layerPostTitle.btnDeleteRecipe.visibility = View.VISIBLE
                binding.layerPostTitle.btnModifyRecipe.setOnClickListener {
                    val intent = Intent(context, RecipeCreateT::class.java)
                    intent.putExtra("modifyMode", true)
                    intent.putExtra("recipeId", recipeId)
                    startActivity(intent)
                }
                binding.layerPostTitle.btnDeleteRecipe.setOnClickListener {
                    DialogFunc.deleteRecipeDialog(context, userId, recipeId!!) { userId, recipeId ->
                        deleteRecipe(userId, recipeId)
                    }
                }
            }
            else{
                checkUserFavoriteThis(userId, recipeId!!)
                checkUserSaveThis(userId, recipeId!!)
            }
        }
        setProgress()
        setRecipeById(recipeId!!)

        db.getReference("users")        // 유저의 최근 본 레시피에 레시피 아이디 저장
            .child(Preference(context).getUserId())
            .child("recentRecipe")
            .setValue(recipeId)


        recipeIngredientAdapter = RecipeIngredientAdapter(ingredientList)
        recipeStepAdapter = RecipeStepAdapter(recipeId!!, stepList)

        binding.layerTopPanel.btnBack.setOnClickListener {
            finish()
        }

        binding.recyclerviewRecipeIngredients.adapter = recipeIngredientAdapter     // 재료 리스트
        binding.recyclerviewRecipeIngredients.layoutManager = GridLayoutManager(context, 2)

        binding.recyclerviewRecipeStep.adapter = recipeStepAdapter                  // 단계 리스트
        binding.recyclerviewRecipeStep.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        binding.btnStartTalkingRecipe.setOnClickListener {
            val intent = Intent(context, TalkingRecipe::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra("recipeId", recipeId)
            startActivity(intent)
            finish()
        }

        binding.layerPostTitle.ivSymbolFavoriteOff.setOnClickListener { // 좋아요 버튼 클릭
            val recipeCreator = recipeId!!.split("_")[1]

            val userScoreRef = db.getReference("users") // 유저의 점수
                .child(recipeCreator)
                .child("score")

            val favoriteRef = db.getReference("recipes") // 레시피를 좋아하는 사람
                .child(recipeId!!)
                .child("favoritePeople")

            val recipeScoreRef = db.getReference("recipes") // 레시피의 점수
                .child(recipeId!!)
                .child("basicInfo")
                .child("score")

            lifecycleScope.launch(Dispatchers.IO) {
                async {
                    val userScore = userScoreRef.get().await().value.toString().toInt()
                    userScoreRef.setValue(userScore+1)
                }
                async {
                    favoriteRef.child(userId).setValue(userId)
                }
                async {
                    val recipeScore = recipeScoreRef.get().await().value.toString().toInt()
                    recipeScoreRef.setValue(recipeScore+1)
                }
                withContext(Dispatchers.Main){
                    binding.layerPostTitle.ivSymbolFavoriteOff.visibility = View.GONE
                    binding.layerPostTitle.ivSymbolFavoriteOn.visibility = View.VISIBLE
                }
            }


        }

        binding.layerPostTitle.ivSymbolFavoriteOn.setOnClickListener { // 좋아요 버튼 해제
            val recipeCreator = recipeId!!.split("_")[1]

            val userScoreRef = db.getReference("users") // 유저의 점수
                .child(recipeCreator)
                .child("score")

            val favoriteRef = db.getReference("recipes") // 레시피를 좋아하는 사람
                .child(recipeId!!)
                .child("favoritePeople")

            val recipeScoreRef = db.getReference("recipes") // 레시피의 점수
                .child(recipeId!!)
                .child("basicInfo")
                .child("score")

            lifecycleScope.launch(Dispatchers.IO) {
                async {
                    val userScore = userScoreRef.get().await().value.toString().toInt()
                    userScoreRef.setValue(userScore-1)
                }
                async {
                    favoriteRef.child(userId).removeValue()
                        .addOnFailureListener {
                            Log.e("ERROR", "PostViewer :: onCreate() -> 좋아요 리스트 업데이트 실패")
                        }
                }
                async {
                    val recipeScore = recipeScoreRef.get().await().value.toString().toInt()
                    recipeScoreRef.setValue(recipeScore-1)
                }
                withContext(Dispatchers.Main){
                    binding.layerPostTitle.ivSymbolFavoriteOff.visibility = View.VISIBLE
                    binding.layerPostTitle.ivSymbolFavoriteOn.visibility = View.GONE
                }
            }
        }

        binding.layerPostTitle.ivSymbolSaveToLockerOff.setOnClickListener {
            db.getReference("users")
                .child(userId)
                .child("saveRecipe")
                .child(recipeId!!)
                .setValue(recipeId)
                .addOnSuccessListener {
                    binding.layerPostTitle.ivSymbolSaveToLockerOff.visibility = View.GONE
                    binding.layerPostTitle.ivSymbolSaveToLockerOn.visibility = View.VISIBLE
                }
        }

        binding.layerPostTitle.ivSymbolSaveToLockerOn.setOnClickListener {
            db.getReference("users")
                .child(userId)
                .child("saveRecipe")
                .child(recipeId!!)
                .removeValue()
                .addOnSuccessListener {
                    binding.layerPostTitle.ivSymbolSaveToLockerOff.visibility = View.VISIBLE
                    binding.layerPostTitle.ivSymbolSaveToLockerOn.visibility = View.GONE
                }
        }
    }
    private fun deleteRecipe(userId: String, recipeId: String){
        db.getReference("users").child(userId).child("uploadRecipe").child(recipeId).removeValue()
        db.getReference("recipes").child(recipeId).removeValue()
        // 메인 이미지 삭제 과정
        val mainImageRef = storage.getReference("recipe_image")
            .child(recipeId)
            .child("main_image")


        mainImageRef.listAll()
            .addOnSuccessListener {
                if(it.items.isNotEmpty()){
                    val path = it.items[0].path.split("/")[4]
                    mainImageRef.child(path).delete()
                }
            }
        // 메인 이미지 삭제 완료

        // 단계 별 이미지 삭제 과정
        val stepImageRef = storage.getReference("recipe_image")
            .child(recipeId)
            .child("step")
        stepImageRef.listAll()
            .addOnSuccessListener {
                if(it.items.isNotEmpty()){
                     for(i in it.items){
                         val path = i.path.split("/")[4]
                         stepImageRef.child(path).delete()
                     }
                }
            }

        db.getReference("users").child(userId).child("recentRecipe").setValue("")
        finish()
    }

    private fun checkRecipeOwner(userId: String, recipeId: String): Boolean{
        val recipeOwner = recipeId.split("_")[1]
        return userId == recipeOwner
    }

     private suspend fun checkUserFavoriteThis(userId: String, recipeId: String){ // 유저가 레시피를 보관함에 넣었는지?
         val list = db.getReference("recipes")
             .child(recipeId)
             .child("favoritePeople")
             .get()
             .await()
             .children

         withContext(Dispatchers.Main){
             binding.layerPostTitle.ivSymbolFavoriteOn.visibility = View.GONE
             binding.layerPostTitle.ivSymbolFavoriteOff.visibility = View.VISIBLE
             for(i in list){
                 if(i.value == userId){
                     binding.layerPostTitle.ivSymbolFavoriteOn.visibility = View.VISIBLE
                     binding.layerPostTitle.ivSymbolFavoriteOff.visibility = View.GONE
                     break
                 }
             }
         }
     }

    private suspend fun checkUserSaveThis(userId: String, recipeId: String){ // 유저가 레시피를 보관함에 넣었는지?
        val list = db.getReference("users")
            .child(userId)
            .child("saveRecipe")
            .get()
            .await()
            .children

        withContext(Dispatchers.Main){
            binding.layerPostTitle.ivSymbolSaveToLockerOn.visibility = View.GONE
            binding.layerPostTitle.ivSymbolSaveToLockerOff.visibility = View.VISIBLE
            for(i in list){
                if(i.value.toString() == recipeId){
                    binding.layerPostTitle.ivSymbolSaveToLockerOn.visibility = View.VISIBLE
                    binding.layerPostTitle.ivSymbolSaveToLockerOff.visibility = View.GONE
                    break
                }
            }
        }
    }

    private fun setProgress(){
        Glide.with(this)
            .asGif()
            .load(R.drawable.progress)
            .into(binding.ivProgressImage)
    }


    private fun setRecipeById(recipeId: String){
        val recipesRef = db.getReference("recipes").child(recipeId)

        val basicInfo = recipesRef.child("basicInfo")
        setBasicInfo(basicInfo)

        val ingredient = recipesRef.child("ingredient")
        setIngredient(ingredient)

        val step = recipesRef.child("step")
        setStep(step)
    }

    private fun viewIsReady(){
        binding.linearProgress.visibility = View.GONE
    }

    private fun setBasicInfo(basicInfo: DatabaseReference){
        basicInfo.addListenerForSingleValueEvent(object: ValueEventListener{
            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    //knu-capstone-f9f55.appspot.com/recipe_image/20230412105019_q/main_image/q_main.jpeg
                    binding.layerPostTitle.tvPostTitle.text = snapshot.child("title").value.toString()  // 제목
                    binding.layerPostTitle.tvPostIntro.text = snapshot.child("intro").value.toString()  // 소개
                    binding.tvCreateTime.text = snapshot.child("time").value.toString()+"분"
                    binding.tvCreateServing.text = snapshot.child("amount").value.toString()+"인분"
                    binding.tvCreateLevel.text = snapshot.child("level").getValue(LEVEL::class.java)?.toKor
                    setImageByPath(recipeId!!, snapshot.child("mainImagePath").value.toString(), binding.ivMainImage)
                    setUserInfo(recipeId!!, binding.layerPostTitle.tvPostCreator)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "ERROR 0 : 불러 오기에 실패 했습니다.", Toast.LENGTH_SHORT).show()
                Log.e("ERROR", "PostViewer :: onCancelled() -> 레시피 기본 정보 실패")
            }
        })
    }

    private fun setUserInfo(recipeId: String, targetView: TextView){
        val userId = recipeId.split("_")[1]
        db.getReference("users")
            .child(userId)
            .addListenerForSingleValueEvent(object: ValueEventListener{
                @SuppressLint("SetTextI18n")
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        val id = snapshot.child("id").value.toString()
                        val name = snapshot.child("name").value.toString()
                        setProfileUri(userId)
                        targetView.text = "$name @$id"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ERROR", "PostViewer :: onCancelled() -> error $error")
                }
            })
    }
    /** *아이디에 해당하는 유저의 프로필 이미지 세팅 */
    private fun setProfileUri(userId: String){
        val defaultUri = Uri.parse("android.resource://$packageName/${R.drawable.default_user_profile_image}")!!
        db.getReference("users")
            .child(userId)
            .child("profileImagePath")
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val imagePath = snapshot.value.toString()
                    if(imagePath.isNotEmpty()){
                        Log.d("LOG_CHECK", "PostViewer :: onDataChange() -> image path : $imagePath")
                        storage.getReference("user_image")
                            .child(userId)
                            .child("profile")
                            .child(imagePath)
                            .downloadUrl
                            .addOnSuccessListener {
                                Glide.with(context)
                                    .load(it)
                                    .fitCenter()
                                    .into(binding.layerPostTitle.ivUserProfileImage)
                            }
                    }
                    else{
                        Glide.with(context)
                            .load(defaultUri)
                            .circleCrop()
                            .into(binding.layerPostTitle.ivUserProfileImage)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun setImageByPath(recipeId: String, path: String, targetView: ImageView){
        if(path.isEmpty()){
            val defaultImage = Uri.parse("android.resource://$packageName/${R.drawable.default_recipe_main_image}")
            targetView.setImageURI(defaultImage)
            viewIsReady()
        }
        else{
            storage
                .getReference("recipe_image")
                .child(recipeId)
                .child("main_image")
                .child(path)
                .downloadUrl
                .addOnSuccessListener { uri->
                    Glide.with(binding.root.context)
                        .load(uri)
                        .into(targetView)
                    viewIsReady()
                }
                .addOnFailureListener { Log.e("ERROR", "PostViewer :: setImageByPath() called :: 실패 :${it.message} 경로 $path") }
        }
    }

    private fun setIngredient(ingredient: DatabaseReference){
        ingredient.addListenerForSingleValueEvent(object: ValueEventListener{
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(data in snapshot.children){
                        data.getValue(Ingredient::class.java)?.let { ingredient->
                            ingredientList.add(ingredient)
                            recipeIngredientAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "ERROR 1 : 불러 오기에 실패 했습니다.", Toast.LENGTH_SHORT).show()
                Log.e("ERROR", "PostViewer :: onCancelled() -> 레시피 재료 실패")
            }
        })
    }

    private fun setStep(step: DatabaseReference){
        step.addListenerForSingleValueEvent(object: ValueEventListener{
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(data in snapshot.children){
                        data.getValue( RecipeStep::class.java)?.let { step->
                            stepList.add(step)
                            recipeStepAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "ERROR 1 : 불러 오기에 실패 했습니다.", Toast.LENGTH_SHORT).show()
                Log.e("ERROR", "PostViewer :: onCancelled() -> 레시피 재료 실패")
            }
        })
    }
}