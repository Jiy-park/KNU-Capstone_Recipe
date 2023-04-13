package com.example.capstone_recipe.post_adapter

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.capstone_recipe.R
import com.example.capstone_recipe.data_class.RecipeStep
import com.example.capstone_recipe.databinding.ItemRecipeStepBinding
import com.google.firebase.storage.FirebaseStorage

class RecipeStepAdapter(private val recipeId: String, private val stepList:List<RecipeStep>):RecyclerView.Adapter<RecipeStepAdapter.Holder>() {
    private val storage = FirebaseStorage.getInstance()
    private lateinit var binding: ItemRecipeStepBinding
    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        binding = ItemRecipeStepBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        context = binding.root.context
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        Log.d("LOG_CHECK", "sdafasfawsf$position")
        holder.setting(stepList[position].explanation, stepList[position].imagePath)
    }

    override fun getItemCount() = stepList.size

    inner class Holder(val binding: ItemRecipeStepBinding):RecyclerView.ViewHolder(binding.root){
        @SuppressLint("SetTextI18n")
        fun setting(explanation:String, imagePath:String?){
            if(imagePath != null){
                storage
                    .getReference("recipe_image")
                    .child(recipeId)
                    .child("step")
                    .child(imagePath)
                    .downloadUrl
                    .addOnSuccessListener { uri->
                        Glide.with(binding.root.context)
                            .load(uri)
                            .into(binding.ivStepImage)
                    }
                    .addOnFailureListener { Log.d("LOG_CHECK", "MainActivity :: downloadImage() called :: 실패 :${it.message} 경로 $imagePath") }
            }
            else{
                val packageName = "com.example.capstone_recipe"
                val uri = Uri.parse("android.resource://$packageName/${R.drawable.ex_img}")
                binding.ivStepImage.setImageURI(uri)
            }
            val order = bindingAdapterPosition + 1
            binding.tvStepExplanation.text = "$order. $explanation"
        }
    }
}