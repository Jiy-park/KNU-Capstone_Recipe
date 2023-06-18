package com.example.capstone_recipe.test_______

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.capstone_recipe.databinding.ActivityTest1Binding
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class Test1 : AppCompatActivity() {
    private val binding by lazy { ActivityTest1Binding.inflate(layoutInflater) }

    private val db = Firebase.database

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
    }
}