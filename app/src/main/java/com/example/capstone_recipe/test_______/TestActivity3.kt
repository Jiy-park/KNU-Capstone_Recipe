package com.example.capstone_recipe.test_______

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.capstone_recipe.databinding.ActivityTest3Binding

class TestActivity3 : AppCompatActivity() {
    private val binding by lazy { ActivityTest3Binding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val d = Uri.EMPTY
        if(d.toString().startsWith("https://")){
            binding.tv.text = "ok"
        }
        else {
            binding.tv.text = "no"
        }
    }
}