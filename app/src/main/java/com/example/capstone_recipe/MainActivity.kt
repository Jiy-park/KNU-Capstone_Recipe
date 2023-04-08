package com.example.capstone_recipe

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.capstone_recipe.databinding.ActivityMainBinding
import com.example.capstone_recipe.dialog.DIALOG_SIZE
import com.example.capstone_recipe.dialog.DeveloperInfo
import com.example.capstone_recipe.dialog.DialogFunc
import com.example.capstone_recipe.dialog.DialogInterface
import com.example.capstone_recipe.test_______.TestActivity

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private lateinit var context: Context

    private var pressTime = 0L //뒤로가기 키 두번 누르는거
    private val timeInterval = 1000L

    private fun testFunction(){ // 테스트 용
        binding.topPanel.tvTopTitle.setOnClickListener {
            val intent = Intent(context, TestActivity::class.java)
            startActivity(intent)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        context = binding.root.context


        testFunction() // 테스트용



        binding.layerCreate.groupCreate.setOnClickListener {
            Toast.makeText(context, "create", Toast.LENGTH_SHORT).show()
        }
        binding.topPanel.btnSetting.setOnClickListener {
            DialogFunc.settingDialog(context)
        }
        binding.layerLocker.groupLocker.setOnClickListener {
            val intent = Intent(context, RecipeLocker::class.java)
            startActivity(intent)
        }
        binding.layerCreate.groupCreate.setOnClickListener {
            val intent = Intent(context, RecipeCreate::class.java)
            startActivity(intent)
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