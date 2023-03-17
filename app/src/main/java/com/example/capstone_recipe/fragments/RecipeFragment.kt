package com.example.capstone_recipe.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.capstone_recipe.databinding.FragmentRecipeBinding
class RecipeFragment : Fragment() {
    private lateinit var binding: FragmentRecipeBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRecipeBinding.inflate(inflater, container, false)
        return binding.root
    }
}