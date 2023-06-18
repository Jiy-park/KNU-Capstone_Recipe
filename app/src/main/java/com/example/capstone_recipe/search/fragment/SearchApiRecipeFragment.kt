package com.example.capstone_recipe.search.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.capstone_recipe.R
import com.example.capstone_recipe.data_class.Filter
import com.example.capstone_recipe.databinding.FragmentSearchTabViewerBinding
import com.example.capstone_recipe.search.SearchApiRecipe
import com.example.capstone_recipe.search.adapter.SearchApiRecipeAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchApiRecipeFragment : Fragment() {
    private var binding: FragmentSearchTabViewerBinding? = null
    private lateinit var context: Context
    private var searchApiRecipeAdapter = SearchApiRecipeAdapter()

    private var isSearching = false
    private var noResult = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSearchTabViewerBinding.inflate(inflater, container, false)
        context = binding!!.root.context

        Glide.with(context)
            .load(R.drawable.progress)
            .into(binding!!.ivProgressImage)

        binding!!.recyclerview.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = searchApiRecipeAdapter
        }

        lifecycleScope.launch { updateSearchingView(isSearching, noResult) }
        return binding!!.root
    }

    suspend fun startSearch(searchTarget: String, searchFilter: Filter, callback: (size: Int) -> Unit) = withContext(Dispatchers.IO){
        updateSearchingView(_isSearching = true)
        SearchApiRecipe().searchFromApi(searchTarget, searchFilter){ recipeList, recipeBasicInfoList ->
            lifecycleScope.launch(Dispatchers.Main) {
                searchApiRecipeAdapter.updateAdapter(recipeList, recipeBasicInfoList)
                updateSearchingView(_isSearching = false, _noResult = recipeList.isEmpty())
                callback(recipeList.size)
            }
        }
    }

    private suspend fun updateSearchingView(_isSearching: Boolean, _noResult: Boolean = false) = withContext(Dispatchers.Main){
        binding?.let {
            if(_isSearching) {
                it.recyclerview.visibility = View.GONE
                it.tvResultSearch.visibility = View.GONE
                it.progress.visibility = View.VISIBLE
            }
            else {
                it.progress.visibility = View.GONE
                it.recyclerview.visibility = View.VISIBLE
                if(_noResult) { it.tvResultSearch.visibility = View.VISIBLE }
                else { it.tvResultSearch.visibility = View.GONE }
            }
        }?: run{
            isSearching = _isSearching
            noResult = _noResult
        }
    }
}