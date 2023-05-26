package com.example.capstone_recipe.create_test.create_fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.capstone_recipe.R
import com.example.capstone_recipe.data_class.Ingredient
import com.example.capstone_recipe.data_class.LEVEL
import com.example.capstone_recipe.data_class.RecipeBasicInfo
import com.example.capstone_recipe.databinding.FragmentRecipeCreateStepFirstBinding
import java.util.logging.Level

class RecipeCreateStepFirst(_recipeBasicInfo: RecipeBasicInfo, _recipeIngredientList: List<Ingredient>) : Fragment() {
    private lateinit var binding: FragmentRecipeCreateStepFirstBinding
    private lateinit var context: Context

    /** * 아래 변수(2) : 액티비티로 전달할 것들*/
    private var recipeBasicInfo = _recipeBasicInfo
    private var recipeIngredientList = _recipeIngredientList

    /** * 아래 변수(2) : 액티비티로 전달 전 build 함수 보조 변수*/
    private var ingredientViewList = mutableListOf<View>()
    private var recipeLevel = LEVEL.EASY

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRecipeCreateStepFirstBinding.inflate(inflater, container, false)
        context = binding.root.context

        setViewEvent()
        setViewByRecipeInfo()

        return binding.root
    }

    /** * 각 뷰들의 이벤트 정의*/
    private fun setViewEvent(){
        binding.rgLevelSelection.setOnCheckedChangeListener { _, checkedId ->
            setRadioTextColor(checkedId, context)
        }
    }

    /** * 레시피 정보에 맞게 뷰 값들 설정*/
    private fun setViewByRecipeInfo(){
        binding.editCreateTitle.setText(recipeBasicInfo.title)
        binding.editCreateIntro.setText(recipeBasicInfo.intro)
        binding.layerQuestionTime.editAnswer.setText(recipeBasicInfo.time)
        binding.layerQuestionAmount.editAnswer.setText(recipeBasicInfo.amount)
        binding.layerQuestionAmount.tvQuestion.text = "양은 얼마나 되나요?"
        binding.layerQuestionAmount.tvUnit.text = "인분"

        when(recipeBasicInfo.level){
            LEVEL.EASY -> binding.radioLevelEasy.isChecked = true
            LEVEL.HARD -> binding.radioLevelHard.isChecked = true
            LEVEL.NORMAL -> binding.radioLevelHard.isChecked = true
        }

        binding.linearForIngredient.removeAllViews() // 재료 뷰를 추가하기 전에 기존 뷰들을 전부 지워줌
        recipeIngredientList.forEach { ingredient ->
            if(ingredient.name.isNotEmpty() && ingredient.amount.isNotEmpty()){
                addIngredientView(ingredient)
            }
        }
        addIngredientView()
    }

    /** * 재료를 적을 공간(뷰)을 추가*/
    private fun addIngredientView(ingredient: Ingredient? = null){
        val newView = LayoutInflater.from(context).inflate(R.layout.common_recipe_ingredient, null)
        newView.id = View.generateViewId()
        newView.requestFocus()

        val editIngredientName = newView.findViewById<EditText>(R.id.editIngredientName)
        val editIngredientAmount = newView.findViewById<EditText>(R.id.editIngredientAmount)
        val btnAdd = newView.findViewById<ImageButton>(R.id.btnAddIngredient)
        val btnRemove = newView.findViewById<ImageButton>(R.id.btnRemoveIngredient)

        editIngredientName.setText(ingredient?.name?:"")
        editIngredientAmount.setText(ingredient?.amount?:"")
        btnAdd.setOnClickListener { addIngredientView() }
        btnRemove.setOnClickListener { removeIngredient(newView) }

        binding.linearForIngredient.addView(newView)
        ingredientViewList.add(newView)
    }

    /** * addIngredientView() 함수로 추가된 뷰를 지움. -> 지울 때 ingredientViewList 에서 해당 뷰도 같이 지워준다.*/
    private fun removeIngredient(view: View){
        if(ingredientViewList.size == 1) { Toast.makeText(context, "재료는 한 개 이상 넣어 주세요!", Toast.LENGTH_SHORT).show() }
        else {
            binding.linearForIngredient.removeView(view)
            ingredientViewList.remove(view)
        }
    }

    /** * 라디오 버튼 텍스트 색 변경 함수*/
    private fun setRadioTextColor(targetId:Int, context:Context){
        val radioLevel = listOf<Int>(
            R.id.radioLevelEasy,
            R.id.radioLevelNormal,
            R.id.radioLevelHard
        )

        for(i in radioLevel.indices){
            binding.root.findViewById<RadioButton>(radioLevel[i]).setTextColor(ContextCompat.getColor(context, R.color.main_text))
            if(radioLevel[i] == targetId){
                binding.root.findViewById<RadioButton>(radioLevel[i]).setTextColor(ContextCompat.getColor(context, R.color.main_color_start))
                recipeLevel = LEVEL.values()[i] // 레시피 난이도 저장
            }
        }
    }

    /** * 액티비티에서 호출. 뷰에 적힌 값들을 recipeBasicInfo 에 정리 후 recipeBasicInfo 반납*/
    fun buildRecipeBasicInfo(): RecipeBasicInfo{
        recipeBasicInfo.title = binding.editCreateTitle.text.toString()
        recipeBasicInfo.intro = binding.editCreateIntro.text.toString()
        recipeBasicInfo.time = binding.layerQuestionTime.editAnswer.text.toString()
        recipeBasicInfo.amount = binding.layerQuestionAmount.editAnswer.text.toString()
        recipeBasicInfo.level = recipeLevel

        return recipeBasicInfo
    }

    /** * 액티비티에서 호출. 뷰에 적힌 값들을 tempIngredientList 에 정리 후 tempIngredientList 반납*/
    fun buildRecipeIngredientList(): List<Ingredient>{
        val tempIngredientList = mutableListOf<Ingredient>()
        ingredientViewList.forEach { view ->
            val iName = view.findViewById<EditText>(R.id.editIngredientName).text.toString()
            val iAmount = view.findViewById<EditText>(R.id.editIngredientAmount).text.toString()
            tempIngredientList.add(Ingredient(iName, iAmount))
        }

        return tempIngredientList
    }
}