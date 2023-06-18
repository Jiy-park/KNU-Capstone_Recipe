package com.example.capstone_recipe.create_test

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.capstone_recipe.Preference
import com.example.capstone_recipe.R
import com.example.capstone_recipe.create_test.create_fragment.RecipeCreateComplete
import com.example.capstone_recipe.create_test.create_fragment.RecipeCreateStepFirst
import com.example.capstone_recipe.create_test.create_fragment.RecipeCreateStepSecond
import com.example.capstone_recipe.create_test.create_fragment.RecipeCreateStepThird
import com.example.capstone_recipe.data_class.Ingredient
import com.example.capstone_recipe.data_class.RecipeBasicInfo
import com.example.capstone_recipe.data_class.RecipeStep
import com.example.capstone_recipe.data_class.RecipeSupplement
import com.example.capstone_recipe.databinding.ActivityRecipeCreateBinding
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat

class RecipeCreateT : AppCompatActivity() {
    private val binding by lazy { ActivityRecipeCreateBinding.inflate(layoutInflater) }
    private lateinit var context: Context
    private lateinit var defaultImageUri: Uri  // 이미지 선택 안할 시 나오는 기본 이미지

    private val creatorId by lazy { Preference(context).getUserId() }

    private val db = Firebase.database
    private val storage = Firebase.storage

    private var recipeId = ""
    private var modifyMode = false
    private var currentStep = STEP_FIRST

    private var recipeBasicInfo = RecipeBasicInfo()
    private var recipeSupplement = RecipeSupplement()
    private var recipeIngredientList = listOf<Ingredient>()
    private var recipeStepList = listOf<RecipeStep>(RecipeStep())
    private var recipeStepImageUriList = listOf<Uri>()
    private var recipeMainImageUri: Uri = Uri.EMPTY
    private var recipeMainImageIndex = -1

    private companion object{
        const val STEP_FIRST = 0
        const val STEP_SECOND = 1
        const val STEP_THIRD = 2
        const val STEP_COMPLETE = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        context = binding.root.context
        defaultImageUri = Uri.parse("android.resource://${context.packageName}/${R.drawable.default_recipe_main_image}")
        recipeStepImageUriList = listOf<Uri>( defaultImageUri )


        Glide.with(context)
            .asGif()
            .load(R.drawable.progress)
            .into(binding.ivProgressImage)

        modifyMode = intent.getBooleanExtra("modifyMode", false)
        intent.removeExtra("modifyMode")
        if(modifyMode){
            setProgress(on = true)
            recipeId = intent.getStringExtra("recipeId")!!
            Log.d("LOG_CHECK", "RecipeCreateT :: onCreate() -> recipeId : $recipeId")
            intent.removeExtra("recipeId")
            binding.topPanel.tvTopTitle.text = "레시피 수정"
            lifecycleScope.launch(Dispatchers.IO) {
                getRecipeInfo(recipeId)
                withContext(Dispatchers.Main){
                    replaceFragment(RecipeCreateStepFirst(recipeBasicInfo, recipeIngredientList), isReplace = true)
                    setProgress(on = false)
                }
            }
        }

        setViewEvent()
        replaceFragment(RecipeCreateStepFirst(recipeBasicInfo, recipeIngredientList), isReplace = true)
    }

    /** * 프로그레스 on / off */
    private fun setProgress(on: Boolean){
        if(on){ binding.progress.visibility = View.VISIBLE }
        else { binding.progress.visibility = View.GONE }
    }

    /** * modifyMode == true 인 경우 호출, 수정할 레시피의 정보를 db 로부터 받아와 세팅*/
    private suspend fun getRecipeInfo(recipeId: String){
        val recipeRef = db.getReference("recipes").child(recipeId).get().await()
        recipeBasicInfo = recipeRef.child("basicInfo").getValue(RecipeBasicInfo::class.java)!!
        recipeIngredientList = recipeRef.child("ingredient").getValue<List<Ingredient>>()!!
        recipeStepList = recipeRef.child("step").getValue<List<RecipeStep>>()!!
        withContext(Dispatchers.IO){
            val tempImageUriList = MutableList(recipeStepList.size) { Uri.EMPTY }
            recipeStepList.mapIndexed { index, recipeStep ->
                withContext(Dispatchers.Default) {
                    val imageUri = getImageByPath(recipeStep.imagePath, "step")
                    tempImageUriList[index] = imageUri
                }
                recipeStepImageUriList = tempImageUriList
            }

        }
        recipeMainImageUri = getImageByPath(recipeBasicInfo.mainImagePath, "main_image")
        if(recipeBasicInfo.mainImagePath.isNotEmpty()){
            recipeMainImageIndex = recipeBasicInfo.mainImagePath.split("_")[0].toInt()
        }
    }

