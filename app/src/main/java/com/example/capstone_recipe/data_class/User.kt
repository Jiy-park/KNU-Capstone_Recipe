package com.example.capstone_recipe.data_class

import android.net.Uri

data class User(
    val ID: String = "",
    val PW: String = "",
    val NAME: String = "",
    val SCORE: Int = 0,                 // 유저가 업로드한 레시피의 추천 수 총합
    val PROFILE_IMAGE: Uri? = null,     // 유저의 프로필 이미지
    val BACK_IMAGE: Uri? = null,         // 유저의 백그라운드 이미지
    val RECENT_RECIPE: Int? = null,     // 유저가 가장 마지막에 본 레시피의 아이디 저장
    val FRIENDS: List<String>,          // 유저 아이디를 리스트로 저장
    val UPLOAD_RECIPE: List<Int>,       // 업로드한 레시피의 아이디를 리스트로 저장
    val RECIPE_LOCKER: List<Int>,       // 유저가 보관하는 레시피의 아이디를 리스트로 저장
)


