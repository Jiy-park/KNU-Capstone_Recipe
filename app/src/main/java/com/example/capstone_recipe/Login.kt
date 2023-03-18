package com.example.capstone_recipe

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.capstone_recipe.databinding.ActivityLoginBinding
import com.example.capstone_recipe.dbSchema.User
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.sothree.slidinguppanel.PanelState


class Login : AppCompatActivity() {
    private val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }
    private val DB by lazy { Firebase.database("https://knu-capstone-f9f55-default-rtdb.asia-southeast1.firebasedatabase.app/") }
    private val usersRef by lazy { DB.getReference("users") }
    private lateinit var context: Context

    private var pressTime = 0L
    private val timeInterval = 1000L
    private var idCheck = false
    private var pwCheck = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        context =  binding.root.context // 컨텍스트 정의

        binding.slidingLayout.isTouchEnabled = false // 슬라이딩 패널 터치 잠금

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

        binding.editSignUpID.setOnFocusChangeListener { _, hasFocus -> // 회원가입 아이디 중복 체크
            if(!hasFocus) { checkUserID(binding.editSignUpID.text.toString()) }
        }

        binding.editSignUpPWCheck.setOnFocusChangeListener { _, hasFocus -> // 비밀번호 입력 확인
            if(!hasFocus) { checkPW() }
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

    private fun checkUserID(userID:String){
        val idRef = usersRef.child(userID)
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
        if(pw1 == pw2){ // 제대로 함
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

    private  fun signIn(id:String, pw:String){
        val idRef = usersRef.child(id)
        idRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    if(pw == dataSnapshot.child("pw").value){
                        Toast.makeText(context, "로그인!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    else{
                        binding.tvCheckerIDPW.visibility = View.VISIBLE
                        binding.editSignInID.backgroundTintList = ContextCompat.getColorStateList(context, R.color.main_color_start)
                        binding.editSignInPW.backgroundTintList = ContextCompat.getColorStateList(context, R.color.main_color_start)
                    }
                }
                else{
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

    private fun signUp(id:String, pw:String, name:String){
        usersRef.child(id).setValue(User(id, pw, name))
        Toast.makeText(context, "환영해요!", Toast.LENGTH_SHORT).show()
        signIn(id, pw)
    }

}

