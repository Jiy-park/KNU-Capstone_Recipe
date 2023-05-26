package com.example.capstone_recipe.create_test

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.capstone_recipe.R
import com.example.capstone_recipe.create_test.create_fragment.RecipeCreateStepSecond
import com.example.capstone_recipe.data_class.RecipeStep
import com.example.capstone_recipe.databinding.ItemCreateExplainationBinding
import com.example.capstone_recipe.dialog.DialogFunc

class RecipeStepAdapter(val parent: RecipeCreateStepSecond): RecyclerView.Adapter<RecipeStepAdapter.Holder>() {
    private lateinit var binding: ItemCreateExplainationBinding
    private lateinit var context: Context

    private var recipeStepList = mutableListOf<RecipeStep>()
    private var recipeStepImageUriList = mutableListOf<Uri>()

    fun getRecipeStepList() = recipeStepList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        binding = ItemCreateExplainationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        context = binding.root.context

        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(recipeStepList[position], recipeStepImageUriList[position])
    }

    override fun getItemCount() = recipeStepList.size

    /** * 아이템을 롱클릭하여 순서를 변동 시킴*/
    fun moveItem(fromPosition: Int, toPosition: Int) {
        val item = recipeStepList.removeAt(fromPosition)
        val item2 = recipeStepImageUriList.removeAt(fromPosition)
        recipeStepList.add(toPosition, item)
        recipeStepImageUriList.add(toPosition, item2)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateRecipeStepInfo(_recipeStepList: MutableList<RecipeStep>, _recipeStepImageUriList: MutableList<Uri>){
        recipeStepList = _recipeStepList
        recipeStepImageUriList = _recipeStepImageUriList
        notifyDataSetChanged()
    }

    inner class Holder(val binding: ItemCreateExplainationBinding): RecyclerView.ViewHolder(binding.root){
        private val defaultImageUri = Uri.parse("android.resource://${parent.context?.packageName}/${R.drawable.default_recipe_main_image}")
        fun bind(recipeStep: RecipeStep, recipeStepImageUri: Uri){
            binding.editExplanation.setText(recipeStep.explanation)
            Glide.with(context)
                .load(recipeStepImageUri)
                .error(R.drawable.default_recipe_main_image)
                .into(binding.ivExplanationImage)

            setViewEvent()
        }
        @SuppressLint("NewApi")
        private fun setViewEvent(){
            binding.ivExplanationImage.setOnClickListener {
                parent.setImageFromGallery(binding.ivExplanationImage, bindingAdapterPosition)
            }

            binding.tvAddExplanation.setOnClickListener {
                recipeStepList.add(bindingAdapterPosition+1, RecipeStep())
                recipeStepImageUriList.add(bindingAdapterPosition+1, defaultImageUri)
                notifyItemInserted(bindingAdapterPosition+1)
                for (i in bindingAdapterPosition until recipeStepList.size) { notifyItemChanged(i) }
            }

            binding.tvRemoveExplanation.setOnClickListener {
                if(recipeStepList.size > 1){
                    recipeStepList.removeAt(bindingAdapterPosition)
                    recipeStepImageUriList.removeAt(bindingAdapterPosition)
                    notifyItemRemoved(bindingAdapterPosition)
                    for (i in bindingAdapterPosition until recipeStepList.size) { notifyItemChanged(i) }
                }
                else { Toast.makeText(context, "과정은 하나 이상 입력해주세요!", Toast.LENGTH_SHORT).show() }
            }

            binding.tvSetTimer.setOnClickListener {
                DialogFunc.timerDialog(context, 0, 0, 0)
            }

            binding.editExplanation.addTextChangedListener(object : TextWatcher { // 설명 저장
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    recipeStepList[bindingAdapterPosition].explanation = binding.editExplanation.text.toString()
                }
            })

        }
    }
}