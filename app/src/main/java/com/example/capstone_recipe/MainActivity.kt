package com.example.capstone_recipe

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.capstone_recipe.databinding.ActivityMainBinding
import com.example.capstone_recipe.fragments.*

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    var isLogin = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        if(!isLogin) { requestSignIn() }

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

    private fun requestSignIn(){ // 로그인
        val intent = Intent(binding.root.context, Login::class.java)
        startActivity(intent)
    }

    // 화면 전환 구현 메소드
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.mainFrame, fragment)
            .commit()
    }

    fun replaceFragment(fragmentIndex:FRAGMENTS) {
        when(fragmentIndex){
            FRAGMENTS.MAIN->supportFragmentManager
                .beginTransaction()
                .replace(R.id.mainFrame, MainFragment())
                .commit()
            FRAGMENTS.RECIPE->supportFragmentManager
                .beginTransaction()
                .replace(R.id.mainFrame, RecipeFragment())
                .commit()
            FRAGMENTS.PROFILE->supportFragmentManager
                .beginTransaction()
                .replace(R.id.mainFrame, ProfileFragment())
                .commit()
            FRAGMENTS.FRIENDS->supportFragmentManager
                .beginTransaction()
                .replace(R.id.mainFrame, FriendsFragment())
                .commit()
            FRAGMENTS.SETTING->supportFragmentManager
                .beginTransaction()
                .replace(R.id.mainFrame, SettingFragment())
                .commit()
        }
    }

    enum class FRAGMENTS{
        MAIN,
        RECIPE,
        PROFILE,
        FRIENDS,
        SETTING
    }
}