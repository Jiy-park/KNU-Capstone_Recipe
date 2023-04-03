package com.example.capstone_recipe.create_fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.capstone_recipe.databinding.FragmentRecipeCreateStepThirdBinding

class RecipeCreateStepThird : Fragment() {
    private lateinit var binding: FragmentRecipeCreateStepThirdBinding
    private lateinit var context: Context

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRecipeCreateStepThirdBinding.inflate(inflater, container, false)
        context = binding.root.context
        return binding.root
    }
}