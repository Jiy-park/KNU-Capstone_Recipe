package com.example.capstone_recipe.test_______

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.capstone_recipe.databinding.ActivityTest4Binding
import java.util.*
import kotlin.collections.ArrayList


class TestActivity4 : AppCompatActivity() {
    private var stt: SpeechRecognizer? = null
    private var sttIntent: Intent? = null
    private val binding by lazy { ActivityTest4Binding.inflate(layoutInflater) }

    private lateinit var galleryLauncher: ActivityResultLauncher<String>
    private var image = Uri.EMPTY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()){
            image = it
//            binding.iv.setImageURI(image)
        }

        binding.btn.setOnClickListener {
            galleryLauncher.launch("image/*")
        }
        binding.upload.setOnClickListener {
            Log.d("LOG_CHECK", "TestActivity4 :: onCreate() -> image : $image")
            val bitmapFactory = BitmapFactory.decodeFile(image.toString())
            Log.d("LOG_CHECK", "TestActivity4 :: onCreate() -> bitmapFactory : $bitmapFactory")
            binding.iv.setImageBitmap(bitmapFactory)
        }
    }

}