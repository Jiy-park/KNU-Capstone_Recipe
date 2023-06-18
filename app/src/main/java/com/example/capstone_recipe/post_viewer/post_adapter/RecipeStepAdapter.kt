package com.example.capstone_recipe.post_viewer.post_adapter

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.capstone_recipe.R
import com.example.capstone_recipe.data_class.RecipeStep
import com.example.capstone_recipe.databinding.ItemRecipeStepBinding

class RecipeStepAdapter():RecyclerView.Adapter<RecipeStepAdapter.Holder>() {
    private lateinit var binding: ItemRecipeStepBinding
    private lateinit var context: Context

    private var recipeStepList = listOf<RecipeStep>()
    private var recipeStepImageList = listOf<Uri>()
    private var fromApi = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        binding = ItemRecipeStepBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        context = binding.root.context
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        if(recipeStepList.isNotEmpty()){
            if(fromApi){ holder.bind(recipeStepList[position].explanation, recipeStepList[position].imagePath.toUri()) }
            else { holder.bind(recipeStepList[position].explanation, recipeStepImageList[position]) }
        }
    }

    override fun getItemCount() = recipeStepList.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(newStepList: List<RecipeStep>, newImageList: List<Uri>, newFromApi: Boolean){
        recipeStepList = newStepList
        recipeStepImageList = newImageList
        fromApi = newFromApi
        notifyDataSetChanged()
    }

    inner class Holder(val binding: ItemRecipeStepBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(explanation: String, imageUri: Uri){
            binding.tvStepExplanation.text =
                if(fromApi) { explanation }
                else { "${bindingAdapterPosition}. $explanation" }
            Glide.with(context)
                .load(imageUri)
                .error(R.drawable.default_recipe_main_image)
                .into(binding.ivStepImage)
        }
    }
}