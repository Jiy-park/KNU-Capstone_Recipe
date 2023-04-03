package com.example.capstone_recipe.create_adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.example.capstone_recipe.MainActivity
import com.example.capstone_recipe.databinding.ItemCreateExplainationBinding

class ExplanationAdapter() : RecyclerView.Adapter<ExplanationAdapter.Holder>() {
    private lateinit var context: Context
    private var explanationList = mutableListOf<String>("")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemCreateExplainationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        context = binding.root.context
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(position, explanationList[position])
    }

    override fun getItemCount() = explanationList.size

    fun moveItem(fromPosition: Int, toPosition: Int) {
        val item = explanationList.removeAt(fromPosition)
        explanationList.add(toPosition, item)
    }

    inner class Holder(private val binding: ItemCreateExplainationBinding): RecyclerView.ViewHolder(binding.root) {
        private var currentPosition: Int = -1

        init {
            binding.ivExplanationImage.setOnClickListener {
            }
            binding.tvAddExplanation.setOnClickListener {
                explanationList.add(currentPosition+1, "")
                notifyItemInserted(currentPosition+1)
                for (i in currentPosition until explanationList.size) { notifyItemChanged(i) }

            }
            binding.tvRemoveExplanation.setOnClickListener {
                if(explanationList.size > 1){
                    explanationList.removeAt(currentPosition)
                    notifyItemRemoved(currentPosition)
                    for (i in currentPosition until explanationList.size) { notifyItemChanged(i) }
                }
                else {
                    Toast.makeText(context, "과정은 하나 이상 입력해주세요!", Toast.LENGTH_SHORT).show() }
            }
            binding.tvSetTimer.setOnClickListener {
                Log.d("LOG_CHECK", "position : $currentPosition, list : ${explanationList[currentPosition]}")
            }
        }

        fun bind(position: Int, explanation: String) {
            currentPosition = position
            binding.editExplanation.setText(explanation)
        }
    }
}