    /** * " detailPath / imagePath "에 맞는 이미지 Uri 반납, imagePath == "" 일 경우 defaultUri 반납  */
    private suspend fun getImageByPath(imagePath: String, detailPath: String): Uri{
        return if(imagePath.isEmpty()){ defaultImageUri }
            else{
                storage.getReference("recipe_image")
                    .child(recipeId)
                    .child(detailPath)
                    .child(imagePath)
                    .downloadUrl
                    .await()
            }
    }

    /** * 각 뷰들의 이벤트 정의*/
    private fun setViewEvent(){
        binding.topPanel.btnBack.setOnClickListener { finish() }
        binding.btnNext.setOnClickListener { moveFragment(toNext = true) }
        binding.btnDone.setOnClickListener { moveFragment(toNext = true) }
        binding.btnPrev.setOnClickListener { moveFragment(toNext = false) }
    }

    /** * moveFragment 호출 시 호출. 프래그먼트가 전환되기 전 현재 프래그먼트의 build 함수 호출*/
    private fun callBuildFunc(currentStep: Int){
        when(currentStep){
            STEP_FIRST -> {
                val fragmentFirst = supportFragmentManager.findFragmentById(R.id.mainFrame)!! as RecipeCreateStepFirst
                recipeBasicInfo = fragmentFirst.buildRecipeBasicInfo()
                recipeIngredientList = fragmentFirst.buildRecipeIngredientList()
            }
            STEP_SECOND -> {
                val fragmentSecond = supportFragmentManager.findFragmentById(R.id.mainFrame)!! as RecipeCreateStepSecond
                recipeStepList = fragmentSecond.buildRecipeStepList()
                recipeStepImageUriList = fragmentSecond.buildRecipeImageUriList()
            }
            STEP_THIRD -> {
                val fragmentThird = supportFragmentManager.findFragmentById(R.id.mainFrame)!! as RecipeCreateStepThird
                recipeBasicInfo.shareOption = fragmentThird.buildShareOption()
                recipeMainImageUri = fragmentThird.buildRecipeMainImageUri()
                recipeMainImageIndex = fragmentThird.buildRecipeMainImageIndex()
            }
        }
    }

    /** * 프래그먼트 전환 시 호출. 프래그먼트 전환 전 해당 프래그먼트에서의 정보를 수집하여 받아옴 (callBuildFunc). 후에 각 프래그먼트에 맞게 뷰 설정*/
    private fun moveFragment(toNext: Boolean){
        callBuildFunc(currentStep)
        hideKeyboard()
        currentStep =
            if(toNext) { currentStep + 1 }
            else { currentStep - 1 }

        when(currentStep){
            STEP_FIRST -> {
                if(!toNext) { supportFragmentManager.popBackStack() }
                binding.btnNext.visibility = View.VISIBLE
                binding.btnPrev.visibility = View.GONE
                binding.btnDone.visibility = View.GONE
            }
            STEP_SECOND -> {
                if(toNext) { replaceFragment(RecipeCreateStepSecond(recipeStepList, recipeStepImageUriList)) }
                else { supportFragmentManager.popBackStack() }
                binding.btnNext.visibility = View.VISIBLE
                binding.btnPrev.visibility = View.VISIBLE
                binding.btnDone.visibility = View.GONE
            }
            STEP_THIRD -> {
                if(toNext){ replaceFragment(RecipeCreateStepThird(recipeStepImageUriList, recipeMainImageUri, recipeMainImageIndex, recipeBasicInfo.shareOption)) }
                else { supportFragmentManager.popBackStack() }
                binding.btnNext.visibility = View.GONE
                binding.btnPrev.visibility = View.VISIBLE
                binding.btnDone.visibility = View.VISIBLE
            }
            STEP_COMPLETE -> {
                startUpload()
                binding.btnNext.visibility = View.GONE
                binding.btnPrev.visibility = View.GONE
                binding.btnDone.visibility = View.GONE
                binding.topPanel.root.visibility = View.GONE
            }
        }
    }

