package com.example.capstone_recipe.talking_recipe_fragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.capstone_recipe.databinding.FragmentTalkingRecipeStepBinding

class TalkingRecipeStep(step: Int) : Fragment() {
    private lateinit var binding: FragmentTalkingRecipeStepBinding
    private lateinit var context: Context
    var currentStep = step // 1 단계 = 0
    var stepList = mutableListOf<String>( // 나중에 이미지도 포함해야 함
        "1 단계",
        "2 단계",
        "3 단계",
        "4 단계",
        "5 단계",
        "6 단계",
        "7 단계",
        "8 단계"
    )

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding =  FragmentTalkingRecipeStepBinding.inflate(inflater, container, false)
        context = binding.root.context
        binding.tvRecipeStep.text = "${currentStep+1}단계"
        binding.tvRecipeStepExplanation.text = stepList[currentStep]
//        binding.ivRecipeStepImage.setImageURI("") 나중에 추가해야함

        return binding.root
    }
}