package com.example.capstone_recipe.post_adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.capstone_recipe.databinding.ItemRecipeStepBinding

class RecipeStepAdapter:RecyclerView.Adapter<RecipeStepAdapter.Holder>() {
    private lateinit var binding: ItemRecipeStepBinding
    private lateinit var context: Context
    var stepList = mutableListOf<String>(
        "1 단계",
        "2 단계",
        "3 단계",
        "4 단계",
        "5 단계",
        "6 단계",
        "7 단계",
        "8 단계"
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        binding = ItemRecipeStepBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        context = binding.root.context
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        Log.d("LOG_CHECK", "sdafasfawsf$position")
        holder.setting(stepList[position])
    }

    override fun getItemCount() = stepList.size

    inner class Holder(val binding: ItemRecipeStepBinding):RecyclerView.ViewHolder(binding.root){
        @SuppressLint("SetTextI18n")
        fun setting(step:String){
            val order = bindingAdapterPosition + 1
            binding.tvStepExplanation.text = "$order. $step"
        }
    }
}