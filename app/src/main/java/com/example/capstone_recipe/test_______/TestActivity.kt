package com.example.capstone_recipe.test_______

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.capstone_recipe.databinding.ActivityTestBinding
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.util.*
import kotlin.math.log

class TestActivity : AppCompatActivity() {
    val binding by lazy { ActivityTestBinding.inflate(layoutInflater) }
    private lateinit var context: Context
    private val storage = FirebaseStorage.getInstance()
    private val db = Firebase.database("https://knu-capstone-f9f55-default-rtdb.asia-southeast1.firebasedatabase.app/")
    lateinit var tts: TextToSpeech

    private fun initTTS(){
        tts = TextToSpeech(binding.root.context) { state ->
            if (state == TextToSpeech.SUCCESS) {
                //사용할 언어를 설정
                val result = tts.setLanguage(Locale.KOREA);
                //언어 데이터가 없거나 혹은 언어가 지원하지 않으면...
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(binding.root.context, "이 언어는 지원하지 않습니다.", Toast.LENGTH_SHORT)
                        .show();
                } else {
                    //음성 톤
                    tts.setPitch(2.0f);
                    //읽는 속도
                    tts.setSpeechRate(2.0f);

                }
            }
        }
        val d = object: UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                Log.d("LOG_CHECK", "TestActivity :: onStart() -> 11")
            }

            override fun onDone(utteranceId: String?) {
            }

            override fun onError(utteranceId: String?) {
                Log.d("LOG_CHECK", "TestActivity :: onError() -> ")
            }

            override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                super.onRangeStart(utteranceId, start, end, frame)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        context = binding.root.context
        initTTS()
        binding.btnStart.setOnClickListener {
            tts.speak(binding.textView.text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
        binding.btnStop.setOnClickListener {
            if(tts.isSpeaking){
                tts.stop()
            }
        }
        binding.btnRestart.setOnClickListener {
            tts.speak(binding.textView.text, TextToSpeech.QUEUE_ADD, null, null)
        }
    }
}
