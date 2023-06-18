package com.example.capstone_recipe.search.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.capstone_recipe.R
import com.example.capstone_recipe.post_viewer.PostViewer
import com.example.capstone_recipe.data_class.RecipeBasicInfo
import com.example.capstone_recipe.databinding.ItemLockerRecipeViewerBinding

class SearchRecipeAdapter: RecyclerView.Adapter<SearchRecipeAdapter.Holder>() {
    private lateinit var binding: ItemLockerRecipeViewerBinding
    private lateinit var context: Context
    var recipeList = listOf<RecipeBasicInfo>()
    var creatorList = listOf<String>()
    var mainImageUriList = listOf<Uri>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        binding = ItemLockerRecipeViewerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        context = binding.root.context
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(recipeList[position], creatorList[position], mainImageUriList[position])
    }

    override fun getItemCount() = recipeList.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapterList(newRecipeList: List<RecipeBasicInfo>, newCreatorList: List<String>, newMainImageUriList: List<Uri>){
        recipeList = newRecipeList
        creatorList = newCreatorList
        mainImageUriList = newMainImageUriList
        notifyDataSetChanged()
    }

    inner class Holder(val binding: ItemLockerRecipeViewerBinding): RecyclerView.ViewHolder(binding.root){
        @SuppressLint("SetTextI18n")
        fun bind(recipe: RecipeBasicInfo, creator: String, mainImageUri: Uri){
            binding.layerViewer.tvRecipeTitle.text = recipe.title
            binding.layerViewer.tvRecipeIntro.text = recipe.intro
            binding.layerViewer.tvRecipeCreator.text = creator
            binding.layerViewer.recipeTime.text = recipe.time + "분"
            binding.layerViewer.recipeLevel.text = recipe.level.toKor
            binding.layerViewer.recipeLike.text = recipe.score.toString()
            Glide.with(context)
                .let {
                    if(mainImageUri == Uri.EMPTY){ it.load(R.drawable.default_recipe_main_image) }
                    else { it.load(mainImageUri) }
                }
                .into(binding.layerViewer.ivRecipeMainImage)
            setClickEvent(recipe.id)
        }
        private fun setClickEvent(recipeId: String){
            binding.root.setOnClickListener {
                val intent = Intent(context, PostViewer::class.java)
                intent.putExtra("recipeId", recipeId)
                context.startActivity(intent)
            }
        }
    }
}