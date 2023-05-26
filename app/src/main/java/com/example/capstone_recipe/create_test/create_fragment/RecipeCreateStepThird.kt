package com.example.capstone_recipe.create_test.create_fragment

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.capstone_recipe.R
import com.example.capstone_recipe.create_test.RecipeSelectMainImageAdapter
import com.example.capstone_recipe.data_class.LEVEL
import com.example.capstone_recipe.data_class.SHARE
import com.example.capstone_recipe.databinding.FragmentRecipeCreateStepThirdBinding
import com.example.capstone_recipe.recipe_create.create_adapter.SelectMainImageAdapter

class RecipeCreateStepThird(
        _recipeStepImageUriList: List<Uri>,
        _recipeMainImageUri: Uri,
        _recipeMainImageIndex: Int,
        _recipeShareOption: SHARE
    ) : Fragment() {
    private lateinit var binding: FragmentRecipeCreateStepThirdBinding
    private lateinit var context: Context
    private lateinit var recipeSelectMainImageAdapter: RecipeSelectMainImageAdapter

    private var recipeStepImageUriList = _recipeStepImageUriList
    private var recipeMainImageUri = _recipeMainImageUri
    private var recipeMainImageIndex = _recipeMainImageIndex
    private var shareOption = _recipeShareOption

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRecipeCreateStepThirdBinding.inflate(inflater, container, false)
        context = binding.root.context

        Toast.makeText(context, "대표 이미지를 선택해 주세요!", Toast.LENGTH_SHORT).show()

        recipeSelectMainImageAdapter = RecipeSelectMainImageAdapter()
        recipeSelectMainImageAdapter.recipeStepImageUriList = recipeStepImageUriList
        recipeSelectMainImageAdapter.checkImageUri = recipeMainImageUri
        recipeSelectMainImageAdapter.checkedImageIndex = recipeMainImageIndex
        binding.recyclerviewSelectMainImage.adapter = recipeSelectMainImageAdapter
        binding.recyclerviewSelectMainImage.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        setShareOption()

        binding.rgLevelSelection.setOnCheckedChangeListener { _, checkedId ->
            shareOption =
                when(checkedId){
                    R.id.radioShareOnlyMe -> SHARE.ONLY_ME
                    R.id.radioShareOnlyFriends -> SHARE.ONLY_FRIENDS
                    R.id.radioShareAll -> SHARE.ALL
                    else -> {
                        Log.e("ERROR", "ERROR::SHARE OPTION CHECK :: checkedId : $checkedId")
                        SHARE.ONLY_ME
                    }
                }
        }
        return binding.root
    }

    private fun setShareOption(){
        when(shareOption){
            SHARE.ONLY_ME -> binding.radioShareOnlyMe.isChecked = true
            SHARE.ONLY_FRIENDS -> binding.radioShareOnlyFriends.isChecked = true
            SHARE.ALL -> binding.radioShareAll.isChecked = true

        }
    }

    fun buildShareOption() = shareOption
    fun buildRecipeMainImageUri() = recipeSelectMainImageAdapter.getRecipeMainImageUri()
    fun buildRecipeMainImageIndex() = recipeSelectMainImageAdapter.getRecipeMainImageIndex()
}