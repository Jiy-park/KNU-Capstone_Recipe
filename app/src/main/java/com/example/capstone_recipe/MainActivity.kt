package com.example.capstone_recipe

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.capstone_recipe.databinding.ActivityMainBinding
import com.example.capstone_recipe.fragments.*

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        supportFragmentManager
            .beginTransaction()
            .add(R.id.mainFrame, MainFragment())
            .commit()


        // 하단 네비게이션 바 클릭 이벤트 설정
        binding.bottomNavi.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.itemMain -> {
                    replaceFragment(MainFragment())
                    true
                }
                R.id.itemRecipe -> {
                    replaceFragment(RecipeFragment())
                    true
                }
                R.id.itemProfile -> {
                    replaceFragment(ProfileFragment())
                    true
                }
                R.id.itemFriend -> {
                    replaceFragment(FriendsFragment())
                    true
                }
                R.id.itemSetting -> {
                    replaceFragment(SettingFragment())
                    true
                }
                else -> false
            }
        }
    }

    // 화면 전환 구현 메소드
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.mainFrame, fragment)
            .commit()
    }
}