package com.example.capstone_recipe

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.capstone_recipe.databinding.ActivityPostViewerBinding
import com.example.capstone_recipe.post_adapter.RecipeIngredientAdapter
import com.example.capstone_recipe.post_adapter.RecipeStepAdapter

class PostViewer : AppCompatActivity() {
    private val binding by lazy { ActivityPostViewerBinding.inflate(layoutInflater) }
    private lateinit var context: Context
    private lateinit var recipeIngredientAdapter: RecipeIngredientAdapter
    private lateinit var recipeStepAdapter: RecipeStepAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        context = binding.root.context
        recipeIngredientAdapter = RecipeIngredientAdapter()
        recipeStepAdapter = RecipeStepAdapter()

        binding.layerTopPanel.btnBack.setOnClickListener {
            finish()
        }

        binding.recyclerviewRecipeIngredients.adapter = recipeIngredientAdapter
        binding.recyclerviewRecipeIngredients.layoutManager = GridLayoutManager(context, 2)

        binding.recyclerviewRecipeStep.adapter = recipeStepAdapter
        binding.recyclerviewRecipeStep.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        binding.btnStartTalkingRecipe.setOnClickListener {
            val intent = Intent(context, TalkingRecipe::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

    }
}