package com.example.capstone_recipe.test_______

import android.R
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.capstone_recipe.databinding.ActivityTest2Binding
import java.util.*


class TestActivity2 : AppCompatActivity() {
    private var speechRecognizer: SpeechRecognizer? = null
    private var speechRecognizerIntent: Intent? = null
    private val binding by lazy { ActivityTest2Binding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        startStt()
    }

    private fun startStt() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(applicationContext)
        speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechRecognizerIntent!!.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        speechRecognizerIntent!!.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        speechRecognizerIntent!!.putExtra(RecognizerIntent.EXTRA_PROMPT, "말하세요")
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle) {
                Log.d("LOG_CHECK", "onReadyForSpeech")
            }

            override fun onBeginningOfSpeech() {
                Log.d("LOG_CHECK", "onBeginningOfSpeech")
            }

            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray) {}
            override fun onEndOfSpeech() {
                Log.d("LOG_CHECK", "onEndOfSpeech")
            }

            override fun onError(error: Int) {
                Log.d("LOG_CHECK", "onError: $error")
                // 인식이 실패한 경우 다시 인식을 시작합니다.
                speechRecognizer?.startListening(speechRecognizerIntent)
            }

            override fun onResults(results: Bundle) {
                Log.d("LOG_CHECK", "onResults")
                val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (matches != null && matches.isNotEmpty()) {
                    val text = matches[0]
                    binding.tvResult.text = text
                    if (text.contains("다시") && text.contains("토킹")) {
                        binding.tvResult.text = "다시시작"
                    }
                    if (text.contains("뭐라고")) {
                        binding.tvResult.text = "다시시작"
                    }
                }
                // 인식이 끝난 후 다시 인식을 시작합니다.
                speechRecognizer?.startListening(speechRecognizerIntent)
            }

            override fun onPartialResults(partialResults: Bundle) {}
            override fun onEvent(eventType: Int, params: Bundle) {}
        })
        // 음성 인식 시작
        speechRecognizer?.startListening(speechRecognizerIntent)
    }

    override fun onPause() {
        super.onPause()
        if (speechRecognizer != null) {
            speechRecognizer!!.stopListening()
        }
    }

    override fun onResume() {
        super.onResume()
        if (speechRecognizer != null) {
            speechRecognizer!!.startListening(speechRecognizerIntent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (speechRecognizer != null) {
            speechRecognizer!!.destroy()
        }
    }
}