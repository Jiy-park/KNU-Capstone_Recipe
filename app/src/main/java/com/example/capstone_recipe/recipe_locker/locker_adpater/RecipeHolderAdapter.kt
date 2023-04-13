package com.example.capstone_recipe.recipe_locker.locker_adpater

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.capstone_recipe.databinding.ItemRecipeHolderBinding

class RecipeHolderAdapter:RecyclerView.Adapter<RecipeHolderAdapter.Holder>() {
    private var recipeHolderList = mutableListOf<String>(
        "내가 만든 레시피",
        "스크랩 한 레시피"
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemRecipeHolderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.settingHolder(recipeHolderList[position])
    }

    override fun getItemCount() = recipeHolderList.size

    class Holder(private val binding:ItemRecipeHolderBinding):RecyclerView.ViewHolder(binding.root){
        private val adapter = RecipeViewerAdapter()
        private val context = binding.root.context
        fun settingHolder(holderTitle:String){
            binding.tvRecipeLockerName.text = holderTitle
            binding.recyclerviewAccordion.adapter = adapter
            binding.recyclerviewAccordion.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            val itemHeight = dp2px(150F, context).toInt()
            val itemCount = adapter.itemCount/3 + 1 // 줄 당 3개의 아이템이 보여짐 -> 줄 개수 조절 해야함 ex) 5개 -> 5/3 + 1 = 2줄
            Log.d("LOG_CHECK", "Holder :: settingHolder() called  $itemCount")
            val collapsedHeight = itemHeight * 1
            val expandedHeight = itemHeight * itemCount // 아이템 높이 * 아이템 개수(3의 배수로 만든 후 곱함)
            addAccordionAnimation(binding.recyclerviewAccordion, collapsedHeight, expandedHeight, binding.tvCollapseToggle)
        }
        @SuppressLint("Recycle")
        private fun addAccordionAnimation(targetView: View, collapsedHeight:Int, expandedHeight:Int, triggerView:View){
            val animator = ValueAnimator.ofInt(collapsedHeight, expandedHeight)
            var isExpended = false

            animator.duration = 300
            animator.addUpdateListener { animation->
                targetView.layoutParams.height = animation.animatedValue as Int
                targetView.requestLayout()
            }

            triggerView.setOnClickListener {
                if(isExpended) { animator.setIntValues(expandedHeight, collapsedHeight) }
                else { animator.setIntValues(collapsedHeight, expandedHeight) }
                animator.start()
                isExpended = !isExpended
            }
        }

        private fun dp2px(dp: Float, context: Context): Float { // dp to px
            val resources = context.resources
            val metrics: DisplayMetrics = resources.displayMetrics
            return dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
        }
    }
}