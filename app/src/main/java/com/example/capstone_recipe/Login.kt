package com.example.capstone_recipe

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.capstone_recipe.databinding.ActivityLoginBinding
import com.example.capstone_recipe.data_class.User
import com.example.capstone_recipe.data_class.UserLogInInfo
import com.example.capstone_recipe.test_______.TestActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.sothree.slidinguppanel.PanelSlideListener
import com.sothree.slidinguppanel.PanelState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

private const val RHA1 = "123187997955-3muqj5hg18m1lu5kbg6sv4ouodhe5ev6.apps.googleusercontent.com"
class Login : AppCompatActivity() {
    private val db by lazy { Firebase.database("https://knu-capstone-f9f55-default-rtdb.asia-southeast1.firebasedatabase.app/") }
    private val auth by lazy { Firebase.auth }
    private val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }
    private val logInRef by lazy { db.getReference("logIn") }
    private val pref by lazy{ Preference(context) }
    private lateinit var context: Context
    private var pressTime = 0L
    private val timeInterval = 1000L

    private var idCheck = false
    private var pwCheck = false

    private lateinit var googleSignInOptions: GoogleSignInOptions
    private lateinit var googleSignInClient: GoogleSignInClient
    private var gsa: GoogleSignInAccount? = null

    private var isSnsSignUp = false

    private val googleSignInContracts = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            lifecycleScope.launch(Dispatchers.IO) { handleSignInResult(task) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        context =  binding.root.context // 컨텍스트 정의
        binding.slidingLayout.isTouchEnabled = false // 슬라이딩 패널 터치 잠금


        if(pref.checkSetAutoLogInInfo()){ pref.autoLogIn(binding.editSignInID, binding.editSignInPW) }

        setupFocusChangeListeners()
        binding.slidingLayout.addPanelSlideListener(object :PanelSlideListener { // 패널 올라올 때 토끼 같이 올라옴
            override fun onPanelSlide(panel: View, slideOffset: Float) {
                binding.slidingRabbit.translationY =  -(slideOffset * panel.height)
            }
            override fun onPanelStateChanged(panel: View, previousState: PanelState, newState: PanelState) { // 토끼 올라오는 조건
                if(binding.slidingLayout.panelState == PanelState.DRAGGING){
                    if(previousState == PanelState.COLLAPSED) { binding.slidingRabbit.visibility = View.VISIBLE }
                    if(previousState == PanelState.ANCHORED) { binding.slidingRabbit.visibility = View.VISIBLE }
                    if(previousState == PanelState.EXPANDED){ binding.slidingRabbit.visibility = View.GONE }
                }
            }
        })

        binding.btnSignIn.setOnClickListener { // 로그인 버튼
            binding.signIn.visibility = View.VISIBLE
            binding.signUp.visibility = View.GONE
            binding.slidingLayout.panelState = PanelState.EXPANDED
        }

        binding.btnSignUp.setOnClickListener { //1분만에 회원가입 버튼
            binding.signUp.visibility = View.VISIBLE
            binding.signIn.visibility = View.GONE
            binding.slidingLayout.panelState = PanelState.EXPANDED
        }

        binding.tvSlideSignUp.setOnClickListener {// 로그인 패널 안쪽 회원가입 버튼
            binding.slidingLayout.panelState = PanelState.COLLAPSED
            binding.signIn.visibility = View.GONE
            binding.signUp.visibility = View.VISIBLE
            Handler(Looper.getMainLooper()).postDelayed({
                binding.slidingLayout.panelState = PanelState.EXPANDED
            }, 400)
        }

        binding.btnSignUpWithGoogle.setOnClickListener {
            googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(RHA1)
                .requestEmail()
                .build()
            googleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions)
            gsa = GoogleSignIn.getLastSignedInAccount(context)
            if(gsa == null) {
                val intent = googleSignInClient.signInIntent
                googleSignInContracts.launch(intent)
            }
        }

        binding.btnSlideSignIn.setOnClickListener {// 로그인 패널 내 로그인 버튼
            val id = binding.editSignInID.text.toString()
            val pw = binding.editSignInPW.text.toString()
            signIn(id, pw)
        }

        binding.btnSignUpSlide.setOnClickListener {//회원가입 패널 내 가입완료 버튼
            if(idCheck && pwCheck && binding.editSignUpPW.text.isNotEmpty() && binding.editSignUpName.text.isNotEmpty()){
                val id = binding.editSignUpID.text.toString()
                val pw = binding.editSignUpPW.text.toString()
                val name = binding.editSignUpName.text.toString()
                signUp(id, pw, name)
            }
        }
        binding.tvSignInPanel.setOnClickListener { binding.slidingLayout.panelState = PanelState.COLLAPSED }
        binding.tvSignUpPanel.setOnClickListener { binding.slidingLayout.panelState = PanelState.COLLAPSED  }
    }

    private suspend fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val acct = completedTask.getResult(ApiException::class.java)
            acct?.let {
//                firebaseAuthWithGoogle(acct.idToken)
                val personGivenName = acct.givenName?: ""
                val personId = acct.id

                // 현재 구글 로그인한 아이디가 이미 등록된 아이디인지 체크
                val d = db.getReference("googleLogIn").child(personId!!).get().await()
                if(d.value.toString().isEmpty()){
                    Toast.makeText(context, "이미 등록된 아이디 입니다.", Toast.LENGTH_SHORT).show()
                }
                withContext(Dispatchers.Main) { signUpWithSns(personId, personGivenName) }

            }
        } catch (e: ApiException) {
            Log.e("ERROR", "Login :: handleSignInResult() -> 구글 로그인 에러 ${e.statusCode}")
        }
    }

    private fun signUpWithSns(realId: String, defaultName: String){
        isSnsSignUp = true
        binding.signUpWithSns.editSignUpName.setText(defaultName)
        binding.signUpWithSns.editSignUpID.setText(realId)

        binding.slidingLayout.panelState = PanelState.COLLAPSED
        binding.signIn.visibility = View.GONE
        binding.signUp.visibility = View.GONE
        binding.signUpWithSns.root.visibility = View.VISIBLE
        Handler(Looper.getMainLooper()).postDelayed({
            binding.slidingLayout.panelState = PanelState.EXPANDED
        }, 400)

        binding.signUpWithSns.btnSnsSignUpSlide.setOnClickListener {
            val fakeId = binding.signUpWithSns.editSignUpID.text.toString()
            val name = binding.signUpWithSns.editSignUpName.text.toString()
            signUp(fakeId, realId, name, isSnsSignUp = true)
        }
    }

