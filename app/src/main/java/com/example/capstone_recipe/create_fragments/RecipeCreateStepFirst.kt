package com.example.capstone_recipe.create_fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.constraintlayout.widget.Group
import androidx.core.content.ContextCompat
import com.example.capstone_recipe.R
import com.example.capstone_recipe.databinding.CommonRecipeIngredientBinding
import com.example.capstone_recipe.databinding.FragmentRecipeCreateStepFirstBinding

class RecipeCreateStepFirst : Fragment() {
    private lateinit var binding:FragmentRecipeCreateStepFirstBinding
    private lateinit var context:Context
    private val ingredientIdList = mutableListOf<Int>() // 재료 추가 뷰 아이디 저장용 리스트

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRecipeCreateStepFirstBinding.inflate(inflater, container, false)
        context = binding.root.context

        addIngredientLayer() // 첫번째 재료

        binding.rgLevelSelection.setOnCheckedChangeListener { _, checkedId -> // 라디오 버튼 텍스트 변경
            setRadioTextColor(checkedId, context) //checkedId에 해당하는 라디오 버튼 텍스트만 색 변경
        }

        binding.layerQuestionAmount.tvQuestion.text = "양은 얼마나 되나요?"
        binding.layerQuestionAmount.tvUnit.text = "인분"
//        Resources.getSystem().getString(R.string.app_name)

        return binding.root
    }

    @SuppressLint("InflateParams")
    private fun addIngredientLayer(){ // 재료 추가 뷰 생성 함수
        val addView = LayoutInflater.from(context).inflate(R.layout.common_recipe_ingredient, null)
        addView.id = View.generateViewId()
        addView.requestFocus()

        addView.findViewById<ImageButton>(R.id.btnAddIngredient).setOnClickListener { addIngredientLayer() }
        addView.findViewById<ImageButton>(R.id.btnRemoveIngredient).setOnClickListener {
            val parentViewId = (it.parent as ViewGroup).id
            removeIngredientLayer(parentViewId)
        }

        binding.linearForIngredient.addView(addView)
        ingredientIdList.add(addView.id)

        if(ingredientIdList.size == 1){
            val viewId = ingredientIdList[0]
            val view = binding.root.findViewById<View>(viewId)
            view.findViewById<ImageButton>(R.id.btnRemoveIngredient).visibility = View.INVISIBLE
        }
        else{
            val index = ingredientIdList.size - 2
            val viewId = ingredientIdList[index]
            val view = binding.root.findViewById<View>(viewId)
            view.findViewById<ImageButton>(R.id.btnAddIngredient).visibility = View.INVISIBLE
        }
    }

    private fun removeIngredientLayer(prentViewId:Int){ //재료 추가 뷰 삭제 함수
        val view = binding.root.findViewById<View>(prentViewId)
        binding.linearForIngredient.removeView(view)
        val indexOfParentViewId = ingredientIdList.indexOf(prentViewId)
        ingredientIdList.remove(prentViewId)

        if(indexOfParentViewId == ingredientIdList.size){
            val lastViewId =  ingredientIdList[indexOfParentViewId-1]
            val lastView = binding.root.findViewById<View>(lastViewId)
            lastView.findViewById<ImageButton>(R.id.btnAddIngredient).visibility = View.VISIBLE
        }
    }

    private fun setRadioTextColor(targetId:Int, context:Context){ // 라디오 버튼 텍스트 색 변경 함수
        binding.run{
            when(targetId){
                R.id.radioLevelEasy -> {
                    radioLevelEasy.setTextColor(ContextCompat.getColor(context, R.color.main_color_start))
                    radioLevelNormal.setTextColor(ContextCompat.getColor(context, R.color.main_text))
                    radioLevelHard.setTextColor(ContextCompat.getColor(context, R.color.main_text))
                }
                R.id.radioLevelNormal -> {
                    radioLevelEasy.setTextColor(ContextCompat.getColor(context, R.color.main_text))
                    radioLevelNormal.setTextColor(ContextCompat.getColor(context, R.color.main_color_start))
                    radioLevelHard.setTextColor(ContextCompat.getColor(context, R.color.main_text))
                }
                R.id.radioLevelHard -> {
                    radioLevelEasy.setTextColor(ContextCompat.getColor(context, R.color.main_text))
                    radioLevelNormal.setTextColor(ContextCompat.getColor(context, R.color.main_text))
                    radioLevelHard.setTextColor(ContextCompat.getColor(context, R.color.main_color_start))
                }
            }
        }
    }
}