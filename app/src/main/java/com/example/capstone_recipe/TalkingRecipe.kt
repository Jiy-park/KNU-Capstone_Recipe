package com.example.capstone_recipe

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.example.capstone_recipe.data_class.RecipeStep
import com.example.capstone_recipe.databinding.ActivityTalkingRecipeBinding
import com.example.capstone_recipe.dialog.DialogFunc
import com.example.capstone_recipe.recipe_locker.RecipeLocker
import com.example.capstone_recipe.talking_recipe_fragment.TalkingRecipeStep
import java.util.*

class TalkingRecipe : AppCompatActivity() {
    private val binding by lazy { ActivityTalkingRecipeBinding.inflate(layoutInflater) }
    private lateinit var context: Context
    private val defaultImage = Uri.parse("android.resource://$packageName/${R.drawable.default_recipe_main_image}")

    private var tts: TextToSpeech? = null
    private var stt: SpeechRecognizer? = null
    private var sttIntent: Intent? = null
    private var sttResponsiveness: Float? = null

    private var from = ""
    private var recipeTitle = ""
    private var recipeCreator = ""
    private var creatorProfileImageUri = Uri.EMPTY
    private var recipeStepList = listOf<RecipeStep>()
    private var recipeStepImageUriList = listOf<Uri>()

    private var currentStep = -1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        context = binding.root.context

        if(Preference(context).getUseTTS()){ initTTS() }

        if(Preference(context).getUseSTT()){
            initSTT(tts)
            stt?.startListening(sttIntent)
        }

        getRecipeInfoFromIntent()                           // 인텐트로부터 레시피 정보 받아옴
        setView()                                           // 레시피 정보에 맞게 뷰 설정
        setViewEvent()                                      // 뷰 이벤트 정의
        changeFragment(isNext = true)                       // 프래그먼트 전환\

    }

    /** * 인텐트로 넘어온 레시피 정보를 받아서 정리*/
    private fun getRecipeInfoFromIntent(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            recipeStepList = intent.getParcelableArrayListExtra("recipeStepList", RecipeStep::class.java)?: emptyList()
            recipeStepImageUriList = intent.getParcelableArrayListExtra("recipeStepImageList", Uri::class.java)?: emptyList()
        }
        else{
            recipeStepList = intent.getParcelableArrayListExtra<RecipeStep>("recipeStepList")?: emptyList()
            recipeStepImageUriList = intent.getParcelableArrayListExtra<Uri>("recipeStepImageList")?: emptyList()
        }

        from = intent.getStringExtra("from")?: ""
        recipeTitle = intent.getStringExtra("recipeTitle")?: ""
        recipeCreator = intent.getStringExtra("creator")?: ""
        creatorProfileImageUri = intent.getStringExtra("creatorProfileImageUri")?.toUri() ?: defaultImage
        intent.extras?.clear()
    }

    /** * 레시피 정보를 바탕으로 뷰 세팅*/
    private fun setView(){
        Glide.with(context)// 유저 프로필 이미지
            .load(creatorProfileImageUri)
            .error(R.drawable.default_user_profile_image)
            .circleCrop()
            .into(binding.layerRecipeTitle.ivUserProfileImage)

        binding.layerRecipeTitle.tvPostTitle.text = recipeTitle
        binding.layerRecipeTitle.tvPostCreator.text = recipeCreator
    }

    /** * 각 뷰들의 이벤트 정의*/
    private fun setViewEvent() = with(binding){
        layerTopPanel.btnBack.setOnClickListener { finish() }
        btnNext.setOnClickListener { changeFragment(isNext = true) }
        btnPrev.setOnClickListener { changeFragment(isNext = false) }
        btnDone.setOnClickListener { finish() }
        ivToxi.setOnClickListener {
            DialogFunc.settingToxiDialog(context){ speed, tone, responsiveness ->
                tts?.setPitch(tone.toFloat())
                tts?.setSpeechRate(speed.toFloat())
                sttResponsiveness = responsiveness.toFloat()
            }
        }

        if(from == "user"){
            layerRecipeTitle.ivUserProfileImage.setOnClickListener {
                val intent = Intent(context, RecipeLocker::class.java)
                intent.putExtra("lockerOwnerId", recipeCreator.split("@")[1])
                context.startActivity(intent)
            }
        }
    }