//    private fun firebaseAuthWithGoogle(idToken: String?) {
//        val credential = GoogleAuthProvider.getCredential(idToken, null)
//        auth.signInWithCredential(credential)
//            .addOnCompleteListener(this@Login) { task ->
//                if (task.isSuccessful) {
//                    val user = auth.currentUser
//                    Log.d("LOG_CHECK", "Login :: firebaseAuthWithGoogle() -> user : $user")
//                } else {
//                    Log.e("ERROR", "Login :: firebaseAuthWithGoogle() -> 구글 로그인 실패 : ${task.exception}")
//                }
//            }
//    }

    private fun setupFocusChangeListeners() {
        val editSignUpID = binding.editSignUpID
        val editSignUpPWCheck = binding.editSignUpPWCheck
        val editSignUpPW = binding.editSignUpPW
        val editSignInID = binding.editSignInID
        val editSignInPW = binding.editSignInPW

        val onFocusChanged: (View, Boolean) -> Unit = { view, hasFocus ->
            val slidingRabbit = binding.slidingRabbit
            slidingRabbit.setImageResource(
                when {
                    hasFocus && (view == binding.editSignUpID  || view == editSignInID) ->
                        R.drawable.login_2
                    hasFocus && (view == editSignUpPWCheck || view == editSignUpPW || view == editSignInPW) ->
                        R.drawable.login_3
                    else -> R.drawable.login_1
                }
            )

            if (!hasFocus && view == editSignUpID) {
                checkUserID(editSignUpID.text.toString())
            } else if (!hasFocus && view == editSignUpPWCheck) {
                checkPW()
            }
        }

        editSignUpID.setOnFocusChangeListener(onFocusChanged)
        editSignUpPWCheck.setOnFocusChangeListener(onFocusChanged)
        editSignUpPW.setOnFocusChangeListener(onFocusChanged)
        editSignInID.setOnFocusChangeListener(onFocusChanged)
        editSignInPW.setOnFocusChangeListener(onFocusChanged)
    }


    private fun checkUserID(userID:String){
        val idRef = logInRef.child(userID)
        idRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) { // 아이디 중복됨
                    dataSnapshot.value
                    binding.tvCheckerID.visibility = View.VISIBLE
                    binding.editSignUpID.backgroundTintList = ContextCompat.getColorStateList(context, R.color.main_color_start)
                    idCheck = false
                }
                else { // 중복 아이디 없음
                    binding.tvCheckerID.visibility = View.GONE
                    binding.editSignUpID.backgroundTintList = ContextCompat.getColorStateList(context, R.color.hint_text)
                    idCheck = true
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.d("LOG_CHECK", "Login :: onCancelled() called")
            }
        })
    }

    private fun checkPW(){
        val pw1 = binding.editSignUpPW.text.toString()
        val pw2 = binding.editSignUpPWCheck.text.toString()
        if((pw1 == pw2) && pw1.isNotEmpty() && pw2.isNotEmpty()){ // 제대로 함
            binding.tvCheckerPW.visibility = View.GONE
            binding.editSignUpPW.backgroundTintList = ContextCompat.getColorStateList(context, R.color.hint_text)
            binding.editSignUpPWCheck.backgroundTintList = ContextCompat.getColorStateList(context, R.color.hint_text)
            pwCheck = true
        }
        else{ // 제대로 안함
            binding.tvCheckerPW.visibility = View.VISIBLE
            binding.editSignUpPW.backgroundTintList = ContextCompat.getColorStateList(context, R.color.main_color_start)
            binding.editSignUpPWCheck.backgroundTintList = ContextCompat.getColorStateList(context, R.color.main_color_start)
            pwCheck = false
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean { // 뒤로가기 버튼 액션
        val tempTime = System.currentTimeMillis()
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            if(binding.slidingLayout.panelState == PanelState.EXPANDED) { binding.slidingLayout.panelState = PanelState.COLLAPSED }
            else if(binding.slidingLayout.panelState == PanelState.COLLAPSED)  {
                if(tempTime - pressTime in 0..timeInterval) { finish() }
                else {
                    Toast.makeText(context, "한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()
                    pressTime = System.currentTimeMillis()
                }
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private  fun signIn(id:String, pw:String, isSns: Boolean = false){
        if(isSns){
            db.getReference("googleLogIn").child(pw).get().addOnSuccessListener {
                val fakeId = it.value.toString()
                if(pref.getUseAutoLogIn()){ pref.saveAutoLogInInfo(id,pw) }
                moveToMain(fakeId)
            }
        }
        else{
            val idRef = logInRef.child(id)
            idRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        if(pw == dataSnapshot.child("pw").value){ // 로그인 성공
                            if(pref.getUseAutoLogIn()){ pref.saveAutoLogInInfo(id,"") }
                            moveToMain(id)
                        }
                        else{ // 비밀번호 틀림
                            binding.tvCheckerIDPW.visibility = View.VISIBLE
                            binding.editSignInID.backgroundTintList = ContextCompat.getColorStateList(context, R.color.main_color_start)
                            binding.editSignInPW.backgroundTintList = ContextCompat.getColorStateList(context, R.color.main_color_start)
                        }
                    }
                    else{ // 아이디 없음
                        binding.tvCheckerIDPW.visibility = View.VISIBLE
                        binding.editSignInID.backgroundTintList = ContextCompat.getColorStateList(context, R.color.main_color_start)
                        binding.editSignInPW.backgroundTintList = ContextCompat.getColorStateList(context, R.color.main_color_start)
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.d("LOG_CHECK", "Login :: onCancelled() called $error 2")
                }
            })
        }
    }


    private fun signUp(id:String, pw:String, name:String, isSnsSignUp: Boolean = false){
        if(isSnsSignUp) {
            db.getReference("googleLogIn").child(pw).setValue(id)
            // googleLogIn
            //       |- realId(pw) : fakeId(id)
        }
        else {
            db.getReference("logIn").child(id).setValue(UserLogInInfo(id, pw))
        }
        db.getReference("users")
            .child(id)                              // 로그인 정보 업로드 성공 시 유저 기본 정보 db 업로드
            .setValue(User(
                id = id,
                name = name,
                score = 0,
                profileImagePath = "",
                backgroundImagePath = "",
                recentRecipe = "",
                friends = mutableListOf(),
                uploadRecipe = mutableListOf(),
                saveRecipe = mutableListOf()
            ))
            .addOnCompleteListener {
                Toast.makeText(context, "환영해요!", Toast.LENGTH_SHORT).show()
                pref.setUseTTS() // tts 사용
                pref.setUseSTT() // stt 사용
                pref.setUseCloudMsg()
                pref.setResponsiveness(8)
                pref.setSpeakSpeed(2)
                pref.setVoiceTone(2)
                if(isSnsSignUp) { signIn(id, pw, isSns = true) }
                else { signIn(id, pw) }
            }
    }

    private fun moveToMain(id:String){ // 로그인
        val intent = Intent(binding.root.context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

}

