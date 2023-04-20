package com.example.capstone_recipe.data_class

import android.net.Uri

data class User(
    var id: String = "",
    var name: String = "",
    var score: Int = 0,                                               // 유저가 업로드한 레시피의 추천 수 총합
    var profileImagePath: String = "",                               // 유저의 프로필 이미지
    var backgroundImagePath: String = "",                            // 유저의 백그라운드 이미지
    var recentRecipe: String = "",                                   // 유저가 가장 마지막에 본 레시피의 아이디 저장
    var friends: MutableList<FriendInfo> = mutableListOf(),              // 유저 아이디를 리스트로 저장
    var uploadRecipe: MutableList<String> = mutableListOf(),         // 업로드한 레시피의 아이디를 리스트로 저장
    var saveRecipe: MutableList<String> = mutableListOf()            // 유저가 보관하는 레시피의 아이디를 리스트로 저장
)

data class FriendInfo(
    var id: String = "",
    var name: String = "",
    var profileImagePath: String = ""
)

data class UserLogInInfo(
    val id: String,
    val pw: String
)

