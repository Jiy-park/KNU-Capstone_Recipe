package com.example.capstone_recipe.data_class

import android.net.Uri

data class User(
    val id: String = "",
    val name: String = "",
    val score: Int = 0,                         // 유저가 업로드한 레시피의 추천 수 총합
    val profileImagePath: String? = null,     // 유저의 프로필 이미지
    val backgroundImagePath: String? = null,                // 유저의 백그라운드 이미지
    val recentRecipe: Int? = null,             // 유저가 가장 마지막에 본 레시피의 아이디 저장
    val friends: List<String>,                  // 유저 아이디를 리스트로 저장
    val uploadRecipe: List<Int>,               // 업로드한 레시피의 아이디를 리스트로 저장
    val recipeLocker: List<Int>,               // 유저가 보관하는 레시피의 아이디를 리스트로 저장
)

data class UserLogInInfo(
    val id: String,
    val pw: String
)

