package com.example.capstone_recipe.recipe_create.create_adapter

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.capstone_recipe.R
import com.example.capstone_recipe.recipe_create.create_fragments.RecipeCreateStepSecond
import com.example.capstone_recipe.databinding.ItemCreateExplainationBinding
import com.example.capstone_recipe.dialog.DialogFunc

class ExplanationAdapter(
    private val parent: RecipeCreateStepSecond,
    private val stepExplanationList: MutableList<String>,
    private var stepImageList: MutableList<Uri?>
    ): RecyclerView.Adapter<ExplanationAdapter.Holder>() {
    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemCreateExplainationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        context = binding.root.context
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(stepExplanationList[position], stepImageList[position])
    }

    override fun getItemCount() = stepExplanationList.size

    fun moveItem(fromPosition: Int, toPosition: Int) {
        val item = stepExplanationList.removeAt(fromPosition)
        val item2 = stepImageList.removeAt(fromPosition)
        stepExplanationList.add(toPosition, item)
        stepImageList.add(toPosition, item2)
    }

    inner class Holder(private val binding: ItemCreateExplainationBinding): RecyclerView.ViewHolder(binding.root) {
        var timerHour = 0
        var timerMin = 0
        var timerSec = 0

        fun bind(explanation:String, image: Uri?) {
            val packageName = "com.example.capstone_recipe"
            var uri = Uri.parse("android.resource://$packageName/${R.drawable.ex_img}") // 이미지 선택 안할 시 나오는 기본 이미지

            binding.editExplanation.setText(explanation)   // 설명
            if (image != null) { uri = image } // 유저가 선택한 이미지로 세팅
            binding.ivExplanationImage.setImageURI(uri)
            setEvent()
        }

        @SuppressLint("NewApi")
        private fun setEvent(){
            binding.ivExplanationImage.setOnClickListener {     // 이미지 선택
                parent.setImageFromGallery(binding.ivExplanationImage, bindingAdapterPosition)
            }

            binding.tvAddExplanation.setOnClickListener {       // 추가 버튼
                stepExplanationList.add(bindingAdapterPosition+1,"")
                stepImageList.add(bindingAdapterPosition+1, null)
                notifyItemInserted(bindingAdapterPosition+1)
                for (i in bindingAdapterPosition until stepExplanationList.size) { notifyItemChanged(i) }
            }

            binding.tvRemoveExplanation.setOnClickListener {    // 삭제 버튼
                if(stepExplanationList.size > 1){
                    stepExplanationList.removeAt(bindingAdapterPosition)
                    stepImageList.removeAt(bindingAdapterPosition)
                    notifyItemRemoved(bindingAdapterPosition)
                    for (i in bindingAdapterPosition until stepExplanationList.size) { notifyItemChanged(i) }
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
                    stepExplanationList[bindingAdapterPosition] = binding.editExplanation.text.toString()
                }
            })
        }

    }
}