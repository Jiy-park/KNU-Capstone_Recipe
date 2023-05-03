package com.example.capstone_recipe.recipe_create.create_fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RadioButton
import androidx.core.content.ContextCompat
import com.example.capstone_recipe.R
import com.example.capstone_recipe.data_class.Ingredient
import com.example.capstone_recipe.data_class.LEVEL
import com.example.capstone_recipe.data_class.RecipeBasicInfo
import com.example.capstone_recipe.databinding.FragmentRecipeCreateStepFirstBinding
import com.example.capstone_recipe.recipe_create.UpdateValue

class RecipeCreateStepFirst : Fragment() {
    private lateinit var updateCollBack: UpdateValue
    private lateinit var binding:FragmentRecipeCreateStepFirstBinding
    private lateinit var context:Context
    private val ingredientIdList = mutableListOf<Int>() // 재료 추가 뷰 아이디 저장용 리스트
     val ingredientList = mutableListOf<Ingredient>()
    private var level = LEVEL.EASY

    override fun onAttach(context: Context) {
        super.onAttach(context)
        updateCollBack = context as UpdateValue
    }

    override fun onResume() {
        super.onResume()
        Log.d("LOG_CHECK", "RecipeCreateStepFirst :: onResume() -> ")
    }

    override fun onPause() {
        super.onPause()
        Log.d("LOG_CHECK", "RecipeCreateStepFirst :: onPause() -> ")
    }

    override fun onStop() {
        Log.d("LOG_CHECK", "RecipeCreateStepFirst :: onStop() -> 레시피 타이틀 적용 안됨 체크용 ")
        super.onStop()
        val title = binding.editCreateTitle.text.toString()
        val intro = binding.editCreateIntro.text.toString()
        val time = binding.layerQuestionTime.editAnswer.text.toString()
        val amount = binding.layerQuestionAmount.editAnswer.text.toString()
        updateCollBack.updateBasicInfo(
            RecipeBasicInfo(
                title = title,
                intro = intro,
                time = time,
                amount = amount,
                level = level,
            )
        )
        updateCollBack.updateIngredientList(ingredientList)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRecipeCreateStepFirstBinding.inflate(inflater, container, false)
        context = binding.root.context

        addIngredientLayer() // 첫번째 재료

        binding.rgLevelSelection.setOnCheckedChangeListener { _, checkedId -> // 라디오 버튼 텍스트 변경
            setRadioTextColor(checkedId, context) //checkedId에 해당하는 라디오 버튼 텍스트만 색 변경 및 난이도 저장
        }

        binding.layerQuestionAmount.tvQuestion.text = "양은 얼마나 되나요?"
        binding.layerQuestionAmount.tvUnit.text = "인분"

        return binding.root
    }

    @SuppressLint("InflateParams")
    private fun addIngredientLayer(){ // 재료 추가 뷰 생성 함수
        val addView = LayoutInflater.from(context).inflate(R.layout.common_recipe_ingredient, null)
        addView.id = View.generateViewId()
        addView.requestFocus()

        addView.findViewById<ImageButton>(R.id.btnAddIngredient).setOnClickListener { addIngredientLayer() }       // 재료 추가
        addView.findViewById<ImageButton>(R.id.btnRemoveIngredient).setOnClickListener {                    // 재료 제거
            val parentViewId = (it.parent as ViewGroup).id
            removeIngredientLayer(parentViewId)
        }
        addView.findViewById<EditText>(R.id.editIngredientName).addTextChangedListener(object: TextWatcher{         // 재료 이름
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val position = ingredientIdList.indexOf(addView.id)
                ingredientList[position].name = addView.findViewById<EditText>(R.id.editIngredientName).text.toString()
            }
        })
        addView.findViewById<EditText>(R.id.editIngredientAmount).addTextChangedListener(object: TextWatcher{       // 재료 양
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val position = ingredientIdList.indexOf(addView.id)
                ingredientList[position].amount = addView.findViewById<EditText>(R.id.editIngredientAmount).text.toString()
            }
        })

        binding.linearForIngredient.addView(addView)

        ingredientIdList.add(addView.id)
        ingredientList.add(Ingredient("", ""))


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
        val position = ingredientIdList.indexOf(prentViewId)
        ingredientIdList.removeAt(position)
        ingredientList.removeAt(position)

        Log.d("LOG_CHECK", "id list : $ingredientIdList , ingredient list : $ingredientList")

        if(position == ingredientIdList.size){
            val lastViewId =  ingredientIdList[position-1]
            val lastView = binding.root.findViewById<View>(lastViewId)
            lastView.findViewById<ImageButton>(R.id.btnAddIngredient).visibility = View.VISIBLE
        }
    }

    private fun setRadioTextColor(targetId:Int, context:Context){ // 라디오 버튼 텍스트 색 변경 함수
        val radioLevel = listOf<Int>(
            R.id.radioLevelEasy,
            R.id.radioLevelNormal,
            R.id.radioLevelHard
        )

        for(i in radioLevel.indices){
            binding.root.findViewById<RadioButton>(radioLevel[i]).setTextColor(ContextCompat.getColor(context, R.color.main_text))
            if(radioLevel[i] == targetId){
                binding.root.findViewById<RadioButton>(radioLevel[i]).setTextColor(ContextCompat.getColor(context, R.color.main_color_start))
                level = LEVEL.values()[i] // 레시피 난이도 저장
            }
        }
    }
}