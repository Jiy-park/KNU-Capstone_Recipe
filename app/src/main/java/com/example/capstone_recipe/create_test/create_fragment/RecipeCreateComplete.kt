package com.example.capstone_recipe.create_test.create_fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.capstone_recipe.R
import com.example.capstone_recipe.databinding.FragmentRecipeCreateCompleteBinding

class RecipeCreateComplete : Fragment() {
    private lateinit var binding: FragmentRecipeCreateCompleteBinding
    private lateinit var context: Context
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRecipeCreateCompleteBinding.inflate(inflater, container, false)
        context = binding.root.context

        return binding.root
    }
}