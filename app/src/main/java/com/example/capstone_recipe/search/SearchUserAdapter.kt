package com.example.capstone_recipe.search

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.capstone_recipe.data_class.UserInfo
import com.example.capstone_recipe.databinding.ItemUserSimpleViewerBinding
import com.example.capstone_recipe.recipe_locker.RecipeLocker

class SearchUserAdapter:RecyclerView.Adapter<SearchUserAdapter.Holder>() {
    private lateinit var binding: ItemUserSimpleViewerBinding
    private lateinit var context: Context
    var userIdList = listOf<UserInfo>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        binding = ItemUserSimpleViewerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        context = binding.root.context
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        Log.d("LOG_CHECK", "SearchUserAdapter :: onBindViewHolder() -> position : $position")
        holder.bind(userIdList[position])
    }

    override fun getItemCount() = userIdList.size

    fun updateUserList(newList: List<UserInfo>){
        userIdList = newList
        notifyDataSetChanged()
        Log.d("LOG_CHECK", "SearchUserAdapter :: updateUserList() -> userIdList : $userIdList")
    }

    inner class Holder(val binding: ItemUserSimpleViewerBinding):RecyclerView.ViewHolder(binding.root){
        @SuppressLint("SetTextI18n")
        fun bind(userInfo: UserInfo){
            Log.d("LOG_CHECK", "Holder :: bind() -> userInfo : $userInfo")
            binding.tvFriendNameWithId.text = "${userInfo.name} @${userInfo.id}"
            Glide.with(context)
                .load(userInfo.profileImageUri)
                .circleCrop()
                .into(binding.ivFriendProfileImage)

            binding.root.setOnClickListener {
                val intent = Intent(context, RecipeLocker::class.java)
                intent.putExtra("lockerOwnerId", userInfo.id)
                context.startActivity(intent)
            }
        }
    }
}