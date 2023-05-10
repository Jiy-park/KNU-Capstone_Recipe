package com.example.capstone_recipe.test_______

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.capstone_recipe.databinding.ActivityTestBinding
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.log

class TestActivity : AppCompatActivity() {
    val binding by lazy { ActivityTestBinding.inflate(layoutInflater) }
    private lateinit var context: Context
    private val storage = FirebaseStorage.getInstance()
    private val db = Firebase.database("https://knu-capstone-f9f55-default-rtdb.asia-southeast1.firebasedatabase.app/")



    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        context = binding.root.context

        binding.btn.setOnClickListener {
            lifecycleScope.launch {
                db.getReference("users").child("q").child("friends").push().child("id").setValue("퍼팬")
            }
        }

    }
}
