package com.example.capstone_recipe.search.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.capstone_recipe.search.fragment.SearchApiRecipeFragment
import com.example.capstone_recipe.search.fragment.SearchRecipeFragment
import com.example.capstone_recipe.search.fragment.SearchUserFragment

class SearchViewPagerAdapter(fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity) {
    val fragmentList = listOf<Fragment>(
        SearchUserFragment(),
        SearchRecipeFragment(),
        SearchApiRecipeFragment()
    )

    override fun getItemCount() = 3

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> return fragmentList[position]
            1 -> return fragmentList[position]
            2 -> return fragmentList[position]
        }
        return SearchUserFragment()
    }
}