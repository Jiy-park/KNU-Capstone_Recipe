package com.example.capstone_recipe.recipe_locker.locker_adpater

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.capstone_recipe.data_class.FriendInfo
import com.example.capstone_recipe.databinding.ItemFriendViewerBinding
import com.example.capstone_recipe.recipe_locker.RecipeLocker

class FriendAdapter(
        private val context: Context,
        private val friendList: List<FriendInfo>,
        private val imageList: List<Uri>
    ):RecyclerView.Adapter<FriendAdapter.Holder>() {

    private lateinit var binding: ItemFriendViewerBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        binding = ItemFriendViewerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(friendList[position], imageList[position])
    }

    override fun getItemCount() = friendList.size

    inner class Holder(val binding:ItemFriendViewerBinding): RecyclerView.ViewHolder(binding.root){
        @SuppressLint("SetTextI18n")
        fun bind(friend: FriendInfo, image: Uri){
            binding.tvFriendNameWithId.text = "${friend.name} @${friend.id}"
            binding.ivFriendProfileImage.setImageURI(image)

            itemView.setOnClickListener {
                val intent = Intent(context, RecipeLocker::class.java)
                intent.putExtra("userId", friend.id)
                context.startActivity(intent)
            }
        }
    }
}