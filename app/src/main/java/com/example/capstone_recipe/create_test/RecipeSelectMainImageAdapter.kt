package com.example.capstone_recipe.create_test

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.capstone_recipe.R
import com.example.capstone_recipe.databinding.ItemSelectMainImageBinding

class RecipeSelectMainImageAdapter: RecyclerView.Adapter<RecipeSelectMainImageAdapter.Holder>() {
    private lateinit var binding: ItemSelectMainImageBinding
    private lateinit var context: Context

    var recipeStepImageUriList = listOf<Uri>()
    var checkImageUri: Uri = Uri.EMPTY
    var checkedImageIndex = -1
    private var checkedView: View? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        binding = ItemSelectMainImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        context = binding.root.context

        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val imageUri = recipeStepImageUriList[position]
        holder.bind(imageUri)
        if((checkedImageIndex == -1 || checkedImageIndex >= recipeStepImageUriList.size) && position == 0){
            checkedView = holder.itemView
            checkedImageIndex = 0
            holder.itemView.findViewById<FrameLayout>(R.id.frameChecked).visibility = View.VISIBLE
        }
        else if(checkedImageIndex == position){
            checkedView = holder.itemView
            holder.itemView.findViewById<FrameLayout>(R.id.frameChecked).visibility = View.VISIBLE
        }
    }

    override fun getItemCount() = recipeStepImageUriList.size

    fun getRecipeMainImageUri() = checkImageUri
    fun getRecipeMainImageIndex() = checkedImageIndex

    inner class Holder(val binding: ItemSelectMainImageBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(imageUri: Uri){
            Log.d("LOG_CHECK", "Holder :: bind() -> imageUri : $imageUri")
            Glide.with(context)
                .load(imageUri)
                .centerCrop()
                .into(binding.ivMainImage)

            setViewEvent(imageUri)
        }

        private fun setViewEvent(imageUri: Uri){
            binding.ivMainImage.setOnClickListener {
                checkedView?.let { it.findViewById<FrameLayout>(R.id.frameChecked).visibility = View.GONE }

                checkedView = itemView
                checkedView!!.findViewById<FrameLayout>(R.id.frameChecked).visibility = View.VISIBLE
                checkImageUri = imageUri
                checkedImageIndex = bindingAdapterPosition
            }
        }
    }
}