package com.example.capstone_recipe.dialog

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.capstone_recipe.databinding.DeveloperInfoBinding

class DeveloperInfo : AppCompatActivity() {
    val binding by lazy { DeveloperInfoBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.btn.setOnClickListener {
            finish()
        }
    }
}