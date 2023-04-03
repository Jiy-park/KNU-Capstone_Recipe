package com.example.capstone_recipe.create_fragments

import android.Manifest
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.example.capstone_recipe.MainActivity
import com.example.capstone_recipe.create_adapter.ExplanationAdapter
import com.example.capstone_recipe.create_adapter.ItemTouchHelperCallback
import com.example.capstone_recipe.databinding.FragmentRecipeCreateStepSecondBinding
import java.util.*

class RecipeCreateStepSecond : Fragment() {
    private lateinit var binding: FragmentRecipeCreateStepSecondBinding
    private lateinit var context: Context
    private lateinit var adapter: ExplanationAdapter
    private lateinit var itemTouchHelper : ItemTouchHelper

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRecipeCreateStepSecondBinding.inflate(inflater, container, false)
        context = binding.root.context

        adapter = ExplanationAdapter()
        itemTouchHelper = ItemTouchHelper(ItemTouchHelperCallback(adapter))
        itemTouchHelper.attachToRecyclerView(binding.recyclerviewCreateExplanation)

        binding.recyclerviewCreateExplanation.adapter = adapter
        binding.recyclerviewCreateExplanation.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        return binding.root
    }
}

