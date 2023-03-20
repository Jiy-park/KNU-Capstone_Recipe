package com.example.capstone_recipe

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.capstone_recipe.databinding.ActivityMainBinding
import com.example.capstone_recipe.fragments.*

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private lateinit var context: Context

    private var pressTime = 0L //뒤로가기 키 두번 누르는거
    private val timeInterval = 1000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        context = binding.root.context

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

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean { // 뒤로가기 버튼 액션
        val tempTime = System.currentTimeMillis()
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            if(tempTime - pressTime in 0..timeInterval) { finish() }
            else {
                Toast.makeText(context, "한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()
                pressTime = System.currentTimeMillis()
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
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