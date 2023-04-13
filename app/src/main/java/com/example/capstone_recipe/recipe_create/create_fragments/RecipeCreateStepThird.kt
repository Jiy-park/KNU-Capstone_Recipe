package com.example.capstone_recipe.recipe_create.create_fragments

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
import com.example.capstone_recipe.UpdateValue
import com.example.capstone_recipe.recipe_create.create_adapter.SelectMainImageAdapter
import com.example.capstone_recipe.data_class.RecipeBasicInfo
import com.example.capstone_recipe.data_class.RecipeStep
import com.example.capstone_recipe.data_class.SHARE
import com.example.capstone_recipe.databinding.FragmentRecipeCreateStepThirdBinding

class RecipeCreateStepThird(private val stepImageList: MutableList<Uri?>) : Fragment() {
    private lateinit var binding: FragmentRecipeCreateStepThirdBinding
    private lateinit var context: Context
    private lateinit var adapter: SelectMainImageAdapter
    private lateinit var updateCallBack: UpdateValue

    override fun onAttach(context: Context) {
        super.onAttach(context)
        updateCallBack = context as UpdateValue
    }

    fun setUri(uri:Uri?){
        updateCallBack.updateMainImage(uri)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRecipeCreateStepThirdBinding.inflate(inflater, container, false)
        context = binding.root.context

        Toast.makeText(context, "대표 이미지를 선택해 주세요!", Toast.LENGTH_SHORT).show()

        adapter = SelectMainImageAdapter(this@RecipeCreateStepThird, stepImageList)

        binding.recyclerviewSelectMainImage.adapter = adapter
        binding.recyclerviewSelectMainImage.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        binding.rgLevelSelection.setOnCheckedChangeListener { _, checkedId ->
            updateCallBack.updateShareOption(
                when(checkedId){
                    R.id.radioShareOnlyMe -> SHARE.ONLY_ME
                    R.id.radioShareOnlyFriends -> SHARE.ONLY_FRIENDS
                    R.id.radioShareAll -> SHARE.ALL
                    else -> {
                        Log.e("ERROR", "ERROR::SHARE OPTION CHECK :: checkedId : $checkedId")
                        SHARE.ONLY_ME
                    }
                }
            )
        }

        return binding.root
    }
}