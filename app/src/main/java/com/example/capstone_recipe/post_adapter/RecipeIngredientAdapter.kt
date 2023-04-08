package com.example.capstone_recipe.post_adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.capstone_recipe.databinding.ItemRecipeIngredientBinding

class RecipeIngredientAdapter:RecyclerView.Adapter<RecipeIngredientAdapter.Holder>() {
    private lateinit var binding: ItemRecipeIngredientBinding
    private lateinit var context: Context
    var ingredientList = mutableListOf<Ingredient>(
        Ingredient("닭", "1.2kg"),
        Ingredient("소금", "2ts"),
        Ingredient("설탕", "2ts"),
        Ingredient("생강 가루", "1ts"),
        Ingredient("마늘 가루", "2ts"),
        Ingredient("후추", "2ts"),
        Ingredient("우유", "500ml"),
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        binding = ItemRecipeIngredientBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        context = binding.root.context
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.setting(ingredientList[position])
    }

    override fun getItemCount() = ingredientList.size

    inner class Holder(val binding: ItemRecipeIngredientBinding):RecyclerView.ViewHolder(binding.root){
        fun setting(ingredient: Ingredient){
            binding.tvIngredientName.text = ingredient.name
            binding.tvIngredientAmount.text = ingredient.amount
        }
    }
}
data class Ingredient(
    var name: String = "",
    var amount: String = ""
)