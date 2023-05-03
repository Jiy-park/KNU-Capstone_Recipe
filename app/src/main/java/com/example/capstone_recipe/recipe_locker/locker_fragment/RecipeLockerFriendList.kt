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
import com.example.capstone_recipe.data_class.FriendInfo
import com.example.capstone_recipe.databinding.FragmentRecipeLockerFriendListBinding
import com.example.capstone_recipe.recipe_locker.locker_adpater.FriendAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import kotlin.system.measureTimeMillis

class RecipeLockerFriendList(private val user: DataSnapshot?) : Fragment() {
    private lateinit var binding: FragmentRecipeLockerFriendListBinding
    private lateinit var context: Context
    private lateinit var friendListAdapter: FriendAdapter

    private var friendList = listOf<FriendInfo>()
    private var imageList = listOf<Uri>()

    private val defaultImageUri by lazy { Uri.parse("android.resource://${context.packageName}/${R.drawable.default_user_profile_image}")!! }

    /** * 유저의 친구 목록 참조하여 해당  유저의 친구 프로필 이미지, 이름, 아이디 가져와서 업데이트*/
    private suspend fun updateFriendList(){
        Log.d("LOG_CHECK", "RecipeLockerFriendList :: updateFriendList() -> user : $user")
        val friendIdList = user!!.child("friends")
        val size = friendIdList.childrenCount.toInt()
        val newFriendList = MutableList(size) { FriendInfo() } // 업로드 리스트
        val newImageList = MutableList<Uri>(size) { Uri.EMPTY }         // 이미지 리스트

        val time = measureTimeMillis{
            withContext(Dispatchers.IO){
                friendIdList.children.mapIndexed{ index, item ->
                    async {
                        val friend = item.getValue(FriendInfo::class.java)!!
                        val friendId = friend.id
                        val friendProfilePath = friend.profileImagePath
                        newFriendList[index] = friend
                        newImageList[index] = getProfileImage(friendId, friendProfilePath)
                    }
                }.awaitAll()
                friendList = newFriendList
                imageList = newImageList
            }
        }
        Log.d("LOG_CHECK", "RecipeLockerFriendList :: updateFriendList() -> time : $time")
    }

    /** * 유저 아이디에 해당하는 유저의 프로필 이미지 uri 반납*/
    private suspend fun getProfileImage(userId: String, profilePath: String): Uri{
        Log.d("LOG_CHECK", "222222222222222")
        val defaultImageUri = Uri.parse("android.resource://${context.packageName}/${R.drawable.default_user_profile_image}")!!
        val uri =
            if(profilePath == ""){ defaultImageUri }
            else{
                Firebase.storage.getReference("user_image")
                    .child(userId)
                    .child("profile")
                    .child(profilePath)
                    .downloadUrl
                    .await()
            }
        Log.d("LOG_CHECK", "1111111111111111111111")
        return uri
    }

    private fun getFriendImageByPath(): MutableList<Uri>{
        val userImageRef= Firebase.storage.getReference("user_image")
        val imageList = mutableListOf<Uri>()
        if(friendList.isEmpty()) { return mutableListOf() }
        for(friend in friendList){
            if(friend.profileImagePath.isEmpty()){ imageList.add(defaultImageUri) }
            else{
                userImageRef
                    .child(friend.id)
                    .child(friend.profileImagePath)
                    .downloadUrl
                    .addOnSuccessListener { uri ->
                        imageList.add(uri)
                    }
                    .addOnFailureListener {
                        Log.e("LOG_CHECK", "RecipeLockerFriendList :: getFriendImageByPath() -> $it")
                    }
            }
        }
        return imageList
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
                    friendListAdapter.imageList = imageList
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
//
//class RecipeLockerFriendList(private val user: DataSnapshot) : Fragment() {
//    private lateinit var binding: FragmentRecipeLockerFriendListBinding
//    private lateinit var context: Context
//    private lateinit var friendListAdapter: FriendAdapter
//
//    private val userRef = Firebase.database.getReference("users")
//    private val friendList = user.child("friends")
//    private var imageList = listOf<Uri>()
//
//    private val defaultImageUri by lazy { Uri.parse("android.resource://${context.packageName}/${R.drawable.head_default_image}")!! }
//
//    private suspend fun getUserFriendProfileList(user: DataSnapshot): MutableList<Uri>{
//        val friendRef = user.child("friend")
//        val imageList = MutableList(friendRef.childrenCount.toInt()) { Uri() }
//    }
//
//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        binding = FragmentRecipeLockerFriendListBinding.inflate(inflater, container, false)
//        context = binding.root.context
//        imageList = getFriendImageByPath()
//
//        friendListAdapter = FriendAdapter(context, friendList, imageList)
//
//        binding.recyclerviewFriendList.layoutManager = LinearLayoutManager(context)
//        binding.recyclerviewFriendList.adapter = friendListAdapter
//
//
//        return binding.root
//    }
//
//
//}