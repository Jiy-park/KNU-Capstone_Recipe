package com.example.capstone_recipe.post_viewer.post_adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.capstone_recipe.data_class.Ingredient
import com.example.capstone_recipe.databinding.ItemRecipeIngredientBinding

class RecipeIngredientAdapter():RecyclerView.Adapter<RecipeIngredientAdapter.Holder>() {
    private lateinit var binding: ItemRecipeIngredientBinding
    private lateinit var context: Context

    private var recipeIngredientList = listOf<Ingredient>()
    private var fromApi = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        binding = ItemRecipeIngredientBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        context = binding.root.context
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        if(recipeIngredientList.isNotEmpty()){
            holder.bind(recipeIngredientList[position])
        }
    }

    override fun getItemCount() = recipeIngredientList.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(newIngredientList: List<Ingredient>, newFromApi: Boolean){
        recipeIngredientList = newIngredientList
        fromApi = newFromApi
        notifyDataSetChanged()
    }

    inner class Holder(val binding: ItemRecipeIngredientBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(ingredient: Ingredient){
            binding.tvIngredientName.text = ingredient.name
            binding.tvIngredientAmount.text = ingredient.amount
        }
    }
}