package com.example.capstone_recipe

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import com.example.capstone_recipe.create_fragments.RecipeCreateComplete
import com.example.capstone_recipe.create_fragments.RecipeCreateStepFirst
import com.example.capstone_recipe.create_fragments.RecipeCreateStepSecond
import com.example.capstone_recipe.create_fragments.RecipeCreateStepThird
import com.example.capstone_recipe.databinding.ActivityRecipeCreateBinding
import com.example.capstone_recipe.databinding.CommonRecipeIngredientBinding

class RecipeCreate : AppCompatActivity() {
    private val binding by lazy { ActivityRecipeCreateBinding.inflate(layoutInflater) }
    private lateinit var context:Context

    private var currentStep = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        context = binding.root.context

        binding.topPanel.btnBack.setOnClickListener { finish() }

        supportFragmentManager
            .beginTransaction()
            .add(R.id.mainFrame, RecipeCreateStepFirst())
            .addToBackStack(null)
            .commit()

        binding.btnNext.setOnClickListener {
            currentStep++
            checkCurrentStep(currentStep)
        }

        binding.btnPrev.setOnClickListener {
            currentStep--
            checkCurrentStep(currentStep)
        }


    }
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (supportFragmentManager.backStackEntryCount > 0) { supportFragmentManager.popBackStack() }
            else{ finish() }
            return true
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
            .replace(R.id.mainFrame, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun checkCurrentStep(currentStep:Int){
        when(currentStep){
            1 -> {
                binding.btnPrev.visibility = View.GONE
                replaceFragment(RecipeCreateStepFirst())
            }
            2 -> {
                binding.btnPrev.visibility = View.VISIBLE
                binding.btnNext.visibility = View.VISIBLE
                replaceFragment(RecipeCreateStepSecond())
            }
            3 -> {
                binding.btnPrev.visibility = View.VISIBLE
                binding.btnNext.visibility = View.VISIBLE
                replaceFragment(RecipeCreateStepThird())
            }
            4 -> {
                binding.btnPrev.visibility = View.GONE
                binding.btnNext.visibility = View.GONE
                replaceFragment(RecipeCreateComplete())
            }
        }
    }

    enum class ANIMATION_DERECTION{

    }
}