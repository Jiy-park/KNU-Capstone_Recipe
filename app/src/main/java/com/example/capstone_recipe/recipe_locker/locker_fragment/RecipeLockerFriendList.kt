package com.example.capstone_recipe.recipe_locker.locker_fragment

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.capstone_recipe.R
import com.example.capstone_recipe.data_class.User
import com.example.capstone_recipe.databinding.FragmentRecipeLockerFriendListBinding
import com.example.capstone_recipe.recipe_locker.locker_adpater.FriendAdapter
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class RecipeLockerFriendList(private val userInfo: User) : Fragment() {
    private lateinit var binding: FragmentRecipeLockerFriendListBinding
    private lateinit var context: Context
    private lateinit var friendListAdapter: FriendAdapter

    private val userRef = Firebase.database.getReference("users")
    private val friendList = userInfo.friends
    private var imageList = listOf<Uri>()

    private val defaultImageUri by lazy { Uri.parse("android.resource://${context.packageName}/${R.drawable.head_default_image}")!! }

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRecipeLockerFriendListBinding.inflate(inflater, container, false)
        context = binding.root.context
        imageList = getFriendImageByPath()

        friendListAdapter = FriendAdapter(context, friendList, imageList)

        binding.recyclerviewFriendList.layoutManager = LinearLayoutManager(context)
        binding.recyclerviewFriendList.adapter = friendListAdapter


        return binding.root
    }


}