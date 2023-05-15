package com.example.capstone_recipe.search

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.capstone_recipe.data_class.Filter
import com.example.capstone_recipe.databinding.ItemFilterOptionBinding

class SearchFilterAdapter(private val filter: Filter):RecyclerView.Adapter<SearchFilterAdapter.Holder>() {
    private lateinit var binding: ItemFilterOptionBinding
    private lateinit var context: Context
    var filterOptionList = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        binding = ItemFilterOptionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        context = binding.root.context
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(filterOptionList[position])
    }

    override fun getItemCount() = filterOptionList.size

    inner class Holder(val binding: ItemFilterOptionBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(filterOption: String){
            binding.btnFilterOption.text = filterOption
            binding.btnFilterOption.setOnClickListener {
                filterOptionList.removeAt(bindingAdapterPosition)
                notifyItemRemoved(bindingAdapterPosition)
                removeOptionFromFilter(filter, filterOption)
            }
        }
        /** * filterOption 의 값이 어떤 옵션인지 판별 후 filter 목록에서 해당 옵션을 지움 */
        private fun removeOptionFromFilter(filter: Filter, filterOption: String){
            /** * 포함 재료, 제외 재료, 소요 시간, 칼로리 ~이상, 칼로리 ~이사, 난이도  */
//            val discriminateWordList = listOf<String>("포함", "제외", "분", "이상", "이하", "난이도")
            val option = filterOption.split(" ")        // ex) "계란 제외" -> "계란", "제외"
            val discriminationWord = option[option.lastIndex]     // ex) "계란", "제외" -> "제외"

            when(discriminationWord){
                "포함" -> {
                    val removeOption = filterOption.replace(" $discriminationWord", "") // ex) "계란 제외" -> "계란"
                    filter.includeIngredient!!.remove(removeOption)
                }
                "제외" -> {
                    val removeOption = filterOption.replace(" $discriminationWord", "") // ex) "계란 제외" -> "계란"
                    filter.excludeIngredient!!.remove(removeOption)
                }
                "분" -> {
                    filter.time = null
                }
                "이하" -> {
                    filter.calorie = null
                }
                "난이도" ->{
                    filter.level = null
                }
            }
        }
    }
}