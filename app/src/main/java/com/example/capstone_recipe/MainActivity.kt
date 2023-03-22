package com.example.capstone_recipe

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.capstone_recipe.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private lateinit var context: Context

    private var pressTime = 0L //뒤로가기 키 두번 누르는거
    private val timeInterval = 1000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        context = binding.root.context

        binding.groupCreate.setOnClickListener {
            Toast.makeText(context, "create", Toast.LENGTH_SHORT).show()
        }
        binding.groupLocker.setOnClickListener {
            Toast.makeText(context, "locker", Toast.LENGTH_SHORT).show()
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