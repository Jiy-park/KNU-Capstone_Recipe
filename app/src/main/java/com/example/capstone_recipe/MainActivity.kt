package com.example.capstone_recipe

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import com.example.capstone_recipe.databinding.ActivityMainBinding
import com.example.capstone_recipe.dialog.DialogFunc
import com.example.capstone_recipe.recipe_locker.RecipeLocker

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private lateinit var context: Context
    private var pressTime = 0L //뒤로가기 키 두번 누르는거
    private val timeInterval = 1000L

    private fun testFunction(){ // 테스트 용
        binding.tvTop.setOnClickListener {
            val intent = Intent(context, PostViewer::class.java)
            intent.putExtra("recipeId", "20230412105019_q")
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        context = binding.root.context

        testFunction() // 테스트용

        binding.tvSearchTrigger.setOnClickListener {
            Toast.makeText(context, "!!", Toast.LENGTH_SHORT).show()
        }

        binding.ivRecipeLocker.setOnClickListener {
            val intent = Intent(context, RecipeLocker::class.java)
            startActivity(intent)
        }

        binding.ivRecipeCreate.setOnClickListener {
            val intent = Intent(context, RecipeCreate::class.java)
            startActivity(intent)
        }

        binding.ivUserInfo.setOnClickListener {
            val intent = Intent(context, RecipeLocker::class.java)
            startActivity(intent)
        }

        binding.ivSetting.setOnClickListener {
            DialogFunc.settingDialog(context)
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
}