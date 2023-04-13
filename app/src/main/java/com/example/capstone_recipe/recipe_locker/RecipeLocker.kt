package com.example.capstone_recipe.recipe_locker

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.capstone_recipe.recipe_locker.locker_adpater.RecipeHolderAdapter
import com.example.capstone_recipe.databinding.ActivityRecipeLockerBinding

class RecipeLocker : AppCompatActivity() {
    private val binding by lazy { ActivityRecipeLockerBinding.inflate(layoutInflater) }
    private lateinit var context:Context
    private lateinit var adpater: RecipeHolderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        adpater = RecipeHolderAdapter()
        context = binding.root.context

        binding.layerTopPanel.btnBack.setOnClickListener { finish() }
        binding.recyclerviewRecipeHolder.adapter = adpater
        binding.recyclerviewRecipeHolder.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL,  false)
    }
}