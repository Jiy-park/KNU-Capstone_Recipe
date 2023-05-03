package com.example.capstone_recipe.recipe_locker

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.capstone_recipe.R
import com.example.capstone_recipe.databinding.ActivityRecipeLockerBinding
import com.example.capstone_recipe.recipe_locker.locker_adpater.LockerViewPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class RecipeLocker: AppCompatActivity() {
    private val binding by lazy { ActivityRecipeLockerBinding.inflate(layoutInflater) }
    private lateinit var context:Context
    private lateinit var adpater: LockerViewPagerAdapter
    private val db = Firebase.database("https://knu-capstone-f9f55-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private val userRef = db.getReference("users")
    private val recipeRef = db.getReference("recipes")
    private var userId = ""
    private var user: DataSnapshot? = null
//    private var userInfo = User()

    private suspend fun getUser(userId: String) = userRef.child(userId).get().await()

    @SuppressLint("SetTextI18n")
    private suspend fun setUserView(user: DataSnapshot) = withContext(Dispatchers.Main){
        val name = user.child("name").value.toString()
        val score = user.child("score").value.toString()
        val profilePath = user.child("profileImagePath").value.toString()
        val backPath = user.child("backgroundImagePath").value.toString()
        val image = getUserImage(userId, profilePath, backPath)
        binding.tvUserNameWithId.text = "$name @$userId"
        binding.tvUserScore.text = score
        binding.ivLockerUserImage.setImageURI(image.first)
        binding.ivLockerBackImage.setImageURI(image.second)
    }

    /*** 유저의 아이디, 프로필 이미지 경로, 백그라운드 이미지 경로를 넣어주면, 해당 유저의 프로필, 백 이미지 순으로 반납 */
    private suspend fun getUserImage(userId: String, profilePath: String, backPath: String): Pair<Uri, Uri>{
        val userImageRef = Firebase.storage.getReference("user_image")
        val defaultProfile = Uri.parse("android.resource://$packageName/${R.drawable.default_user_profile_image}")!!
        val defaultBack = Uri.parse("android.resource://$packageName/${R.drawable.default_user_back_image}")!!
        Log.d("LOG_CHECK", "RecipeLocker :: getUserImage() -> profile : $profilePath, back : $backPath")
        var profile = Uri.EMPTY
        var back = Uri.EMPTY
        withContext(Dispatchers.IO){
            async {
                profile =
                    if (profilePath == "") {
                        defaultProfile
                    } else {
                        userImageRef.child(userId)
                            .child("profile")
                            .child(profilePath)
                            .downloadUrl
                            .await()
                    }
            }.await()
            async{
                back =
                    if (backPath == "") {
                        defaultBack
                    } else {
                        userImageRef
                            .child(userId)
                            .child("background")
                            .child(profilePath)
                            .downloadUrl
                            .await()
                    }
            }.await()
        }
        Log.d("LOG_CHECK", "'RecipeLocker' :: getUserImage() -> return profile : $profile, back : $back")
        return Pair(profile, back)
    }

//    private suspend fun getUserInfo(userId: String): User{
//        val info = userRef.child(userId).get().await()
//        val user = User()
//        user.id = userId
//        user.name = info.child("name").value.toString()
//        user.score = info.child("score").value.toString().toInt()
//        user.profileImagePath = (info.child("profileImagePath").value?:"").toString()
//        user.backgroundImagePath = info.child("backgroundImagePath").value.toString()
//        withContext(Dispatchers.IO){
//            async {
//                for(data in info.child("uploadRecipe").children){
//                    user.uploadRecipe.add(data.value.toString())
//                }
//            }
//            async {
//                for(data in info.child("saveRecipe").children){
//                    user.saveRecipe.add(data.value.toString())
//                }
//            }
//            async {
//                for(data in info.child("friends").children){
//                    user.friends.add(data.getValue(FriendInfo::class.java)!!)
//                }
//            }
//        }.await()
//        return user
//    }
//
//
//    @SuppressLint("SetTextI18n")
//    private fun setUserInfoView(){
//        binding.tvUserNameWithId.text = "${userInfo.name} @${userInfo.id}"
//        binding.tvUserScore.text = userInfo.score.toString()
//        binding.tabLayout.getTabAt(0)?.text = "작성글 ${userInfo.uploadRecipe.size}"
//        binding.tabLayout.getTabAt(1)?.text = "친구 ${userInfo.friends.size}"
//        binding.tabLayout.getTabAt(2)?.text = "보관함 ${userInfo.saveRecipe.size}"
//    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        context = binding.root.context

        adpater = LockerViewPagerAdapter(null, this@RecipeLocker)

        userId = intent.getStringExtra("userId")!!
        intent.removeExtra("userId")

        binding.viewPager.adapter = adpater

        binding.viewPager.currentItem = intent.getIntExtra("page", 0)
        intent.removeExtra("page")


        // 탭 레이아웃에 탭 추가
        val uploadTab = binding.tabLayout.newTab()
        val friendTab = binding.tabLayout.newTab()
        val saveTab = binding.tabLayout.newTab()
        binding.tabLayout.addTab(uploadTab, 0)
        binding.tabLayout.addTab(friendTab, 1)
        binding.tabLayout.addTab(saveTab, 2)

        lifecycleScope.launch(Dispatchers.IO) {
            user = getUser(userId)
            launch(Dispatchers.Main) {
                setUserView(user!!)
                adpater = LockerViewPagerAdapter(user, this@RecipeLocker)
                binding.viewPager.adapter = adpater
            }
        }


        // ViewPager2와 TabLayout 연결
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "작성글"
                1 -> tab.text = "친구"
                2 -> tab.text = "보관함"
            }
        }.attach()
    }

}