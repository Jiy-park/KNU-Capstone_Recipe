package com.example.capstone_recipe

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.Image
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.capstone_recipe.data_class.Ingredient
import com.example.capstone_recipe.data_class.LEVEL
import com.example.capstone_recipe.data_class.RecipeBasicInfo
import com.example.capstone_recipe.data_class.RecipeStep
import com.example.capstone_recipe.databinding.ActivityPostViewerBinding
import com.example.capstone_recipe.post_adapter.RecipeIngredientAdapter
import com.example.capstone_recipe.post_adapter.RecipeStepAdapter
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.database.ktx.values
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.util.logging.Level
import kotlin.reflect.typeOf

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
                binding.layerPostTitle.btnModifyRecipe.setOnClickListener {
                    Toast.makeText(context, "수정 어떤식으로 해야하나...", Toast.LENGTH_SHORT).show()
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
            startActivity(intent)
            finish()
        }

        binding.layerPostTitle.ivSymbolFavoriteOff.setOnClickListener { // 좋아요 버튼 클릭
            val recipeCreator = recipeId!!.split("_")[1]
            val scoreRef = db.getReference("users")
                .child(recipeCreator)
                .child("score")

            val favoriteRef = db.getReference("recipes")
                .child(recipeId!!)
                .child("favoritePeople")

            scoreRef
                .addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.exists()){
                            scoreRef.setValue(snapshot.value.toString().toInt() + 1)
                            binding.layerPostTitle.ivSymbolFavoriteOff.visibility = View.GONE
                            binding.layerPostTitle.ivSymbolFavoriteOn.visibility = View.VISIBLE
                            favoriteRef
                                .push()
                                .setValue(userId)
                        }
                    }
                    override fun onCancelled(error: DatabaseError) { Log.e("LOG_CHECK", "onCancelled: 뭐가 문제일까? $error", ) }
                })
        }

        binding.layerPostTitle.ivSymbolFavoriteOn.setOnClickListener { // 졸아요 버튼 해제
            val recipeCreator = recipeId!!.split("_")[1]
            val scoreRef = db.getReference("users")
                .child(recipeCreator)
                .child("score")
            scoreRef
                .addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.exists()){
                            val score = snapshot.value.toString().toInt()
                            if(score > 0){
                                scoreRef.setValue(score - 1)
                                binding.layerPostTitle.ivSymbolFavoriteOff.visibility = View.VISIBLE
                                binding.layerPostTitle.ivSymbolFavoriteOn.visibility = View.GONE
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) { Log.e("LOG_CHECK", "onCancelled: 뭐가 문제일까? $error", ) }
                })
        }

        binding.layerPostTitle.ivSymbolSaveToLockerOff.setOnClickListener {
            db.getReference("users")
                .child(userId)
                .child("saveRecipe")
                .push()
                .setValue(recipeId)
                .addOnSuccessListener {
                    binding.layerPostTitle.ivSymbolSaveToLockerOff.visibility = View.GONE
                    binding.layerPostTitle.ivSymbolSaveToLockerOn.visibility = View.VISIBLE
                    Log.d("LOG_CHECK", "PostViewer :: onCreate() -> add save list : user = $userId , recipe = $recipeId")
                }
        }

        binding.layerPostTitle.ivSymbolSaveToLockerOn.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val userRef = db.getReference("users")
                    .child(userId)
                    .child("saveRecipe")

                val userSaveList = userRef
                        .get()
                        .await()
                        .children

                val newSaveList = mutableListOf<String>()
                for(data in userSaveList){
                    if(data.value.toString() == recipeId){ continue }
                    newSaveList.add(data.value.toString())
                }
                userRef.setValue(newSaveList)
                withContext(Dispatchers.Main){
                    binding.layerPostTitle.ivSymbolSaveToLockerOff.visibility = View.VISIBLE
                    binding.layerPostTitle.ivSymbolSaveToLockerOn.visibility = View.GONE
                }
            }

        }
    }

    private  fun checkRecipeOwner(userId: String, recipeId: String): Boolean{
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

        Log.d("LOG_CHECK", "PostViewer :: checkUserFavoriteThis() -> list : $list")

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

        Log.d("LOG_CHECK", "PostViewer :: checkUserSaveThis() -> list : $list")

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
            .load(R.drawable.test2222222)
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
                    Log.d("LOG_CHECK", "PostViewer :: setBasicInfo() -> \n${snapshot.value}")
                    binding.layerPostTitle.tvPostTitle.text = snapshot.child("title").value.toString()  // 제목
                    binding.layerPostTitle.tvPostIntro.text = snapshot.child("intro").value.toString()  // 소개
                    binding.tvCreateTime.text = snapshot.child("time").value.toString()+"분"
                    binding.tvCreateServing.text = snapshot.child("amount").value.toString()+"인분"
                    binding.tvCreateLevel.text = snapshot.child("level").getValue(LEVEL::class.java)?.toKor
                    Log.d("LOG_CHECK", "PostViewer :: onDataChange() -> mainImagePath : ${snapshot.child("mainImagePath").value}")
                    setImageByPath(recipeId!!, snapshot.child("mainImagePath").value.toString(), binding.ivMainImage)
                    setUserInfo(recipeId!!, binding.layerPostTitle.tvPostCreator)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "ERROR 0 : 불러 오기에 실패 했습니다.", Toast.LENGTH_SHORT).show()
                Log.d("LOG_CHECK", "PostViewer :: onCancelled() -> 레시피 기본 정보 실패")
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
                        targetView.text = "$name @$id"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("LOG_CHECK", "PostViewer :: onCancelled() -> error $error")
                }
            })
    }

    private fun setImageByPath(recipeId: String, path: String?, targetView: ImageView){
        if(path == null){
            val defaultImage = Uri.parse("android.resource://$packageName/${R.drawable.ex_img}")
            targetView.setImageURI(defaultImage)
            Log.d("LOG_CHECK", "PostViewer :: setImageByPath() -> success set Image : null")
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
                    Log.d("LOG_CHECK", "PostViewer :: setImageByPath() -> success set Image")
                    viewIsReady()
                }
                .addOnFailureListener { Log.d("LOG_CHECK", "PostViewer :: setImageByPath() called :: 실패 :${it.message} 경로 $path") }
        }
    }

    private fun setIngredient(ingredient: DatabaseReference){
        ingredient.addListenerForSingleValueEvent(object: ValueEventListener{
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    Log.d("LOG_CHECK", "PostViewer :: setIngredient() -> \n${snapshot.value}")
//                    ingredientList = (snapshot.getValue(Ingredient::class.java) as? MutableList<Ingredient>)!!
                    for(data in snapshot.children){
                        Log.d("LOG_CHECK", "PostViewer :: setIngredient() -> data : $data")
                        data.getValue(Ingredient::class.java)?.let { ingredient->
                            Log.d("LOG_CHECK", "PostViewer :: setIngredient() -> \n" +
                                    "name : ${ingredient.name} amount : ${ingredient.amount}")
                            ingredientList.add(ingredient)
                            recipeIngredientAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "ERROR 1 : 불러 오기에 실패 했습니다.", Toast.LENGTH_SHORT).show()
                Log.d("LOG_CHECK", "PostViewer :: onCancelled() -> 레시피 재료 실패")
            }
        })
    }

    private fun setStep(step: DatabaseReference){
        step.addListenerForSingleValueEvent(object: ValueEventListener{
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    Log.d("LOG_CHECK", "PostViewer :: setStep() -> \n${snapshot.value}")
                    for(data in snapshot.children){
                        Log.d("LOG_CHECK", "PostViewer :: setStep() -> data : $data")
                        data.getValue( RecipeStep::class.java)?.let { step->
                            Log.d("LOG_CHECK", "PostViewer :: setStep() -> \n" +
                                    "name : ${step.explanation} amount : ${step.imagePath}")
                            stepList.add(step)
                            recipeStepAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "ERROR 1 : 불러 오기에 실패 했습니다.", Toast.LENGTH_SHORT).show()
                Log.d("LOG_CHECK", "PostViewer :: onCancelled() -> 레시피 재료 실패")
            }
        })
    }
}