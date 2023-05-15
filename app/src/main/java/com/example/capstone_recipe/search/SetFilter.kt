package com.example.capstone_recipe.search

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.capstone_recipe.R
import com.example.capstone_recipe.data_class.Filter
import com.example.capstone_recipe.data_class.LEVEL
import com.example.capstone_recipe.databinding.ActivitySetFilterBinding

class SetFilter() : AppCompatActivity() {
    private val binding by lazy { ActivitySetFilterBinding.inflate(layoutInflater) }
    private lateinit var context: Context
    private lateinit var filter: Filter
    private val includeIngredient = mutableListOf<String>() // Search 클래스에 넘길 정보
    private val excludeIngredient = mutableListOf<String>() // Search 클래스에 넘길 정보
    private val includeViewList = mutableListOf<View>() // 새로 생성된 뷰들을 관리
    private val excludeViewList = mutableListOf<View>() // 새로 생성된 뷰들을 관리
    private var level = LEVEL.EASY

    @SuppressLint("ResourceType", "NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        context = binding.root.context
        filter = intent.getSerializableExtra("filter", Filter::class.java)!!


        binding.topPanel.btnBack.setOnClickListener { finish() }
        binding.btnDone.setOnClickListener {
            val intent = Intent(context, Search::class.java)
            intent.putExtra("filter", buildFilter())
            setResult(RESULT_OK, intent)
            finish()
        }
        setFilterState()
        setCheckBoxEvent()
        setViewEvent()
    }

