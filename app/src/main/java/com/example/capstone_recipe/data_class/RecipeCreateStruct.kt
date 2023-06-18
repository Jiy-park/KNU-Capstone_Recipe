package com.example.capstone_recipe.data_class

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Timer(
    var hour: Int = 0,
    var minute: Int = 0,
    var second: Int = 0
): Parcelable

@Parcelize
data class RecipeStep(
    var explanation: String = "",               // 단계 설명
    var imagePath: String = "",                  // 단계 이미지
    var timer: Timer? = null                    // 타이머 설정 시간
): Parcelable

@Parcelize
data class Ingredient(                     // 레시피
    var name: String = "",                       // 재료의 이름
    var amount: String = ""                      // 재료의 양
): Parcelable

@Parcelize
data class RecipeBasicInfo(                     // 레시피 기본 정보
    var id: String = "",
    var title: String = "",                     // 레시피 제목
    var intro: String = "",                     // 레시피 한 줄 소개
    var mainImagePath: String = "",          // 레시피 대표 이미지
    var time: String = "",                      // 레시피 조리 시간
    var amount:String = "",                     // 레시피 요리 양
    var level: LEVEL = LEVEL.EASY,              // 레시피 난이도
    var shareOption: SHARE = SHARE.ALL,      // 레시피 공개 대상
    var score: Int = 0,
): Parcelable

@Parcelize
data class RecipeSupplement(
    val calorie: String = "",
    val fat: String = "",
    val carbohydrate: String = "",
    val protein: String = "",
    val sodium: String = ""
): Parcelable

enum class SHARE{
    ONLY_ME,
    ONLY_FRIENDS,
    ALL
}

enum class LEVEL(val toKor:String){
    EASY("쉬움"),
    NORMAL("보통"),
    HARD("어려움"),
    NONE("-")
}
