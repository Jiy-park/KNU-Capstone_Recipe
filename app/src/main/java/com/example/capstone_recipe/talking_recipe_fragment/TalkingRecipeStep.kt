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
import com.example.capstone_recipe.databinding.FragmentTalkingRecipeStepBinding

class TalkingRecipeStep(
        private val tts: TextToSpeech,
        step: Int,
        val stepExplanationList: List<String>,
        private val stepImageUriList: List<Uri>
    ): Fragment() {

    private lateinit var binding: FragmentTalkingRecipeStepBinding
    private lateinit var context: Context
    private lateinit var pref: Preference
    private var isToastShown = false
    var currentStep = step // 1 단계 = 0


    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding =  FragmentTalkingRecipeStepBinding.inflate(inflater, container, false)
        context = binding.root.context
        pref = Preference(context)

        if(stepExplanationList.isNotEmpty()) {
            updateView(currentStep)
            if(pref.getUseTTS()) { startTTS(currentStep) }
            else{
                if(currentStep == 0 && !isToastShown){
                    Toast.makeText(context, "레시피를 읽어주길 원하신다면,\n환경설정에서 '읽어주기'를 허용해 주세요.", Toast.LENGTH_SHORT).show()
                    isToastShown = true
                }
            }
        }
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    private fun updateView(step: Int){
        binding.tvRecipeStep.text = "${step+1}단계"
        binding.tvRecipeStepExplanation.text = stepExplanationList[step]
        Glide.with(context)
            .load(stepImageUriList[step])
            .into(binding.ivRecipeStepImage)
    }

    private fun startTTS(step: Int){
        val text = stepExplanationList[step]
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }


}