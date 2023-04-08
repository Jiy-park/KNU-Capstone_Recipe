package com.example.capstone_recipe.create_adapter

import android.content.Context
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.capstone_recipe.R
import com.example.capstone_recipe.create_fragments.RecipeCreateStepSecond
import com.example.capstone_recipe.data_class.RecipeStep
import com.example.capstone_recipe.databinding.ItemCreateExplainationBinding
import com.example.capstone_recipe.dialog.DialogFunc

class ExplanationAdapter(_createStepList: MutableList<RecipeStep>, var parent: RecipeCreateStepSecond): RecyclerView.Adapter<ExplanationAdapter.Holder>() {
    private lateinit var context: Context
    private var createStepList = _createStepList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemCreateExplainationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        context = binding.root.context
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(createStepList[position])
    }

    override fun getItemCount() = createStepList.size

    fun moveItem(fromPosition: Int, toPosition: Int) {
        val item = createStepList.removeAt(fromPosition)
        createStepList.add(toPosition, item)
    }

    inner class Holder(private val binding: ItemCreateExplainationBinding): RecyclerView.ViewHolder(binding.root) {
        var timerHour = 0
        var timerMin = 0
        var timerSec = 0

        fun bind(oneStep: RecipeStep) {
            val packageName = "com.example.capstone_recipe"
            var uri = Uri.parse("android.resource://$packageName/${R.drawable.ex_img}") // 이미지 선택 안할 시 나오는 기본 이미지

            binding.editExplanation.setText(oneStep.explanation)   // 설명
            if (oneStep.Image != null) { uri = oneStep.Image } // 유저가 선택한 이미지로 세팅
            binding.ivExplanationImage.setImageURI(uri)
            setEvent()
        }

        private fun setEvent(){
            binding.ivExplanationImage.setOnClickListener {     // 이미지 선택
                parent.setImageFromGallery(binding.ivExplanationImage, bindingAdapterPosition)
            }

            binding.tvAddExplanation.setOnClickListener {       // 추가 버튼
                createStepList.add(bindingAdapterPosition+1, RecipeStep("", null, 0, 0, 0))
                notifyItemInserted(bindingAdapterPosition+1)
                for (i in bindingAdapterPosition until createStepList.size) { notifyItemChanged(i) }
            }

            binding.tvRemoveExplanation.setOnClickListener {    // 삭제 버튼
                if(createStepList.size > 1){
                    createStepList.removeAt(bindingAdapterPosition)
                    notifyItemRemoved(bindingAdapterPosition)
                    for (i in bindingAdapterPosition until createStepList.size) { notifyItemChanged(i) }
                }
                else { Toast.makeText(context, "과정은 하나 이상 입력해주세요!", Toast.LENGTH_SHORT).show() }
            }

            binding.tvSetTimer.setOnClickListener {             // 타이머
                DialogFunc.timerDialog(context, timerHour, timerMin, timerSec)
            }

            binding.editExplanation.addTextChangedListener(object : TextWatcher { // 설명 저장
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    createStepList[bindingAdapterPosition].explanation = binding.editExplanation.text.toString()
                }
            })
        }

    }
}