//    /** * step 단계로 바로 넘어감*/
//    private fun jumpStepTo(step: Int){
//        if(currentStep == step) { return }
//        val toNext = currentStep < step // 현재 단계가 점프 단계 보다 작을 시 다음단계로 이동 , 클 시 이전 단계로 이동
//        currentStep = step
//        changeFragment(toNext, currentStep)
//
//    }

    /** * 다음 or 이전 버튼 클릭 시 프래그먼트 전환 이벤트 정의*/
    private fun changeFragment(isNext: Boolean, jumpTo: Int? = null){
        if(isNext){
            currentStep++
            val stepExplanation = recipeStepList[currentStep].explanation
            val stepImageUri =
                if(from == "user") { recipeStepImageUriList[currentStep] }
                else { recipeStepList[currentStep].imagePath.toUri() }
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.animation_enter_from_right,
                    R.anim.animation_exit_to_left,
                    R.anim.animation_enter_from_left,
                    R.anim.animation_exit_to_right
                )
                .replace(R.id.mainFrame, TalkingRecipeStep(currentStep, stepExplanation, stepImageUri))
                .commit()
        }
        else{
            if(currentStep == 0) { finish() }
            else {
                currentStep--
                val stepExplanation = recipeStepList[currentStep].explanation
                val stepImageUri =
                    if(from == "user") { recipeStepImageUriList[currentStep] }
                    else { recipeStepList[currentStep].imagePath.toUri() }
                supportFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        R.anim.animation_enter_from_left,
                        R.anim.animation_exit_to_right,
                        R.anim.animation_enter_from_right,
                        R.anim.animation_exit_to_left
                    )
                    .replace(R.id.mainFrame, TalkingRecipeStep(currentStep, stepExplanation, stepImageUri))
                    .commit()
            }
        }
        refreshView(currentStep)
        speakCurrentStep(currentStep)
    }

    /** * 현재 프래그먼트에 보여지는 단계에 맞게 뷰(다음 or 이전 버튼) 설정*/
    private fun refreshView(currentStep: Int) = with(binding){
        Log.d("LOG_CHECK", "TalkingRecipe :: refreshView() -> current : $currentStep")
        if(currentStep == recipeStepList.size-1) {
            btnNext.visibility = View.GONE
            btnPrev.visibility = View.GONE
            btnDone.visibility = View.VISIBLE
        }
        else {
            when(currentStep){
                0 -> {
                    btnNext.visibility = View.VISIBLE
                    btnPrev.visibility = View.GONE
                    btnDone.visibility = View.GONE
                }
                in 1 until recipeStepList.size-1 -> {
                    btnNext.visibility = View.VISIBLE
                    btnPrev.visibility = View.VISIBLE
                    btnDone.visibility = View.GONE
                }
                recipeStepList.size-1 -> {
                    btnNext.visibility = View.GONE
                    btnPrev.visibility = View.VISIBLE
                    btnDone.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            changeFragment(isNext = false)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    /** * 유저가 tts 설정을 사용으로 햇을 시 실행. */
    private fun initTTS(){
        tts = TextToSpeech(context){
            if(it == TextToSpeech.SUCCESS){
                val result = tts?.setLanguage(Locale.KOREA)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(binding.root.context, "지원하지 않는 언어입니다.", Toast.LENGTH_SHORT).show()
                }
                else {
                    tts?.setPitch(Preference(context).getVoiceTone().toFloat());
                    tts?.setSpeechRate(Preference(context).getSpeakSpeed().toFloat());
                }
            }
        }
    }

    private fun speakCurrentStep(currentStep: Int){
        tts?.speak(recipeStepList[currentStep].explanation, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    /** *stt */
    private fun initSTT(tts: TextToSpeech?) {
        stt = SpeechRecognizer.createSpeechRecognizer(binding.root.context)
        sttIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        sttResponsiveness = Preference(context).getResponsiveness().toFloat()

        sttIntent?.let {
            it.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            it.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            it.putExtra(RecognizerIntent.EXTRA_PROMPT, "말하세요")
        }

        stt?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle) {
                Log.d("LOG_CHECK", "TalkingRecipe :: onReadyForSpeech() -> 음성인식 준비")
            }

            override fun onBeginningOfSpeech() {
                Log.d("LOG_CHECK", "TalkingRecipe :: onBeginningOfSpeech() -> 음성인식 시작")
            }

            override fun onRmsChanged(rmsdB: Float) { // 대충 7.5 ~ 8.0??
                if(rmsdB >= sttResponsiveness!!) {
                    // 토시 이미지 변경 : 기본 -> 느낌표 이미지 (음성 인식 시작)
                    binding.tvToxiText.text = "듣고 있어요!"
                }
            }
            override fun onBufferReceived(buffer: ByteArray) {}
            override fun onEndOfSpeech() {
                binding.tvToxiText.text = "..."
            }

            override fun onError(error: Int) {
                Log.d("LOG_CHECK", "TalkingRecipe :: onError() -> 에러")
                when(error){
                    in 1..2 -> { Log.d("LOG_CHECK", "TestActivity4 :: onError() -> 네트워크 오류") }
                    3 -> { Log.d("LOG_CHECK", "TestActivity4 :: onError() -> 오디오 오류")}
                    4 -> { Log.d("LOG_CHECK", "TestActivity4 :: onError() -> 서버 오류") }
                    5 -> { Log.d("LOG_CHECK", "TestActivity4 :: onError() -> 클라이언트 오류") }
                    6 -> { Log.d("LOG_CHECK", "TestActivity4 :: onError() -> 음성인식 타임 아웃") }
                    7 -> {
                        Log.d("LOG_CHECK", "TestActivity4 :: onError() -> 매치된 인식 없음 -> 다시 실행")
                        binding.tvToxiText.text = "다시 말해 주세요!"
                        stt?.startListening(sttIntent)
                    }
                    8 -> {
                        Log.d("LOG_CHECK", "TestActivity4 :: onError() -> 현재 작업중.. 대기 바람")
                        binding.tvToxiText.text = "잠시만 기다려 주세요!"
                    }
                    9 -> { Log.d("LOG_CHECK", "TestActivity4 :: onError() -> 권한 부족 권한 요청") }
                }
            }

            override fun onResults(results: Bundle) {
                Log.d("LOG_CHECK", "TalkingRecipe :: onResults() -> 결과 반납")
                val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                Log.d("LOG_CHECK", "TalkingRecipe :: onResults() -> 결과 : $matches")

                if (matches != null && matches.isNotEmpty()) {
                    val recognizedText = matches[0]
//                    if(callToxi.any { word -> recognizedText.contains(word) }){
                        if(callToxiForNextStep.any { word -> recognizedText.contains(word) }) { changeFragment(isNext = true) }
                        if(callToxiForPrevStep.any { word -> recognizedText.contains(word) }) { changeFragment(isNext = false) }
                        if(callToxiForStopTTS.any { word -> recognizedText.contains(word) }) { tts?.stop() }
                        if(callToxiForStartTTS.any { word -> recognizedText.contains(word) }) { speakCurrentStep(currentStep) }
//                    }
                }
                stt?.startListening(sttIntent)
            }

            override fun onPartialResults(partialResults: Bundle) {}
            override fun onEvent(eventType: Int, params: Bundle) {}
        })
    }

    override fun onPause() {
        super.onPause()
        tts?.stop()
        stt?.stopListening()
    }

    override fun onResume() {
        super.onResume()
        stt?.startListening(sttIntent)
    }
    override fun onDestroy() {
        super.onDestroy()
        tts?.stop()
        stt?.destroy()
    }

    /** * 음성인식 시 토시를 부르면 실행 -> 토시를 구별하기 위한 리스트*/
    private val callToxi = listOf<String>(
        "토시", "도시", "2시", "두시", "투시"
    )

    /** * 음성인식 시 다음 단계로 넘어가기*/
    private val callToxiForNextStep = listOf<String>(
        "다음", "앞으로", "오케이", "다운", "다움"
    )

    /** * 음성인식 시 이전 단계로 넘어가기*/
    private val callToxiForPrevStep = listOf<String>(
        "이전", "뒤로", "돌아", "도라"
    )

    /** * 음성인식 시 tts 정지*/
    private val callToxiForStopTTS = listOf<String>(
        "잠깐", "잠만", "스탑", "정지", "멈춰",
    )

    /** * 음성인식 시 tts 재생*/
    private val callToxiForStartTTS = listOf<String>(
        "시작", "재생", "진행", "계속",
    )

}