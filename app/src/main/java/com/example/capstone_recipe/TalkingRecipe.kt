package com.example.capstone_recipe

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.capstone_recipe.data_class.RecipeStep
import com.example.capstone_recipe.recipe_create.create_fragments.RecipeCreateStepFirst
import com.example.capstone_recipe.databinding.ActivityTalkingRecipeBinding
import com.example.capstone_recipe.talking_recipe_fragment.TalkingRecipeStep
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.util.*

class TalkingRecipe : AppCompatActivity() {
    private val binding by lazy { ActivityTalkingRecipeBinding.inflate(layoutInflater) }
    private val storage = FirebaseStorage.getInstance()
    private val db = Firebase.database("https://knu-capstone-f9f55-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private lateinit var context: Context
    private lateinit var tts: TextToSpeech
    private var stepExplanationList = listOf<String>()
    private var stepImageUriList = listOf<Uri>()
    private var recipeId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        context = binding.root.context
        initTTS()

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.mainFrame, TalkingRecipeStep(tts,0, stepExplanationList, stepImageUriList))
            .commit()


        lifecycleScope.launch(Dispatchers.IO) {
            updateActivityView()
            updateStepList()
            val fragment = supportFragmentManager.findFragmentById(R.id.mainFrame)!!
            supportFragmentManager.beginTransaction()
                .replace(R.id.mainFrame, TalkingRecipeStep(tts,0, stepExplanationList, stepImageUriList))
                .addToBackStack("")
                .commit()
        }

        recipeId = intent.getStringExtra("recipeId")!!
        intent.removeExtra("recipeId")

        binding.layerTopPanel.btnBack.setOnClickListener { finish() }
        binding.btnNext.setOnClickListener { moveRecipeStep(STEP.NEXT) }
        binding.btnPrev.setOnClickListener { moveRecipeStep(STEP.PREV) }
    }
    private fun initTTS(){
        tts = TextToSpeech(context){
            if(it == TextToSpeech.SUCCESS){
                val result = tts.setLanguage(Locale.KOREA);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(binding.root.context, "지원하지 않는 언어입니다.", Toast.LENGTH_SHORT).show();
                }
                else {
                    tts.setPitch(2.0f);
                    tts.setSpeechRate(2.0f);
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private suspend fun updateActivityView(){
        val creatorId = recipeId.split("_")[1]
        var creatorProfileImageUri = Uri.parse("android.resource://${context.packageName}/${R.drawable.default_user_profile_image}")!!
        val creatorRef =  db.getReference("users").child(creatorId)
        withContext(Dispatchers.IO){
            val creatorName = creatorRef
                .child("name")
                .get()
                .await()
                .value
                .toString()

            val creatorProfileImagePath = creatorRef
                .child("profileImagePath")
                .get()
                .await()
                .value
                .toString()

            val recipeTitle = db.getReference("recipes")
                .child(recipeId)
                .child("basicInfo")
                .child("title")
                .get()
                .await()
                .value
                .toString()

            if(creatorProfileImagePath != ""){
                creatorProfileImageUri = storage.getReference("user_image")
                    .child(creatorId)
                    .child("profile")
                    .child(creatorProfileImagePath)
                    .downloadUrl
                    .await()
            }
            withContext(Dispatchers.Main){
                binding.layerRecipeTitle.tvPostCreator.text = "$creatorName @$creatorId"
                Glide.with(context)
                    .load(creatorProfileImageUri)
                    .circleCrop()
                    .into(binding.layerRecipeTitle.ivUserProfileImage)
                binding.layerRecipeTitle.tvPostTitle.text = recipeTitle
            }
        }
    }

    private suspend fun updateStepList(){
        val stepList = db.getReference("recipes")
            .child(recipeId)
            .child("step")
            .get()
            .await()
            .children
            .toList()
        val stepImageRef = storage.getReference("recipe_image")
            .child(recipeId)
            .child("step")

        val newExplanationList = MutableList<String> (stepList.size) { "" }
        val newImageList = MutableList<Uri>(stepList.size) { Uri.EMPTY }
        val defaultImageUri = Uri.parse("android.resource://${context.packageName}/${R.drawable.default_user_profile_image}")!!

        withContext(Dispatchers.IO){
            stepList.mapIndexed { index, dataSnapShot ->
                async {
                    val step = dataSnapShot.getValue(RecipeStep::class.java)!!
                    newExplanationList[index] = step.explanation
                    if(step.imagePath == null) { newImageList[index] = defaultImageUri }
                    else { newImageList[index] = stepImageRef.child(step.imagePath!!).downloadUrl.await() }
                }
            }.awaitAll()
            stepExplanationList = newExplanationList
            stepImageUriList = newImageList
        }
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return if (supportFragmentManager.backStackEntryCount > 1) {
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

    private fun refreshFragment(step:Int, isNext:Boolean = true){
        if(isNext){
            supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(
                    R.anim.animation_enter_from_right,
                    R.anim.animation_exit_to_left,
                    R.anim.animation_enter_from_left,
                    R.anim.animation_exit_to_right
                )
                .replace(R.id.mainFrame, TalkingRecipeStep(tts, step, stepExplanationList, stepImageUriList))
                .addToBackStack("")
                .commit()
        }
        else{
            supportFragmentManager.popBackStack()
        }

    }

    private fun moveRecipeStep(step:STEP){
        val fragment = supportFragmentManager.findFragmentById(R.id.mainFrame) as? TalkingRecipeStep
        if(fragment != null){
            var currentStep = fragment.currentStep
            when(step){
                STEP.NEXT -> {
                    currentStep++
                    if(currentStep == fragment.stepExplanationList.size-1){
                        binding.btnNext.visibility = View.GONE
                    }
                    binding.btnPrev.visibility = View.VISIBLE
                    refreshFragment(currentStep)
                }
                STEP.PREV -> {
                    currentStep--
                    if(currentStep == 0){
                        binding.btnPrev.visibility = View.GONE
                    }
                    binding.btnNext.visibility = View.VISIBLE
                    refreshFragment(currentStep, isNext = false)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        tts.stop()
    }

    private enum class STEP{
        PREV,
        NEXT
    }
}