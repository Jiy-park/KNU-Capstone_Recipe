package com.example.capstone_recipe.recipe_locker

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.capstone_recipe.Preference
import com.example.capstone_recipe.R
import com.example.capstone_recipe.databinding.ActivityRecipeLockerBinding
import com.example.capstone_recipe.recipe_locker.locker_adpater.LockerViewPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.database.ktx.values
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.tasks.await

class RecipeLocker: AppCompatActivity() {
    private val binding by lazy { ActivityRecipeLockerBinding.inflate(layoutInflater) }
    private lateinit var context:Context
    private lateinit var adpater: LockerViewPagerAdapter
    private val db = Firebase.database
    private val userRef = db.getReference("users")
    private var lockerOwnerId = ""
    private var lockerOwner: DataSnapshot? = null
    private var userId: String = ""
    private lateinit var image: Pair<Uri, Uri>

    private lateinit var imageChanger :ActivityResultLauncher<Intent>

//    private var userInfo = User()

    private suspend fun getUser(userId: String) = userRef.child(userId).get().await()
    private fun addValueChangeListener(){
        Log.d("LOG_CHECK", "RecipeLocker :: addValueChangeListener() -> val  : ${lockerOwner?.value}")
        userRef.child(userId).addChildEventListener(object: ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                Log.d("LOG_CHECK", "RecipeLocker :: onChildAdded() -> " +
                        "owner : $lockerOwnerId" +
                        "snapshot : ${snapshot.value}")
                Log.d("LOG_CHECK", "RecipeLocker :: onChildAdded() -> previousChildName : $previousChildName")
            }

            @SuppressLint("SetTextI18n")
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                Log.d("LOG_CHECK", "RecipeLocker :: onChildChanged() -> " +
                        "owner : $lockerOwnerId" +
                        "previousChildName : $previousChildName")
                Log.d("LOG_CHECK", "RecipeLocker :: onChildChanged() -> snapshot : ${snapshot.value}")
                when(previousChildName){
                    "id" -> binding.tvUserNameWithId.text = "${snapshot.value} @$lockerOwnerId"
                }

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                Log.d("LOG_CHECK", "RecipeLocker :: onChildRemoved() -> " +
                        "owner : $lockerOwnerId" +
                        "snapshot : ${snapshot.value}")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    @SuppressLint("SetTextI18n")
    private suspend fun setUserView(user: DataSnapshot) = withContext(Dispatchers.Main){
        val name = user.child("name").value.toString()
        val score = user.child("score").value.toString()
        val profilePath = user.child("profileImagePath").value.toString()
        val backPath = user.child("backgroundImagePath").value.toString()
        Log.d("LOG_CHECK", "RecipeLocker :: setUserView() -> profilePath : $profilePath\nbackPath : $backPath")
        image = getUserImage(lockerOwnerId, profilePath, backPath)
        binding.tvUserNameWithId.text = "$name @$lockerOwnerId"
        binding.tvUserScore.text = score
        Glide.with(context)
            .load(image.first)
            .into(binding.ivLockerUserImage)
        Glide.with(context)
            .load(image.second)
            .into(binding.ivLockerBackImage)
    }

    /*** 유저의 아이디, 프로필 이미지 경로, 백그라운드 이미지 경로를 넣어주면, 해당 유저의 프로필, 백 이미지 순으로 반납 */
    private suspend fun getUserImage(userId: String, profilePath: String, backPath: String): Pair<Uri, Uri>{
        val userImageRef = Firebase.storage.getReference("user_image").child(userId)
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
                        userImageRef
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
                            .child("back")
                            .child(backPath)
                            .downloadUrl
                            .await()
                    }
            }.await()
        }
        Log.d("LOG_CHECK", "'RecipeLocker' :: getUserImage() -> return profile : $profile, back : $back")
        return Pair(profile, back)
    }

    /** *유저 아이디에 해당하는 유저의 친구 목록을 리스트 형태로 반납 */
    private suspend fun getUserFriendList(userId: String): List<String> {
        val beforeList = db.getReference("users").child(userId).child("friends").get().await().children.toList()
        var afterList = listOf<String>()
        withContext(Dispatchers.Default){
            afterList = beforeList.map {
                async {
                    it.value.toString()
                }
            }.awaitAll()
        }
        return afterList
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        context = binding.root.context
        userId = Preference(context).getUserId()

        adpater = LockerViewPagerAdapter(null, this@RecipeLocker)

        lockerOwnerId = intent.getStringExtra("lockerOwnerId")!!
        intent.removeExtra("lockerOwnerId")

        binding.viewPager.adapter = adpater

        imageChanger = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            if(result.resultCode == RESULT_OK){
                val newProfileUri = result.data?.getStringExtra("profile")!!
                val newBackUri = result.data?.getStringExtra("back")!!
                image = Pair(newProfileUri.toUri(), newBackUri.toUri())

                Glide.with(context).load(image.first).into(binding.ivLockerUserImage)
                Glide.with(context).load(image.second                                    ).into(binding.ivLockerBackImage)

            }
        }


