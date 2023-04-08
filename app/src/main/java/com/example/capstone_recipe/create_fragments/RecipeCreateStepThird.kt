package com.example.capstone_recipe.create_fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.capstone_recipe.R
import com.example.capstone_recipe.create_adapter.SelectMainImageAdapter
import com.example.capstone_recipe.data_class.RecipeBasicInfo
import com.example.capstone_recipe.data_class.RecipeStep
import com.example.capstone_recipe.data_class.SHARE
import com.example.capstone_recipe.databinding.FragmentRecipeCreateStepThirdBinding

class RecipeCreateStepThird(_recipeBasicInfo: RecipeBasicInfo, private val _createStepList: MutableList<RecipeStep>) : Fragment() {
    private lateinit var binding: FragmentRecipeCreateStepThirdBinding
    private lateinit var context: Context
    private lateinit var adapter:SelectMainImageAdapter
    private var recipeBasicInfo = _recipeBasicInfo

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRecipeCreateStepThirdBinding.inflate(inflater, container, false)
        context = binding.root.context
        adapter = SelectMainImageAdapter(recipeBasicInfo, _createStepList)

        binding.recyclerviewSelectMainImage.adapter = adapter
        binding.recyclerviewSelectMainImage.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        binding.rgLevelSelection.setOnCheckedChangeListener { _, checkedId ->
            recipeBasicInfo.shareTarget = when(checkedId){
                R.id.radioShareOnlyMe -> SHARE.ONLY_ME
                R.id.radioShareOnlyFriends -> SHARE.ONLY_FRIENDS
                R.id.radioShareAll -> SHARE.ALL
                else -> {
                    Log.e("ERROR", "ERROR::SHARE OPTION CHECK :: checkedId : $checkedId")
                    SHARE.ONLY_ME
                }
            }
        }

        binding.btn.setOnClickListener {
            Log.d("LOG_CHECK", "_recipeBasicInfo : $recipeBasicInfo \n_createStepList : $_createStepList")
        }

        return binding.root
    }
}