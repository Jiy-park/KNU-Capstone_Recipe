package com.example.capstone_recipe.recipe_locker.locker_adpater

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.capstone_recipe.databinding.ItemRecipeViewerBinding

class RecipeViewerAdapter:RecyclerView.Adapter<RecipeViewerAdapter.Holder>() {

    private var recipeList = mutableListOf<String>(
        "0", "1", "2",
        "3", "4", "1",
        "0", "1", "2",
        "3", "4", "1",
        "0", "1", "2",
        "3", "4"
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemRecipeViewerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        Log.d("LOG_CHECK", "RecipeViewerAdapter :: main position $position")
        if(position % 3 == 0){
            Log.d("LOG_CHECK", "RecipeViewerAdapter ::position $position")
            val triple = getElementFromList(position, recipeList)
//            holder.settingRecipe(triple)
        }
    }

    override fun getItemCount() = recipeList.size

    private fun getElementFromList(startIndex:Int, list:MutableList<String>):Triple<String,String?,String?>{
        var first = ""
        var second:String? = null
        var third:String? = null
        for(i in startIndex until startIndex + 3){
            if(i >= list.size) { break }
            when{
                i % 3 == 0 -> first = list[i]
                i % 3 == 1 -> second = list[i]
                i % 3 == 2 -> third = list[i]
            }
        }
        Log.d("LOG_CHECK", "RecipeViewerAdapter :: first : $first second : $second third : $third \n\n")
        return Triple(first, second, third)
    }

    class Holder(private val binding:ItemRecipeViewerBinding):RecyclerView.ViewHolder(binding.root){
//        fun settingRecipe(tripleRecipe:Triple<String,String?,String?>){
//            // 첫번째 레시피 설정
//            binding.ivRecipeThumbnail11.tvRecipeDefaultText.text = tripleRecipe.first
//            // 두번째 레시피 설정
//            if(tripleRecipe.second == null){ binding.ivRecipeThumbnail22.prentLayout.visibility = View.GONE }
//            else{ binding.ivRecipeThumbnail22.tvRecipeDefaultText.text = tripleRecipe.second }
//            // 세번째 레시피 설정
//            if(tripleRecipe.second == null){ binding.ivRecipeThumbnail33.prentLayout.visibility = View.GONE }
//            else{ binding.ivRecipeThumbnail33.tvRecipeDefaultText.text = tripleRecipe.third }
//        }
    }
}