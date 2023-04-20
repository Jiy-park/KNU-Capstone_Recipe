package com.example.capstone_recipe.recipe_locker.locker_adpater

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.capstone_recipe.data_class.User
import com.example.capstone_recipe.recipe_locker.locker_fragment.RecipeLockerFriendList
import com.example.capstone_recipe.recipe_locker.locker_fragment.RecipeLockerSaveList
import com.example.capstone_recipe.recipe_locker.locker_fragment.RecipeLockerUploadList

class LockerViewPagerAdapter(info: User, fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity) {
    private var userInfo = info
    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> return RecipeLockerUploadList(userInfo)
            1 -> return RecipeLockerFriendList(userInfo)
            2 -> return RecipeLockerSaveList(userInfo)
        }
        return RecipeLockerUploadList(userInfo)
    }

    override fun getItemCount() = 3
}