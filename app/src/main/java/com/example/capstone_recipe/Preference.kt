package com.example.capstone_recipe

import android.content.Context
import android.widget.EditText

class Preference(context: Context) {
    private val sharedPref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)

    fun getUserId() = sharedPref.getString(USER_ID, "") ?: ""
    fun getUserName() = sharedPref.getString(USER_NAME, "") ?: ""
    fun getUseAutoLogIn() = sharedPref.getBoolean(USE_AUTO_LOG_IN, true)

    fun setUseAutoLogIn(use: Boolean = true){       // 자동 로그인 사용 여부
        val editor = sharedPref.edit()
        editor.putBoolean(USE_AUTO_LOG_IN, use)
        editor.apply()
    }
    fun checkSetAutoLogInInfo(): Boolean{       // 자동 로그인에 사용될 id, pw 체크
        val id = sharedPref.getString(USER_ID, null)
        val pw = sharedPref.getString(USER_PW, null)
        val use = sharedPref.getBoolean(USE_AUTO_LOG_IN, true)
        if(!use || id == null || pw == null){ return false }
        return true
    }

    fun saveUserInfo(userId: String, userName: String) {
        val editor = sharedPref.edit()
        editor.putString(USER_ID, userId)
        editor.putString(USER_NAME, userName)
        editor.apply()
    }


    fun saveAutoLogInInfo(userId: String, userPw: String){
        val editor = sharedPref.edit()
        editor.putString(USER_ID, userId)
        editor.putString(USER_PW, userPw)
        editor.apply()
    }
    fun autoLogIn(editId: EditText, editPw: EditText){
        val userId = sharedPref.getString(USER_ID, null)
        val userPw = sharedPref.getString(USER_PW, null)
        if(userId != null && userPw != null){
            editId.setText(userId)
            editPw.setText(userPw)
        }
    }

    companion object KEYS{
        const val PREFERENCE_NAME = "preference"

        const val USER_ID = "userId"
        const val USER_PW = "userPw"
        const val USER_NAME = "userName"

        const val USE_AUTO_LOG_IN = "useAutoLogIn"
    }
}