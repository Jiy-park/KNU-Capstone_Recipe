package com.example.capstone_recipe.recipe_locker.locker_adpater

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.capstone_recipe.recipe_locker.locker_fragment.RecipeLockerFriendList
import com.example.capstone_recipe.recipe_locker.locker_fragment.RecipeLockerSaveList
import com.example.capstone_recipe.recipe_locker.locker_fragment.RecipeLockerUploadList
import com.google.firebase.database.DataSnapshot

class LockerViewPagerAdapter(private val user: DataSnapshot?, fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity) {
    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> return RecipeLockerUploadList(user)
            1 -> return RecipeLockerFriendList(user)
            2 -> return RecipeLockerSaveList(user)
        }
        return RecipeLockerUploadList(user)
    }

    override fun getItemCount() = 3
}