    /** * 뒤로가기 키 이벤트 정의 */
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return if (supportFragmentManager.backStackEntryCount in STEP_SECOND..STEP_THIRD) {
                moveFragment(toNext = false)
                true
            }
            else{
                finish()
                true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    /** * fragment 로 화면 전환. isReplace -> 해당 액티비티가 처음 호출 됐을 때만 사용*/
    private fun replaceFragment(fragment: Fragment, isReplace: Boolean = false) {
        supportFragmentManager
            .beginTransaction()
            .let {
                if(isReplace) { it.replace(R.id.mainFrame, fragment) }
                else {
                    it.setCustomAnimations(
                            R.anim.animation_enter_from_right,
                            R.anim.animation_exit_to_left,
                            R.anim.animation_enter_from_left,
                            R.anim.animation_exit_to_right
                        )
                        .add(R.id.mainFrame, fragment)
                        .addToBackStack(currentStep.toString())
                }
            }
            .commit()
    }

    private fun startUpload(){
        setProgress(on = true)
        if(recipeId.isEmpty()) {
            recipeId = makeRecipeId()
            recipeBasicInfo.id = recipeId
        }
        makeUpRecipeInfo()
    }

    private fun uploadAll(){
        lifecycleScope.launch(Dispatchers.IO) {
            async { uploadToUserDB() }.await()
            async { uploadToRecipeDB() }.await()
            async { uploadToStorage() }.await()
        }
        endUpload()
    }

    private fun endUpload(){
        setProgress(on = false)
        replaceFragment(RecipeCreateComplete(recipeId))
    }

    /** * 레시피 아이디를 생성하여 반납. 생성일_작성자_레시피 타이틀*/
    @SuppressLint("SimpleDateFormat")
    private fun makeRecipeId(): String{
        val currentTime = SimpleDateFormat("yyyyMMddHHmmss")
            .format(System.currentTimeMillis()) // 2023 04 09 22 48  형식으로 변경
        return currentTime + "_" + creatorId + "_" + recipeBasicInfo.title
    }

    /** * 레시피에 필요한 정보를 최종 정리 :
     * recipeBasicInfo
     * recipeIngredientList
     * recipeStepList
     * recipeSupplement */
    private fun makeUpRecipeInfo(){
        recipeStepImageUriList.forEachIndexed { index, uri ->
            Log.d("LOG_CHECK", "RecipeCreateT :: makeUpRecipeInfo() -> index : $index , path : ${recipeStepList[index].imagePath} uri : $uri")
            if(uri.toString().startsWith("https://")){
                val extension = recipeStepList[index].imagePath.split(".")[1] // q_step_2.jpeg -> jpeg
                recipeStepList[index].imagePath = "${creatorId}_step_${index}.$extension"// q_step_2.jpeg -> q_step_$index.jpeg
            }
            else {
                Log.d("LOG_CHECK", "RecipeCreateT :: makeUpRecipeInfo() -> inner uri : $uri")
                val imagePath = uriToPath(uri, index)
                recipeStepList[index].imagePath = imagePath

            }
        }

        if(recipeMainImageUri.toString().startsWith("https://")){
            Log.d("LOG_CHECK", "RecipeCreateT :: makeUpRecipeInfo() -> main uri : $recipeMainImageUri")
            val extension = recipeStepList[recipeMainImageIndex].imagePath.split(".")[1] // q_step_2.jpeg -> jpeg
            recipeBasicInfo.mainImagePath = "${recipeMainImageIndex}_${creatorId}_main.$extension"// q_step_2.jpeg -> $mainImageIndex_q_main.jpeg
            Log.d("LOG_CHECK", "RecipeCreateT :: makeUpRecipeInfo() -> recipeBasicInfo.mainImagePath : ${recipeBasicInfo.mainImagePath}")

        }
        else {
            val mainImagePath = uriToPath(recipeMainImageUri)
            recipeBasicInfo.mainImagePath =
                if(mainImagePath.isNotEmpty()) { "${recipeMainImageIndex}_$mainImagePath" }
                else { "" }
        }

        val ingredient = ingredientListToString(recipeIngredientList)
        TextTranslate().translate(ingredient){ translatedText ->
            Log.d("LOG_CHECK", "RecipeCreateT :: makeUpRecipeInfo() -> 변환 후 $translatedText")
            SupplementCalculate().calculateSupplement(translatedText){
                Log.d("LOG_CHECK", "RecipeCreateT :: makeUpRecipeInfo() -> 영양 성분 계산 : $it")
                recipeSupplement = it
                uploadAll()
            }
        }
    }

    /** * 유저 데이터베이스에 해당 레시피의 아이디를 업로드 함*/
    private fun uploadToUserDB(){
        db.getReference("users")
            .child(creatorId)
            .child("uploadRecipe")
            .child(recipeId)
            .setValue(recipeId)
    }

    /** * 레시피 정보를 레시피 데이터베이스에 업로드 함*/
    private fun uploadToRecipeDB(){
        val recipeRef = db.getReference("recipes").child(recipeId)
        val basicInfoPath = recipeRef.child("basicInfo")
        val ingredientPath = recipeRef.child("ingredient")
        val stepPath = recipeRef.child("step")
        val favoritePeoplePath = recipeRef.child( "favoritePeople")
        val supplementPath = recipeRef.child("supplement")

        basicInfoPath.setValue(recipeBasicInfo)
        ingredientPath.setValue(recipeIngredientList)
        favoritePeoplePath.setValue("")
        stepPath.setValue(recipeStepList)
        supplementPath.setValue(recipeSupplement)
    }

    /** * 레시피에 사용된 이미지를 데이터베이스에 업로드 함*/
    private suspend fun uploadToStorage(){
//        TODO("1. 수정할 때 기존의 사진들은 확장자가 이상하게 저장되는 거 같음." +
//                "2. RecipeCreateComplete 클래스 완성하면 끝일듯")
        val recipeImageRef = storage.getReference("recipe_image").child(recipeId)
        val stepRef = recipeImageRef.child("step")
        withContext(Dispatchers.IO){
            recipeStepImageUriList.mapIndexed { index, uri ->
                async {
                    if(uri != defaultImageUri && !uri.toString().startsWith("https://")){
                        stepRef
                            .child(recipeStepList[index].imagePath)
                            .putFile(uri)
                            .addOnFailureListener{
                                Log.e("LOG_CHECK", "RecipeCreateT :: uploadToStorage() -> error in step: $index : $it")
                            }
                    }
                }
            }.awaitAll()
            if(recipeBasicInfo.mainImagePath.isNotEmpty()){
                recipeImageRef
                    .child("main_image")
                    .child(recipeBasicInfo.mainImagePath)
                    .putFile(recipeMainImageUri)
                    .addOnFailureListener {
                        Log.e("LOG_CHECK", "RecipeCreateT :: uploadToStorage() -> error in main : $it")
                    }
            }
        }
        Log.d("LOG_CHECK", "RecipeCreateT :: uploadToStorage() -> recipeBasicInfo.mainImagePath : ${recipeBasicInfo.mainImagePath}")
    }

    /** * 매개변수로 들어온 uri 를 기반으로 imagePath 생성 후 반납, step 값을 통해 단계별 이미지의 사진을 변환할 수 있음*/
    private fun uriToPath(uri: Uri, step:Int = -1): String {
        Log.d("LOG_CHECK", "RecipeCreateT :: uriToPath() -> uri : $uri")
        if(uri == defaultImageUri) { return "" }
        val mimeType = contentResolver?.getType(uri)?: "/none" //마임타입 ex) images/jpeg
        val ext = mimeType.split("/")[1] //확장자 ex) jpeg

        return if(step == -1) { "${creatorId}_main.$ext" }  // step이 -1인 경우 메인 이미지로 간주
        else { "${creatorId}_step_$step.$ext" }             // step 이미지인 경우 각 스텝 번호를 이미지 경로에 부여
    }

    /** * 영양 성분 분석에 필요한 재료 문자열을 만들어줌*/
    private fun ingredientListToString(ingredientList: List<Ingredient>,): String{
        var ingredient = ""
        ingredientList.forEachIndexed() { index, it ->
            ingredient += "${it.name} ${it.amount}"
            if(index < recipeIngredientList.size-1) { ingredient += ", " }
        }
        return ingredient
    }

    /** *현재 키보드가 올라와 있는 상태면 강제로 내림 */
    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (currentFocus != null) { imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0) }
    }
}