//        binding.viewPager.currentItem = intent.getIntExtra("page", 0)
//        intent.removeExtra("page")

        if(lockerOwnerId == Preference(context).getUserId()){
            binding.ivModifyUserProfile.visibility = View.VISIBLE
            binding.ivModifyUserProfile.setOnClickListener {
                val intent = Intent(context, ModifyRecipeLocker::class.java)
                startActivity(intent)
            }
        }

        binding.btnAddFriend.setOnClickListener {
            binding.btnAddFriend.visibility = View.GONE
        }

        // 탭 레이아웃에 탭 추가
        val uploadTab = binding.tabLayout.newTab()
        val friendTab = binding.tabLayout.newTab()
        val saveTab = binding.tabLayout.newTab()
        binding.tabLayout.addTab(uploadTab, 0)
        binding.tabLayout.addTab(friendTab, 1)
        binding.tabLayout.addTab(saveTab, 2)

        lifecycleScope.launch(Dispatchers.IO) {
            lockerOwner = getUser(lockerOwnerId)
            addValueChangeListener()
            launch(Dispatchers.Main) {
                setUserView(lockerOwner!!)
                adpater = LockerViewPagerAdapter(lockerOwner, this@RecipeLocker)
                binding.viewPager.adapter = adpater
                if(lockerOwnerId == userId){
                    binding.ivModifyUserProfile.visibility = View.VISIBLE
                    binding.ivModifyUserProfile.setOnClickListener {
                        val intent = Intent(context, ModifyRecipeLocker::class.java)
                        intent.putExtra("profileUri", image.first.toString())
                        intent.putExtra("backUri", image.second.toString())
                        imageChanger.launch(intent)
                    }
                }
                else{ // 현재 보여지는 RecipeLocker의 주인이 현재 앱 사용자와 다름 -> 친구 추가/삭제 가능
                    val userFriendList = getUserFriendList(userId)
                    Log.d("LOG_CHECK", "RecipeLocker :: onCreate() -> \nlockerOwnerId : $lockerOwnerId\nuserFriendList : $userFriendList")
                    if(userFriendList.contains(lockerOwnerId)) { binding.btnRemoveFriend.visibility = View.VISIBLE }
                    else { binding.btnAddFriend.visibility = View.VISIBLE }
                }
                binding.viewPager.currentItem = intent.getIntExtra("page", 0)
                intent.removeExtra("page")
            }
        }

        binding.btnAddFriend.setOnClickListener {
            db.getReference("users")
                .child(userId)
                .child("friends")
                .child(lockerOwnerId)
                .setValue(lockerOwnerId)
                .addOnSuccessListener {
                    binding.btnAddFriend.visibility = View.GONE
                    binding.btnRemoveFriend.visibility = View.VISIBLE
                }
        }
        binding.btnRemoveFriend.setOnClickListener {
            db.getReference("users")
                .child(userId)
                .child("friends")
                .child(lockerOwnerId)
                .removeValue()
                .addOnSuccessListener {
                    binding.btnAddFriend.visibility = View.VISIBLE
                    binding.btnRemoveFriend.visibility = View.GONE
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