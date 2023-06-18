package com.example.capstone_recipe.search

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TableLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.capstone_recipe.Preference
import com.example.capstone_recipe.R
import com.example.capstone_recipe.data_class.Filter
import com.example.capstone_recipe.data_class.Ingredient
import com.example.capstone_recipe.data_class.RecipeBasicInfo
import com.example.capstone_recipe.data_class.UserInfo
import com.example.capstone_recipe.databinding.ActivitySearchBinding
import com.example.capstone_recipe.search.adapter.*
import com.example.capstone_recipe.search.fragment.SearchApiRecipeFragment
import com.example.capstone_recipe.search.fragment.SearchRecipeFragment
import com.example.capstone_recipe.search.fragment.SearchUserFragment
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await

class Search : AppCompatActivity() {
    private val binding by lazy { ActivitySearchBinding.inflate(layoutInflater) }
    private lateinit var context: Context
    private lateinit var searchFilterAdapter: SearchFilterAdapter
    private lateinit var searchViewPagerAdapter: SearchViewPagerAdapter
    private var searchFilter = Filter()

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
        if(result.resultCode == Activity.RESULT_OK){
            searchFilter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { result.data?.getSerializableExtra("filter", Filter::class.java)!! }
            else{ result.data?.getSerializableExtra("filter") as Filter }
            searchFilterAdapter.updateList(makeFilterOptionList(searchFilter))
            searchFilterAdapter.filter = searchFilter
        }
    }

    @Suppress("DeferredResultUnused")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        context = binding.root.context

        binding.recyclerviewFilters.apply {
            searchFilterAdapter = SearchFilterAdapter()
            searchFilterAdapter.filter = searchFilter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = searchFilterAdapter
        }

        initTab()

        binding.tabLayout.addTab(binding.tabLayout.newTab(), 0)
        binding.tabLayout.addTab(binding.tabLayout.newTab(), 1)
        binding.tabLayout.addTab(binding.tabLayout.newTab(), 2)
        binding.viewPager.adapter = searchViewPagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager){ tab, position ->
            when (position) {
                0 -> tab.text = "유저"
                1 -> tab.text = "유저 작성글"
                2 -> tab.text = "토시 작성글"
            }
        }.attach()

        binding.btnSetFilter.setOnClickListener {
            val intent = Intent(context, SetFilter::class.java)
            intent.putExtra("filter", searchFilter)
            resultLauncher.launch(intent)
        }

        binding.layerTopPanel.btnBack.setOnClickListener { finish() }

        binding.editSearch.setOnEditorActionListener { _, actionId, _ ->
            if(actionId == EditorInfo.IME_ACTION_SEARCH){
                val searchTarget = binding.editSearch.text.toString()
                if(searchTarget.isNotEmpty()){
                    lifecycleScope.launch(Dispatchers.IO) {
                        async {
                            (searchViewPagerAdapter.fragmentList[0] as SearchUserFragment)
                                .startSearch(searchTarget){ size ->
                                    binding.tabLayout.getTabAt(0)?.text = "유저 \n$size 명"
                                }
                        }
                        async {
                            (searchViewPagerAdapter.fragmentList[1] as SearchRecipeFragment)
                                .startSearch(searchTarget, searchFilter){ size ->
                                    binding.tabLayout.getTabAt(1)?.text = "유저 작성글\n$size 개"
                                }
                        }
                        async {
                            (searchViewPagerAdapter.fragmentList[2] as SearchApiRecipeFragment)
                                .startSearch(searchTarget, searchFilter){ size ->
                                    binding.tabLayout.getTabAt(2)?.text = "토시 작성글\n$size 개"
                                }
                        }
                    }
                }
                else { Toast.makeText(context, "검색어를 입력해주세요.", Toast.LENGTH_SHORT).show() }
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

    /** * 필터 옵션을 상단에 띄움*/
    private fun makeFilterOptionList(filter: Filter): MutableList<String>{
        val filterOptionList = mutableListOf<String>()
        filter.includeIngredient?.let { list ->
            list.forEach { filterOptionList.add("$it 포함") }
        }
        filter.excludeIngredient?.let { list ->
            list.forEach { filterOptionList.add("$it 제외") }
        }
        filter.timeLimit?.let { filterOptionList.add("$it 분") }
        filter.calorieLimit?.let { filterOptionList.add("$it kcal이하") }
        filter.levelLimit?.let { filterOptionList.add("${it.toKor} 난이도") }
        return filterOptionList
    }

    /** * 탭 레이아웃, 뷰페이저 초기화 및 연동*/
    private fun initTab(){
        searchViewPagerAdapter = SearchViewPagerAdapter(this@Search)

        binding.tabLayout.addTab(binding.tabLayout.newTab(), 0)
        binding.tabLayout.addTab(binding.tabLayout.newTab(), 1)
        binding.tabLayout.addTab(binding.tabLayout.newTab(), 2)
        binding.viewPager.adapter = searchViewPagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager){ tab, position ->
            when (position) {
                0 -> tab.text = "유저"
                1 -> tab.text = "유저 작성글"
                2 -> tab.text = "토시 작성글"
            }
        }.attach()
    }
}