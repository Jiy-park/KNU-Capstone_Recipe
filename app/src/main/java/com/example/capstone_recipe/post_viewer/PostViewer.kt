package com.example.capstone_recipe.post_viewer

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.capstone_recipe.Preference
import com.example.capstone_recipe.R
import com.example.capstone_recipe.TalkingRecipe
import com.example.capstone_recipe.create_test.RecipeCreateT
import com.example.capstone_recipe.data_class.Ingredient
import com.example.capstone_recipe.data_class.RecipeBasicInfo
import com.example.capstone_recipe.data_class.RecipeStep
import com.example.capstone_recipe.data_class.RecipeSupplement
import com.example.capstone_recipe.databinding.ActivityPostViewerBinding
import com.example.capstone_recipe.dialog.DialogFunc
import com.example.capstone_recipe.post_viewer.post_adapter.RecipeIngredientAdapter
import com.example.capstone_recipe.post_viewer.post_adapter.RecipeStepAdapter
import com.example.capstone_recipe.recipe_locker.RecipeLocker
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class PostViewer : AppCompatActivity() {
    private val binding by lazy { ActivityPostViewerBinding.inflate(layoutInflater) }
    private lateinit var context: Context
    private lateinit var recipeIngredientAdapter: RecipeIngredientAdapter
    private lateinit var recipeStepAdapter: RecipeStepAdapter

    private val defaultImageUri = Uri.parse("android.resource://$packageName/${R.drawable.default_recipe_main_image}")

    private val db = Firebase.database
    private val storage = Firebase.storage
    private var recipeInfo: DataSnapshot? = null
    private var readerInfo: DataSnapshot? = null
    private var creatorInfo: DataSnapshot? = null
    private var creatorProfileImageUri = defaultImageUri

    private var recipeBasicInfo = RecipeBasicInfo()
    private var recipeMainImageUri = Uri.EMPTY
    private var recipeSupplement = RecipeSupplement()
    private var recipeIngredientList = listOf<Ingredient>(Ingredient())
    private var recipeStepList = listOf<RecipeStep>()
    private var recipeStepImageList = listOf<Uri>()

    companion object {
        const val USER = "user"
        const val API = "api"
    }

    private var recipeId = ""
    private var readerId = ""

    private var from = USER

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        context = binding.root.context


        readerId = Preference(context).getUserId()

        from = intent.getStringExtra("from")?: USER
        intent.removeExtra("from")

        binding.recyclerviewRecipeStep.apply { // 스텝
            this.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            recipeStepAdapter = RecipeStepAdapter()
            this.adapter = recipeStepAdapter
        }

        if(from == USER){
            binding.recyclerviewRecipeIngredientsForUser.apply {// 재료
                this.layoutManager = GridLayoutManager(context, 2)
                recipeIngredientAdapter = RecipeIngredientAdapter()
                this.adapter = recipeIngredientAdapter
            }
        }

        setView(false)
        when(from){
            USER ->{
//                TODO("레시피의 제작자와 현재 열람 중인 유저를 비교하여, 수정 삭제 버튼 뷰 조절")
                recipeId = intent.getStringExtra("recipeId")!!
                setRecentRecipe(recipeId, USER)
                lifecycleScope.launch(Dispatchers.IO) {
                    recipeInfo = getRecipe(recipeId)
                    readerInfo = getReader(readerId)
                    creatorInfo = getCreator(recipeId.split("_")[1])

                    getRecipeInfoFromDB()                    // 레시피 정보 받아옴
                    getCreatorProfileImageUri()              // 포스트의 제작자 프로필 이미지 세팅

                    withContext(Dispatchers.Main){
                        recipeStepAdapter.updateAdapter(recipeStepList, recipeStepImageList, false)
                        recipeIngredientAdapter.updateAdapter(recipeIngredientList, false)
                        if(checkRecipeOwner()){             // 현재 포스트를 읽는 유저가 해당 포스트 제작자인지 확인
                            setViewEvent(true)
                        }
                        else{
                            checkReaderSaveThis(recipeId)   // 현재 포스트를 읽는 유저가 해당 포스트를 보관함에 추가했는지 확인
                            checkReaderFavorite(readerId)   // 현재 포스트를 읽는 유저가 해당 포스트의 좋아요를 눌렀는지 확인
                            setViewEvent(false)
                        }
                        setView(true)
                    }
                }

            }
            API -> {
                getRecipeInfoFromIntent()
                recipeStepAdapter.updateAdapter(recipeStepList, recipeStepImageList, true)
                binding.tvIngredientForApi.text = recipeIngredientList[0].name
//                checkReaderSaveThis(readerId)
//                checkReaderFavorite(readerId)
                setViewEvent(false)
                setView(true)

            }
        }
    }

    private suspend fun getRecipe(recipeId: String) = db.getReference("recipes").child(recipeId).get().await()
    private suspend fun getReader(readerId: String) = db.getReference("users").child(readerId).get().await()
    private suspend fun getCreator(creatorId: String) = db.getReference("users").child(creatorId).get().await()

    private fun setRecentRecipe(recipeId: String, from: String){
        when(from){
            USER -> {
                db.getReference("users").child(readerId).child("recentRecipe").setValue(recipeId)
            }
            API -> {
//                TODO("어케 함?????")
            }
        }
    }

    /** * 인텐트로 넘어온 레시피 정보를 받아옴*/
    private fun getRecipeInfoFromIntent(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            recipeBasicInfo = intent.getParcelableExtra("recipeBasicInfo", RecipeBasicInfo::class.java)?: RecipeBasicInfo()
            recipeStepList = intent.getParcelableArrayListExtra("recipeStepList", RecipeStep::class.java)?: emptyList()
            recipeSupplement = intent.getParcelableExtra("recipeSupplement", RecipeSupplement::class.java)?: RecipeSupplement()
        }
        else {
            recipeBasicInfo = intent.getParcelableExtra<RecipeBasicInfo>("recipeBasicInfo")?: RecipeBasicInfo()
            recipeStepList = intent.getParcelableArrayListExtra<RecipeStep>("recipeStepList")?: emptyList()
            recipeSupplement = intent.getParcelableExtra<RecipeSupplement>("recipeSupplement")?: RecipeSupplement()
        }
        recipeIngredientList[0].name = intent.getStringExtra("recipeIngredientList")?: ""
        intent.extras?.clear()

        recipeId = recipeBasicInfo.id
        recipeMainImageUri = recipeBasicInfo.mainImagePath.toUri()
    }

    /** * recipeId 에 해당하는 레시피의 정보를 받아옴*/
    private suspend fun getRecipeInfoFromDB(){
        recipeBasicInfo = recipeInfo!!.child("basicInfo").getValue(RecipeBasicInfo::class.java)?: RecipeBasicInfo()
        recipeMainImageUri = getRecipeImageUriByPath(recipeBasicInfo.mainImagePath, "main_image")

        recipeIngredientList = recipeInfo!!.child("ingredient").getValue<List<Ingredient>>()?: emptyList()

        recipeSupplement = recipeInfo!!.child("supplement").getValue(RecipeSupplement::class.java)?: RecipeSupplement()

        recipeStepList = recipeInfo!!.child("step").getValue<List<RecipeStep>>()?: emptyList()
        withContext(Dispatchers.IO){
            val tempList = MutableList<Uri>(recipeStepList.size) { defaultImageUri }
            recipeStepList.mapIndexed { index, step ->
                async {
                    tempList[index] = getRecipeImageUriByPath(step.imagePath, "step")
                }
            }.awaitAll()
            recipeStepImageList = tempList
        }

        intent.removeExtra("recipeId")
    }

    /** * 경로에 맞는 이미지 uri 반납, 이미지의 경로가 비었을 경우 defaultImageUri 반납*/
    private suspend fun getRecipeImageUriByPath(imagePath: String, detailPath: String): Uri{
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

    /** * 포스트 제작자의 프로필 이미지 세팅*/
    private suspend fun getCreatorProfileImageUri(){
        creatorInfo?.let { creator ->
            Log.d("LOG_CHECK", "ApiPostViewer :: setCreatorProfile() -> creatorInfo : $creatorInfo")
            val creatorProfileImagePath = creator
                .child("profileImagePath")
                .value
                .toString()

            creatorProfileImageUri =
                if(creatorProfileImagePath.isEmpty()) { defaultImageUri }
                else {
                    storage.getReference("user_image")
                        .child(creator.child("id").value.toString())
                        .child("profile")
                        .child(creatorProfileImagePath)
                        .downloadUrl
                        .await()
                }
        }

    }

    /** * 현재 해당 포스트를 보는 유저가 해당 포스트의 좋아요 버튼 클릭 여부를 확인*/
    private fun checkReaderFavorite(readerId: String){
        recipeInfo?.let {
            it.child("favoritePeople")
                .children
                .forEach { userId ->
                    if(userId.value.toString() == readerId) {
                        binding.layerPostTitle.ivSymbolFavoriteOn.visibility = View.VISIBLE
                        binding.layerPostTitle.ivSymbolFavoriteOff.visibility = View.GONE
                        return
                    }
            }
        }
    }

    /** * 현재 해당 포스트를 보는 유저가 해당 포스트 저장 여부를 확인*/
    private fun checkReaderSaveThis(recipeId: String){
        readerInfo?.let { reader ->
            reader.child("saveRecipe")
                .children
                .forEach {
                        Log.d("LOG_CHECK", "PostViewer :: checkReaderSaveThis() -> " +
                                "${it.value.toString() } --- $recipeId")
                    if(it.value.toString() == recipeId){
                        binding.layerPostTitle.ivSymbolSaveToLockerOn.visibility = View.VISIBLE
                        binding.layerPostTitle.ivSymbolSaveToLockerOff.visibility = View.GONE
                        return
                    }
                }
        }
    }

    /** * 현재 포스트를 읽는 유저가 포스트 제작자인지 확인. 맞으면 true 반납*/
    private fun checkRecipeOwner(): Boolean{
        val creatorId = recipeId.split("_")[1]
        if(creatorId == readerId){
            with(binding.layerPostTitle){
                ivSymbolFavoriteOff.visibility = View.GONE
                ivSymbolFavoriteOn.visibility = View.GONE
                ivSymbolSaveToLockerOn.visibility = View.GONE
                ivSymbolSaveToLockerOff.visibility = View.GONE

                btnDeleteRecipe.visibility = View.VISIBLE
                btnModifyRecipe.visibility = View.VISIBLE
            }
            return true
        }
        return false
    }



    /** * 레시피 정보에 맞게 각 뷰들 세팅*/
    @SuppressLint("SetTextI18n")
    private fun setView(isReady: Boolean) = with(binding){
        if(isReady){
            when(from){
                USER -> {
                    layerPostTitle.tvPostCreator.text = getCreatorNameId()
                    tvCreateTime.text = recipeBasicInfo.time + " 분"
                    Glide.with(context)// 레시피 메인 이미지
                        .load(recipeMainImageUri)
                        .error(R.drawable.default_recipe_main_image)
                        .into(ivMainImage)

                    recyclerviewRecipeIngredientsForUser.visibility = View.VISIBLE
                    tvIngredientForApi.visibility = View.GONE
                }
                API -> {
                    layerPostTitle.tvPostCreator.text = "Toxi @TalkingRecipe"
                    tvCreateTime.text = recipeBasicInfo.time
                    Glide.with(context)// 레시피 메인 이미지
                        .load(recipeBasicInfo.mainImagePath.toUri())
                        .error(R.drawable.default_recipe_main_image)
                        .into(ivMainImage)

                    recyclerviewRecipeIngredientsForUser.visibility = View.GONE
                    tvIngredientForApi.visibility = View.VISIBLE
                }
            }

            layerPostTitle.run{
                tvPostTitle.text = recipeBasicInfo.title
                tvPostIntro.text = recipeBasicInfo.intro
            }
            tvCreateAmount.text = recipeBasicInfo.amount + " 인분"
            tvCreateCalorie.text = recipeSupplement.calorie
            tvCreateLevel.text = recipeBasicInfo.level.toKor

            layerSupplement.run {
                tvSupplement1.text = "탄수화물 " + recipeSupplement.carbohydrate
                tvSupplement2.text = "단백질 " + recipeSupplement.protein
                tvSupplement3.text = "지방 " + recipeSupplement.fat
                tvSupplement4.text = "나트륨 " + recipeSupplement.sodium
            }

            Glide.with(context)// 제작자 프로필 이미지
                .load(creatorProfileImageUri)
                .error(R.drawable.default_recipe_main_image)
                .circleCrop()
                .into(layerPostTitle.ivUserProfileImage)

            linearProgress.visibility = View.GONE
        }
        else {
            Glide.with(context)
                .asGif()
                .load(R.drawable.progress)
                .into(binding.ivProgressImage)
            linearProgress.visibility = View.VISIBLE
        }
    }

    /** * 각 뷰들의 이벤트 설정.
     *- isCreator == ture -> 현재 포스트를 읽는 유저가 포스트 제작자로 판단하여 그에 맞게  이벤트 설정*/
    @Suppress("DeferredResultUnused")
    private fun setViewEvent(isCreator: Boolean) = with(binding) {
        with(layerPostTitle) {
            if (isCreator) {
                btnModifyRecipe.setOnClickListener {
                    val intent = Intent(context, RecipeCreateT::class.java)
                    intent.putExtra("modifyMode", true)
                    intent.putExtra("recipeId", recipeId)
                    startActivity(intent)
                }
                btnDeleteRecipe.setOnClickListener {
                    DialogFunc.deleteRecipeDialog(
                        context,
                        recipeId.split("_")[1],
                        recipeId
                    ) { userId, recipeId ->
                        deleteRecipe(userId, recipeId)
                    }
                }
            }
            else {
                ivSymbolFavoriteOn.setOnClickListener { // 좋아요 -> 좋아요 취소
                    lifecycleScope.launch(Dispatchers.IO) {
                        if(from == USER){
                            val userScoreRef = db.getReference("users") // 유저의 점수
                                .child(recipeId.split("_")[1])
                                .child("score")
                            async {
                                val userScore = userScoreRef.get().await().value.toString().toInt()
                                userScoreRef.setValue(userScore-1)
                            }
                        }

                        val favoriteRef = db.getReference("recipes") // 레시피를 좋아하는 사람
                            .child(recipeId)
                            .child("favoritePeople")
                        val recipeScoreRef = db.getReference("recipes") // 레시피의 점수
                            .child(recipeId)
                            .child("basicInfo")
                            .child("score")

                        async {
                            favoriteRef.child(readerId).removeValue()
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

                ivSymbolFavoriteOff.setOnClickListener { // 좋아요 취소 -> 좋아요
                    val userScoreRef = db.getReference("users") // 유저의 점수
                        .child(recipeId.split("_")[1])
                        .child("score")

                    val favoriteRef = db.getReference("recipes") // 레시피를 좋아하는 사람
                        .child(recipeId)
                        .child("favoritePeople")

                    val recipeScoreRef = db.getReference("recipes") // 레시피의 점수
                        .child(recipeId)
                        .child("basicInfo")
                        .child("score")

                    lifecycleScope.launch(Dispatchers.IO) {
                        async {
                            val userScore = userScoreRef.get().await().value.toString().toInt()
                            userScoreRef.setValue(userScore+1)
                        }
                        async {
                            favoriteRef.child(readerId).setValue(readerId)
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

                val readerRef = db.getReference("users")
                    .child(readerId).child("saveRecipe").child(recipeId)
                ivSymbolSaveToLockerOn.setOnClickListener { // 저장 -> 저장 취소
                    readerRef
                        .removeValue()
                        .addOnSuccessListener {
                            binding.layerPostTitle.ivSymbolSaveToLockerOff.visibility = View.VISIBLE
                            binding.layerPostTitle.ivSymbolSaveToLockerOn.visibility = View.GONE
                        }
                }
                ivSymbolSaveToLockerOff.setOnClickListener {// 저장 취소 -> 저장
                    readerRef
                        .setValue(recipeId)
                        .addOnSuccessListener {
                            binding.layerPostTitle.ivSymbolSaveToLockerOff.visibility = View.GONE
                            binding.layerPostTitle.ivSymbolSaveToLockerOn.visibility = View.VISIBLE
                        }
                }
            }

            ivSymbolShare.setOnClickListener { // 공유 버튼
                Toast.makeText(context, "공유", Toast.LENGTH_SHORT).show()
            }
            ivUserProfileImage.setOnClickListener {// 제작자 프로필 이미지
                if(from == USER){
                    val intent = Intent(context, RecipeLocker::class.java)
                    intent.putExtra("lockerOwnerId", creatorInfo!!.child("id").value.toString())
                    startActivity(intent)
                }
            }
        }
        layerTopPanel.btnBack.setOnClickListener { finish() } // 상단 뒤로가기 키
        btnStartTalkingRecipe.setOnClickListener {// 토킹레시피 버튼
            val creatorName = creatorInfo?.child("name")?.let { it.value.toString() }?: "Toxi"
            val creatorId = creatorInfo?.child("id")?.let { it.value.toString() }?: "TalkingRecipe"
            val intent = Intent(context, TalkingRecipe::class.java)


            Log.d("LOG_CHECK", "ApiPostViewer :: setViewEvent() -> \n" +
                    "from : $from\n" +
                    "recipeTitle : ${recipeBasicInfo.title}\n" +
                    "recipeCreator : ${"$creatorName @$creatorId"}\n" +
                    "creatorProfileImageUri : $creatorProfileImageUri\n" +
                    "recipeStepList : ${ArrayList(recipeStepList)}\n" +
                    "recipeStepImageUriList : $recipeStepImageList\n")

            intent.putExtra("from", from)
            intent.putExtra("recipeTitle", recipeBasicInfo.title)
            intent.putExtra("creator", "$creatorName @$creatorId")
            intent.putExtra("creatorProfileImageUri", creatorProfileImageUri.toString())
            intent.putExtra("recipeStepList", ArrayList(recipeStepList))
            intent.putExtra("recipeStepImageList", ArrayList(recipeStepImageList))

            startActivity(intent)
        }
    }

    private fun getCreatorNameId(): String{
        val creatorName = creatorInfo!!.child("name").value.toString()
        val creatorId = creatorInfo!!.child("id").value.toString()

        return "$creatorName @$creatorId"
    }

    /** * 레시피 아이디에 해당하는 레시피를 삭제함*/
    private fun deleteRecipe(creatorId: String, recipeId: String){
        db.getReference("users").child(creatorId).child("uploadRecipe").child(recipeId).removeValue()
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

        db.getReference("users").child(creatorId).child("recentRecipe").setValue("")
        finish()
    }
}
