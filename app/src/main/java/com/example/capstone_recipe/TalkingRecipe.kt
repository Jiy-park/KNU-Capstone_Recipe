package com.example.capstone_recipe

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.capstone_recipe.create_fragments.RecipeCreateStepFirst
import com.example.capstone_recipe.databinding.ActivityTalkingRecipeBinding
import com.example.capstone_recipe.talking_recipe_fragment.TalkingRecipeStep

class TalkingRecipe : AppCompatActivity() {
    private val binding by lazy { ActivityTalkingRecipeBinding.inflate(layoutInflater) }
    private lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        context = binding.root.context

        supportFragmentManager
            .beginTransaction()
            .add(R.id.mainFrame, TalkingRecipeStep(0))
            .addToBackStack("step : 0")
            .commit()

        binding.layerTopPanel.btnBack.setOnClickListener {
            finish()
        }

        binding.btnNext.setOnClickListener {
            moveRecipeStep(STEP.NEXT)
        }
        binding.btnPrev.setOnClickListener {
            moveRecipeStep(STEP.PREV)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return if (supportFragmentManager.backStackEntryCount > 1) {
                supportFragmentManager.popBackStack()
                Log.d("LOG_CHECK", "${supportFragmentManager.backStackEntryCount}")
                if(supportFragmentManager.backStackEntryCount == 2){
                    binding.btnPrev.visibility = View.GONE
                }
                true
            }
            else{
                finish()
                true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun refreshFragment(step:Int, isNext:Boolean = true){
        if(isNext){
            supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(
                    R.anim.animation_enter_from_right,
                    R.anim.animation_exit_to_left,
                    R.anim.animation_enter_from_left,
                    R.anim.animation_exit_to_right
                )
                .add(R.id.mainFrame, TalkingRecipeStep(step))
                .addToBackStack("step : $step")
                .commit()
        }
        else{
            supportFragmentManager
                .popBackStack()
        }

    }

    private fun moveRecipeStep(step:STEP){
        val fragment = supportFragmentManager.findFragmentById(R.id.mainFrame) as? TalkingRecipeStep
        if(fragment != null){
            var currentStep = fragment.currentStep
            when(step){
                STEP.NEXT -> {
                    currentStep++
                    if(currentStep == fragment.stepList.size-1){
                        binding.btnNext.visibility = View.GONE
                    }
                    binding.btnPrev.visibility = View.VISIBLE
                    refreshFragment(currentStep)
                }
                STEP.PREV -> {
                    currentStep--
                    if(currentStep == 0){
                        binding.btnPrev.visibility = View.GONE
                    }
                    binding.btnNext.visibility = View.VISIBLE
                    refreshFragment(currentStep, isNext = false)
                }
            }
        }
    }
    private enum class STEP{
        PREV,
        NEXT
    }
}