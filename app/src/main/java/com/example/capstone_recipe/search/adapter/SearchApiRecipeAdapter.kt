package com.example.capstone_recipe.search.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.capstone_recipe.data_class.RecipeBasicInfo
import com.example.capstone_recipe.data_class.RecipeStep
import com.example.capstone_recipe.data_class.RecipeSupplement
import com.example.capstone_recipe.databinding.ItemLockerRecipeViewerBinding
import com.example.capstone_recipe.post_viewer.PostViewer
import com.example.capstone_recipe.search.RecipeInfoFromApi


class SearchApiRecipeAdapter: RecyclerView.Adapter<SearchApiRecipeAdapter.Holder>(){
    lateinit var binding: ItemLockerRecipeViewerBinding
    var recipeList = listOf<RecipeInfoFromApi>()
    var recipeBasicInfoList = listOf<RecipeBasicInfo>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        binding = ItemLockerRecipeViewerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        if(recipeBasicInfoList.isNotEmpty()){
            holder.recipeInfoFromApi = recipeList[position]
            holder.bind(recipeBasicInfoList[position])
            holder.setViewEvent(recipeBasicInfoList[position])
        }
    }

    override fun getItemCount() = recipeBasicInfoList.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(newRecipeList: List<RecipeInfoFromApi>, newRecipeBasicInfoList: List<RecipeBasicInfo>){
        recipeList = newRecipeList
        recipeBasicInfoList = newRecipeBasicInfoList
        notifyDataSetChanged()
    }

    inner class Holder(val binding: ItemLockerRecipeViewerBinding): RecyclerView.ViewHolder(binding.root){
        lateinit var recipeInfoFromApi: RecipeInfoFromApi

        @SuppressLint("SetTextI18n")
        fun bind(recipeBasicInfo: RecipeBasicInfo){
            Glide.with(binding.root.context)
                .load(recipeBasicInfo.mainImagePath.toUri())
                .into(binding.layerViewer.ivRecipeMainImage)

            binding.layerViewer.tvRecipeTitle.text = recipeBasicInfo.title
            binding.layerViewer.tvRecipeCreator.text = "Toxi @API"
            binding.layerViewer.tvRecipeIntro.text = recipeBasicInfo.intro
            binding.layerViewer.recipeTime.text = "-"
            binding.layerViewer.recipeLevel.text = recipeBasicInfo.level.toKor
            binding.layerViewer.recipeLike.text = "-"
        }

        fun setViewEvent(recipeBasicInfo: RecipeBasicInfo){
            binding.root.setOnClickListener {
                val recipeSupplement = buildApiRecipeSupplement()
                val recipeStepList = buildApiRecipeStepList()
                val recipeIngredients = buildApiRecipeIngredientList()
                val intent = Intent(binding.root.context, PostViewer::class.java)
                intent.putExtra("from", "api")
                intent.putExtra("recipeBasicInfo", recipeBasicInfo)
                intent.putExtra("recipeSupplement", recipeSupplement)
                intent.putExtra("recipeStepList", ArrayList(recipeStepList))
                intent.putExtra("recipeIngredientList", recipeIngredients)
                binding.root.context.startActivity(intent)
            }
        }

        private fun buildApiRecipeSupplement() = RecipeSupplement(
            calorie =  recipeInfoFromApi.calorie + " kcal",
            fat = recipeInfoFromApi.fat + " g",
            carbohydrate = recipeInfoFromApi.carbohydrate + " g",
            protein = recipeInfoFromApi.protein + " g",
            sodium = recipeInfoFromApi.sodium + " mg"
        )

        private fun buildApiRecipeStepList(): List<RecipeStep>{
            val stepList = mutableListOf<RecipeStep>()
            for(i in 1..20){
                val recipeStep = getStepFromApi(recipeInfoFromApi, i)
                if(recipeStep.explanation.isEmpty() && recipeStep.imagePath.isEmpty()) { break }
                stepList.add(recipeStep)
            }
            return stepList
        }

        private fun buildApiRecipeIngredientList() = recipeInfoFromApi.ingredients.replace(",", "\n").trimIndent()

        private fun getStepFromApi(recipeInfoFromApi: RecipeInfoFromApi, index: Int): RecipeStep {
            return when(index){
                1 ->  { RecipeStep(recipeInfoFromApi.MANUAL01, recipeInfoFromApi.MANUAL_IMG01)  }
                2 ->  { RecipeStep(recipeInfoFromApi.MANUAL02, recipeInfoFromApi.MANUAL_IMG02)  }
                3 ->  { RecipeStep(recipeInfoFromApi.MANUAL03, recipeInfoFromApi.MANUAL_IMG03)  }
                4 ->  { RecipeStep(recipeInfoFromApi.MANUAL04, recipeInfoFromApi.MANUAL_IMG04)  }
                5 ->  { RecipeStep(recipeInfoFromApi.MANUAL05, recipeInfoFromApi.MANUAL_IMG05)  }
                6 ->  { RecipeStep(recipeInfoFromApi.MANUAL06, recipeInfoFromApi.MANUAL_IMG06)  }
                7 ->  { RecipeStep(recipeInfoFromApi.MANUAL07, recipeInfoFromApi.MANUAL_IMG07)  }
                8 ->  { RecipeStep(recipeInfoFromApi.MANUAL08, recipeInfoFromApi.MANUAL_IMG08)  }
                9 ->  { RecipeStep(recipeInfoFromApi.MANUAL09, recipeInfoFromApi.MANUAL_IMG09)  }
                10 -> { RecipeStep(recipeInfoFromApi.MANUAL10, recipeInfoFromApi.MANUAL_IMG10)  }
                11 -> { RecipeStep(recipeInfoFromApi.MANUAL11, recipeInfoFromApi.MANUAL_IMG11)  }
                12 -> { RecipeStep(recipeInfoFromApi.MANUAL12, recipeInfoFromApi.MANUAL_IMG12)  }
                13 -> { RecipeStep(recipeInfoFromApi.MANUAL13, recipeInfoFromApi.MANUAL_IMG13)  }
                14 -> { RecipeStep(recipeInfoFromApi.MANUAL14, recipeInfoFromApi.MANUAL_IMG14)  }
                15 -> { RecipeStep(recipeInfoFromApi.MANUAL15, recipeInfoFromApi.MANUAL_IMG15)  }
                16 -> { RecipeStep(recipeInfoFromApi.MANUAL16, recipeInfoFromApi.MANUAL_IMG16)  }
                17 -> { RecipeStep(recipeInfoFromApi.MANUAL17, recipeInfoFromApi.MANUAL_IMG17)  }
                18 -> { RecipeStep(recipeInfoFromApi.MANUAL18, recipeInfoFromApi.MANUAL_IMG18)  }
                19 -> { RecipeStep(recipeInfoFromApi.MANUAL19, recipeInfoFromApi.MANUAL_IMG19)  }
                20 -> { RecipeStep(recipeInfoFromApi.MANUAL20, recipeInfoFromApi.MANUAL_IMG20)  }
                else -> { RecipeStep() }
            }
        }
    }
}