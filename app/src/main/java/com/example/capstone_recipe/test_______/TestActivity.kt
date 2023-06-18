package com.example.capstone_recipe.test_______

import android.R
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.capstone_recipe.databinding.ActivityTestBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

/** * 구글 로그인 */
private const val RHA1 = "123187997955-3muqj5hg18m1lu5kbg6sv4ouodhe5ev6.apps.googleusercontent.com"
class TestActivity : AppCompatActivity() {

    private val binding by lazy { ActivityTestBinding.inflate(layoutInflater) }
    // 구글api클라이언트
    private var mGoogleSignInClient: GoogleSignInClient? = null

    // 구글 계정
    private var gsa: GoogleSignInAccount? = null

    // 파이어베이스 인증 객체 생성
    private var mAuth: FirebaseAuth? = null

    val d = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode == RESULT_OK) {
            Log.d("LOG_CHECK", "TestActivity :: signIn() -> data : ${it.data}")
            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            handleSignInResult(task)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val 한글변수 = "이게 되네????"
        Toast.makeText(binding.root.context, "$한글변수", Toast.LENGTH_SHORT).show()
        Firebase.auth

        // 파이어베이스 인증 객체 선언
        mAuth = FirebaseAuth.getInstance()

        // Google 로그인을 앱에 통합
        // GoogleSignInOptions 개체를 구성할 때 requestIdToken을 호출
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(RHA1)
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)
        binding.btnGoogleSignIn.setOnClickListener(View.OnClickListener { view: View? ->
            // 기존에 로그인 했던 계정을 확인한다.
            gsa = GoogleSignIn.getLastSignedInAccount(binding.root.context)
            if (gsa != null) // 로그인 되있는 경우
                Toast.makeText(binding.root.context, "이미 로그인", Toast.LENGTH_SHORT)
                    .show()
            else signIn()
        })
        binding.btnlogoutgoogle.setOnClickListener(View.OnClickListener { view: View? ->
            signOut() //로그아웃
        })
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient!!.signInIntent
//        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
//            if(it.resultCode == RESULT_OK) {
//                Log.d("LOG_CHECK", "TestActivity :: signIn() -> data : ${it.data}")
//            }
//        }


        d.launch(signInIntent)
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

//    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
//        if (requestCode == RC_SIGN_IN) {
//            // The Task returned from this call is always completed, no need to attach a listener.
//            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
//            handleSignInResult(task)
//        }
//    }

    /* 사용자 정보 가져오기 */
    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val acct = completedTask.getResult(ApiException::class.java)
            if (acct != null) {
                firebaseAuthWithGoogle(acct.idToken)
                val personName = acct.displayName
                val personGivenName = acct.givenName
                val personFamilyName = acct.familyName
                val personEmail = acct.email
                val personId = acct.id
                val personPhoto = acct.photoUrl
                Log.d(
                    TAG,
                    "handleSignInResult:personName $personName"
                )
                Log.d(
                    TAG,
                    "handleSignInResult:personGivenName $personGivenName"
                )
                Log.d(
                    TAG,
                    "handleSignInResult:personEmail $personEmail"
                )
                Log.d(
                    TAG,
                    "handleSignInResult:personId $personId"
                )
                Log.d(
                    TAG,
                    "handleSignInResult:personFamilyName $personFamilyName"
                )
                Log.d(
                    TAG,
                    "handleSignInResult:personPhoto $personPhoto"
                )
            }
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.e(TAG, "signInResult:failed code=" + e.statusCode)
        }
    }

    // [START auth_with_google]
    private fun firebaseAuthWithGoogle(idToken: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(
                this
            ) { task: Task<AuthResult?> ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    Toast.makeText(binding.root.context, "로그인", Toast.LENGTH_SHORT)
                        .show()
                    val user = mAuth!!.currentUser
                    //                            updateUI(user);
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(
                        TAG,
                        "signInWithCredential:failure",
                        task.exception
                    )
                    Toast.makeText(binding.root.context, "로그인 실패", Toast.LENGTH_SHORT)
                        .show()
                    //                            updateUI(null);
                }
            }
    }

    private fun updateUI(user: FirebaseUser) {}

    /* 로그아웃 */
    private fun signOut() {
        mGoogleSignInClient!!.signOut()
            .addOnCompleteListener(this) { task: Task<Void?>? ->
                mAuth!!.signOut()
                Toast.makeText(binding.root.context, "로그아웃", Toast.LENGTH_SHORT)
                    .show()
            }
        gsa = null
    }

    /* 회원 삭제요청 */
    private fun revokeAccess() {
        mGoogleSignInClient!!.revokeAccess()
            .addOnCompleteListener(
                this
            ) { task: Task<Void?>? -> }
    }

    companion object {
        private const val TAG = "LOG_CHECK"
        private const val RC_SIGN_IN = 9001
    }
}



