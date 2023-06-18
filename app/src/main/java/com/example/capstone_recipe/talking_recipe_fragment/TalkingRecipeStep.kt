package com.example.capstone_recipe.talking_recipe_fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.capstone_recipe.Preference
import com.example.capstone_recipe.R
import com.example.capstone_recipe.databinding.FragmentTalkingRecipeStepBinding

class TalkingRecipeStep(
        _step: Int,
        private val stepExplanation: String,
        private val stepImageUri: Uri
    ): Fragment() {

    private lateinit var binding: FragmentTalkingRecipeStepBinding
    private lateinit var context: Context
    private var currentStep = _step // 1 단계 = 0


    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding =  FragmentTalkingRecipeStepBinding.inflate(inflater, container, false)
        context = binding.root.context

        setView()
        return binding.root
    }

    /** * 설명, 이미지에 맞게 뷰 설정*/
    @SuppressLint("SetTextI18n")
    private fun setView() = with(binding){
        tvRecipeStep.text = "${currentStep+1} 단계"
        tvRecipeStepExplanation.text = stepExplanation

        Glide.with(context)
            .load(stepImageUri)
            .error(R.drawable.default_recipe_main_image)
            .into(ivRecipeStepImage)
    }
}