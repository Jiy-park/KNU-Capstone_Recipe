package com.example.capstone_recipe

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.fragment.app.Fragment
import com.example.capstone_recipe.create_fragments.RecipeCreateComplete
import com.example.capstone_recipe.create_fragments.RecipeCreateStepFirst
import com.example.capstone_recipe.create_fragments.RecipeCreateStepSecond
import com.example.capstone_recipe.create_fragments.RecipeCreateStepThird
import com.example.capstone_recipe.data_class.*
import com.example.capstone_recipe.databinding.ActivityRecipeCreateBinding

class RecipeCreate : AppCompatActivity() {
    private val binding by lazy { ActivityRecipeCreateBinding.inflate(layoutInflater) }
    private lateinit var context:Context



    private var ingredientList = mutableListOf<RecipeIngredient>()               // 첫번째 - 재료 리스트
    private var createStepList = mutableListOf<RecipeStep>(                     // 두번째 - 단계 리스트
        RecipeStep("", null, 0, 0, 0) // 텍스트 설명,  이미지 uri, 타이머 유무
    )
    private var recipeBasicInfo = RecipeBasicInfo(                              // 첫번째, 세번째 - 레시피 기본 정보
        "",
        "",
        "",
        "",
        LEVEL.EASY,
        null,
        SHARE.ONLY_ME
    )


    private var currentStep = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        context = binding.root.context

        binding.topPanel.btnBack.setOnClickListener { finish() }

        supportFragmentManager
            .beginTransaction()
            .add(R.id.mainFrame, RecipeCreateStepFirst(ingredientList, recipeBasicInfo))
            .addToBackStack(null)
            .commit()


        binding.btnNext.setOnClickListener {
            currentStep++
            checkCurrentStep(currentStep)
        }

        binding.btnPrev.setOnClickListener {
            currentStep--
            if(currentStep == 1) { binding.btnPrev.visibility = View.GONE }
            supportFragmentManager.popBackStack()
        }


    }
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return if (supportFragmentManager.backStackEntryCount > 1) {
                currentStep--
                if(currentStep == 1) { binding.btnPrev.visibility = View.GONE }
                supportFragmentManager.popBackStack()
                true
            }
            else{
                finish()
                true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(
                R.anim.animation_enter_from_right,
                R.anim.animation_exit_to_left,
                R.anim.animation_enter_from_left,
                R.anim.animation_exit_to_right
            )
            .add(R.id.mainFrame, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun checkCurrentStep(currentStep:Int){
        when(currentStep){
            1 -> {
                binding.btnPrev.visibility = View.GONE
                replaceFragment(RecipeCreateStepFirst(ingredientList, recipeBasicInfo))
            }
            2 -> {
                binding.btnPrev.visibility = View.VISIBLE
                binding.btnNext.visibility = View.VISIBLE
                replaceFragment(RecipeCreateStepSecond(createStepList))
            }
            3 -> {
                binding.btnPrev.visibility = View.VISIBLE
                binding.btnNext.visibility = View.VISIBLE
                replaceFragment(RecipeCreateStepThird(recipeBasicInfo, createStepList))
            }
            4 -> {
                binding.topPanel.root.visibility = View.GONE
                binding.btnPrev.visibility = View.GONE
                binding.btnNext.visibility = View.GONE
                replaceFragment(RecipeCreateComplete())
            }
        }
    }
}