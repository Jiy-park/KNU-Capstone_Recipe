package com.example.capstone_recipe.recipe_locker

import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.capstone_recipe.Preference
import com.example.capstone_recipe.R
import com.example.capstone_recipe.databinding.ActivityModifyRecipeLockerBinding
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** *내 정보에서 수정버튼을 클릭했을 때 넘어옴 */
class ModifyRecipeLocker : AppCompatActivity() {
    private val binding by lazy { ActivityModifyRecipeLockerBinding.inflate(layoutInflater) }
    private val storage = FirebaseStorage.getInstance()
    private val db = Firebase.database("https://knu-capstone-f9f55-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private lateinit var storagePermission: ActivityResultLauncher<String>  // 저장소 권한
    private lateinit var galleryLauncher: ActivityResultLauncher<String>    //갤러리 열기
    private lateinit var context: Context
    private lateinit var userId: String

    private lateinit var newName: String
    private var newProfile: Uri? = null
    private var newBack: Uri? = null

    private lateinit var profile: Uri
    private lateinit var back: Uri

    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        context = binding.root.context
        userId = Preference(context).getUserId()

        storagePermission = registerForActivityResult(ActivityResultContracts.RequestPermission()){ isGranted-> // 갤러리 권한 요청
            if(isGranted == false) { Toast.makeText(context, "권한을 승인해야 레시피 제작 시 이미지를 첨부할 수 있습니다.", Toast.LENGTH_SHORT).show() }
        }

        galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()){ uri-> // 갤러리 열기
            when(imageView.id){
                binding.ivLockerProfileImage.id -> {
                    if(uri == null) { binding.ivLockerProfileImage.setImageURI(profile) }
                    else {
                        newProfile = uri
                        binding.ivLockerProfileImage.setImageURI(newProfile)
                    }
                }
                binding.ivLockerBackImage.id -> {
                    if(uri == null) { binding.ivLockerBackImage.setImageURI(back) }
                    else{
                        newBack = uri
                        binding.ivLockerBackImage.setImageURI(newBack)
                    }
                }
            }
        }


        initView()
        initViewEvent()

    }

    /** *유저의 기존 이미지들로 뷰 세팅 및 유저의 기존 닉네임으로 설정 */
    private fun initView(){
        profile = Uri.parse(intent.getStringExtra("profileUri")!!)
        back = Uri.parse(intent.getStringExtra("backUri")!!)
        db.getReference("users")
            .child(userId)
            .child("name")
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    binding.editModifyUserName.setText(snapshot.value.toString())
                    Glide.with(context)
                        .load(profile)
                        .into(binding.ivLockerProfileImage)
                    Glide.with(context)
                        .load(back)
                        .into(binding.ivLockerBackImage)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    /** *뷰의 이벤트 세팅 */
    private fun initViewEvent(){
        binding.btnSave.setOnClickListener {// 저장 버튼
            if(checkUpdate()){
                lifecycleScope.launch {
                    uploadModifiedInfo()
                    withContext(Dispatchers.Main){ finish() }
                }
            }
        }

        binding.btnGetProfileImage.setOnClickListener {// 프로필 이미지 선택
            imageView = binding.ivLockerProfileImage
            galleryLauncher.launch("image/*")
        }

        binding.btnGetBackImage.setOnClickListener { // 백그라운드 이미지 선택
            imageView = binding.ivLockerBackImage
            galleryLauncher.launch("image/*")
        }

    }

    /** *저장 전 업데이트 내용 확인.*/
    private fun checkUpdate(): Boolean{
        if(binding.editModifyUserName.text.isEmpty()) {
            Toast.makeText(context, "닉네임을 입력해주세요!", Toast.LENGTH_SHORT).show()
            return false
        }
        newName = binding.editModifyUserName.text.toString()
        if(newProfile == null){ newProfile = profile }
        if (newBack == null) { newBack = back }
        hideKeyboard()
        return true
    }

    /** *저장 버튼을 눌렀을 때 호출. 변경된 유저 닉네임, 프로필 이미지, 백그라운드 이미지를 병렬로 데이터베이스에 올림 */
    private suspend fun uploadModifiedInfo(){
        val imageRef = storage.getReference("user_image").child(userId)
        val userRef = db.getReference("users").child(userId)
        withContext(Dispatchers.IO){
            async { // 유저 이름 업데이트
                userRef
                    .child("name")
                    .setValue(newName)
                    .addOnSuccessListener {
                        Log.d("LOG_CHECK", "ModifyRecipeLocker :: uploadModifiedInfo() -> 1$it")
                    }
            }
            async {// 프로필 사진 등록 (유저 정보, 스토리지)
                val profilePath = uriToPath(newProfile, isProfile = true)
                if(profilePath != null){
                    userRef
                        .child("profileImagePath")
                        .setValue(profilePath)
                        .addOnSuccessListener {
                            Log.d("LOG_CHECK", "ModifyRecipeLocker :: uploadModifiedInfo() -> 2$it")
                        }
                    imageRef
                        .child("profile")
                        .child(profilePath)
                        .putFile(newProfile!!)
                        .addOnSuccessListener {
                            Log.d("LOG_CHECK", "ModifyRecipeLocker :: uploadModifiedInfo() -> 3$it")
                        }
                }
            }
            async {// 백그라운드 사진 등록 (유저 정보, 스토리지)
                val backPath = uriToPath(newBack, isProfile = false)
                if(backPath != null){
                    userRef
                        .child("backgroundImagePath")
                        .setValue(backPath)
                        .addOnSuccessListener {
                            Log.d("LOG_CHECK", "ModifyRecipeLocker :: uploadModifiedInfo() -> 4$it")
                        }
                    imageRef
                        .child("back")
                        .child(backPath)
                        .putFile(newBack!!)
                        .addOnSuccessListener {
                            Log.d("LOG_CHECK", "ModifyRecipeLocker :: uploadModifiedInfo() -> 5$it")
                        }
                }
            }
        }
    }

    /** *Uri를 받아 path로 변경 */
    private fun uriToPath(uri: Uri?, isProfile: Boolean): String? {
        if(uri == null) { return null }
        val mimeType = contentResolver?.getType(uri) ?: "/none" //마임타입 ex) images/jpeg
        val ext = mimeType.split("/")[1] //확장자 ex) jpeg

        return if(isProfile) { "${userId}_profile.$ext" }  // step이 -1인 경우 메인 이미지로 간주
        else { "${userId}_back.$ext" }             // step 이미지인 경우 각 스텝 번호를 이미지 경로에 부여
    }

    /** *현재 키보드가 올라와 있는 상태면 강제로 내림 */
    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (currentFocus != null) { imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0) }
    }
}