    /** * 체크 박스 이벤트 설정 */
    private fun setCheckBoxEvent(){
        binding.checkInclude.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){ addIngredient(isInclude = true) }
            else{
                binding.linearIncludeIngredient.removeAllViews()
                includeIngredient.clear()
            }
        }

        binding.checkExclude.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){ addIngredient(isInclude = false) }
            else{
                binding.linearExcludeIngredient.removeAllViews()
                excludeIngredient.clear()
            }
        }

        binding.checkTime.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) { binding.linearQuestionTime.visibility = View.VISIBLE }
            else { binding.linearQuestionTime.visibility = View.GONE }
        }

        binding.checkCalorie.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) { binding.linearQuestionCalorie.visibility = View.VISIBLE }
            else { binding.linearQuestionCalorie.visibility = View.GONE }
        }

        binding.checkLevel.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) { binding.rgLevelSelection.visibility = View.VISIBLE }
            else { binding.rgLevelSelection.visibility = View.GONE }
        }
    }

    /** * 각 뷰 이벤트 정의*/
    private fun setViewEvent(){
        binding.topPanel.btnBack.setOnClickListener { finish() }    // 상단 바의 뒤로가기 버튼
        binding.btnDone.setOnClickListener {                 // 하단의 적용 버튼
            val intent = Intent(context, Search::class.java)
            intent.putExtra("filter", buildFilter())
            setResult(RESULT_OK, intent)
            finish()
        }
        binding.rgLevelSelection.setOnCheckedChangeListener { _, checkedId -> // 라디오 버튼 텍스트 변경
            setRadioTextColor(checkedId, context) //checkedId에 해당하는 라디오 버튼 텍스트만 색 변경 및 난이도 저장
        }
    }

    /** *기존에 설정했던 필터 내용 복구 */
    private fun setFilterState(){
        filter.includeIngredient?.let {// 포함 재료
            binding.checkInclude.isChecked = true
            binding.linearIncludeIngredient.visibility = View.VISIBLE
            it.forEach { ingredientName ->
                addIngredient(isInclude = true, ingredientName)
            }
        }

        filter.excludeIngredient?.let {// 제외 재료
            binding.checkExclude.isChecked = true
            binding.linearExcludeIngredient.visibility = View.VISIBLE
            it.forEach { ingredientName ->
                addIngredient(isInclude = false, ingredientName)
            }
        }

        filter.time?.let { time ->
            binding.linearQuestionTime.visibility = View.VISIBLE
            binding.checkTime.isChecked = true
            binding.editAnswerTime.setText(time)
        }

        filter.calorie?.let {
            binding.linearQuestionCalorie.visibility = View.VISIBLE
            binding.checkCalorie.isChecked = true
            binding.editAnswerCalorie.setText(it.toString())
        }

        filter.level?.let { checkedLevel ->
            binding.rgLevelSelection.visibility = View.VISIBLE
            binding.checkLevel.isChecked = true
            when(checkedLevel){
                LEVEL.EASY -> {
                    binding.radioLevelEasy.isChecked = true
                }
                LEVEL.NORMAL-> {
                    binding.radioLevelNormal.isChecked = true
                }
                LEVEL.HARD -> {
                    binding.radioLevelHard.isChecked = true
                }
            }
        }
    }

    /** *적용 버튼 누를 시 호출, 사용자가 필터링 설정한 값들은 모아 Search 클래스로 보냄  */
    private fun buildFilter(): Filter{
        buildIngredientFilter()
        return Filter(
            includeIngredient = if(binding.checkInclude.isChecked) { includeIngredient } else { null },
            excludeIngredient = if(binding.checkExclude.isChecked) { excludeIngredient } else { null },
            time = if(binding.checkTime.isChecked) { binding.editAnswerTime.text.toString() } else { null },
            calorie = if(binding.checkCalorie.isChecked) { binding.editAnswerCalorie.text.toString().toInt() } else { null },
            level = if(binding.checkLevel.isChecked) { level } else { null }
        )
    }

    /** * buildFilter() 호출 시 호출, 모든 필터를 모으기 전 재료(포함/제와) 필터를 정리
     * -> 뷰 리스트에 있는 뷰를 모두 돌며 유효한 값(재료 이름)을 가진 뷰의 값들만 뽑아
     * 각 재료 포함/제외 리스트에 대입*/
    private fun buildIngredientFilter(){
        includeViewList.forEach { view ->
            val ingredientName = view.findViewById<EditText>(R.id.editIngredientName).text.toString()
            if(ingredientName.isNotEmpty()) { includeIngredient.add(ingredientName) }
        }
        excludeViewList.forEach { view->
            val ingredientName = view.findViewById<EditText>(R.id.editIngredientName).text.toString()
            if(ingredientName.isNotEmpty()) { excludeIngredient.add(ingredientName) }
        }
    }

    /** - 재료를 적을 수 있는 칸 추가 (리스트에 방을 추가하는 형식)
     * - isInclude = true -> 포함 재료에 추가
     * - isInclude = false -> 불포함 재료에 추가*/
    private fun addIngredient(isInclude: Boolean, text: String? = ""){
        val newView = LayoutInflater.from(context).inflate(R.layout.common_filter_ingredient, null)
        newView.id = View.generateViewId()
        newView.requestFocus()

        val editIngredientName = newView.findViewById<EditText>(R.id.editIngredientName)
        val btnAdd = newView.findViewById<ImageButton>(R.id.btnAddIngredient)
        val btnRemove = newView.findViewById<ImageButton>(R.id.btnRemoveIngredient)

        editIngredientName.setText(text)
        btnAdd.setOnClickListener { addIngredient(isInclude) }
        btnRemove.setOnClickListener { removeIngredient(newView, isInclude) }

        if(isInclude){
            binding.linearIncludeIngredient.addView(newView)
            includeViewList.add(newView)
        }
        else {
            binding.linearExcludeIngredient.addView(newView)
            excludeViewList.add(newView)
        }
    }

    /** * addIngredient() 함수로 추가된 뷰를 지움. -> 지울 때 includeViewList/excludeViewList 에서 해당 뷰도 같이 지워준다.*/
    private fun removeIngredient(view: View, isInclude: Boolean){
        if(isInclude){
            binding.linearIncludeIngredient.removeView(view)
            includeViewList.remove(view)
            if(includeViewList.size == 0) { binding.checkInclude.isChecked = false }
        }
        else{
            binding.linearExcludeIngredient.removeView(view)
            excludeViewList.remove(view)
            if(excludeViewList.size == 0) { binding.checkExclude.isChecked = false }
        }
    }
    /** * */
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