package com.example.capstone_recipe.recipe_locker.locker_fragment

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.capstone_recipe.R
import com.example.capstone_recipe.data_class.UserInfo
import com.example.capstone_recipe.databinding.FragmentRecipeLockerFriendListBinding
import com.example.capstone_recipe.recipe_locker.locker_adpater.FriendAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import kotlin.system.measureTimeMillis

class RecipeLockerFriendList(private val user: DataSnapshot?) : Fragment() {
    private lateinit var binding: FragmentRecipeLockerFriendListBinding
    private lateinit var context: Context
    private lateinit var friendListAdapter: FriendAdapter
    private val db = Firebase.database("https://knu-capstone-f9f55-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private val userImageRef = Firebase.storage.getReference("user_image")
    private var friendList = mutableListOf<UserInfo>()

    private val defaultImageUri by lazy { Uri.parse("android.resource://${context.packageName}/${R.drawable.default_user_profile_image}")!! }

    /** * 유저의 친구 목록 참조하여 해당  유저의 친구 프로필 이미지, 이름, 아이디 가져와서 업데이트*/
    private suspend fun updateFriendList(){
        val mutex = Mutex()
        val friendIdList = user!!.child("friends").children.toList()
        val userRef = db.getReference("users")

//        val d = friendIdList.children.toList()
//        Log.d("LOG_CHECK", "RecipeLockerFriendList :: updateFriendList() -> friendList : ${d[0].value.toString()}")

        withContext(Dispatchers.IO){
            friendIdList.map{
                async {
                    val friendId = it.value.toString()
                    Log.d("LOG_CHECK", "RecipeLockerFriendList :: updateFriendList() -> friendId : $friendId")
                    val friendName = userRef.child(friendId).child("name").get().await().value.toString()
                    val friendProfilePath = userRef.child(friendId).child("profileImagePath").get().await().value.toString()
                    val profileUri = getFriendImageByPath(friendId, friendProfilePath)
                    mutex.withLock {
                        friendList.add(UserInfo(friendId, friendName, profileUri))
                    }
                }
            }.awaitAll()
        }
        Log.d("LOG_CHECK", "RecipeLockerFriendList :: updateFriendList() -> 완료 : $friendList")
    }

    /** *유저의 아이디와 프로필 이미지 경로를 받아 해당 이미지를 반납 */
    private suspend fun getFriendImageByPath(userId: String, imagePath: String): Uri{
        return if(imagePath.isNotEmpty()){
            userImageRef.child(userId).child("profile").child(imagePath).downloadUrl.await()
        }
        else {
            defaultImageUri
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRecipeLockerFriendListBinding.inflate(inflater, container, false)
        context = binding.root.context

        Log.d("LOG_CHECK", "RecipeLockerFriendList :: onCreateView() -> 22")
        if(user != null && user.child("friends").exists()){
            Log.d("LOG_CHECK", "RecipeLockerFriendList :: onCreateView() -> called")
            lifecycleScope.launch(Dispatchers.IO) {
                updateFriendList()
                withContext(Dispatchers.Main){
                    friendListAdapter.friendList = friendList
                    friendListAdapter.notifyDataSetChanged()
                }
            }
        }
        Log.d("LOG_CHECK", "RecipeLockerFriendList :: onCreateView() -> 22")

//        imageList = getFriendImageByPath()
        friendListAdapter = FriendAdapter()
        binding.recyclerviewFriendList.layoutManager = LinearLayoutManager(context)
        binding.recyclerviewFriendList.adapter = friendListAdapter


        return binding.root
    }
}