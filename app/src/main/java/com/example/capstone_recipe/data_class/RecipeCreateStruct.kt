package com.example.capstone_recipe.data_class

import android.net.Uri

data class RecipeStep(
    var explanation: String = "",               // 단계 설명
    var Image: Uri? = null,                     // 단계 이미지
    var timerHour:Int = 0,                      //타이머 설정 시간
    var timerMin:Int = 0,
    var timerSec:Int = 0
)

data class RecipeIngredient(                    // 레시피
    var name:String = "",                       // 재료의 이름
    var amount:String = ""                      // 재료의 양
)

data class RecipeBasicInfo(                     // 레시피 기본 정보
    var title: String = "",                     // 레시피 제목
    var intro: String = "",                     // 레시피 한 줄 소개
    var time: String = "",                          // 레시피 조리 시간
    var amount:String = "",                         // 레시피 요리 양
    var level: LEVEL = LEVEL.EASY,              // 레시피 난이도
    var mainImage:Uri? = null,                  // 레시피 대표 이미지
    var shareTarget: SHARE = SHARE.ONLY_ME      // 레시피 공개 대상
)

enum class SHARE{
    ONLY_ME,
    ONLY_FRIENDS,
    ALL
}

enum class LEVEL{
    EASY,
    NORMAL,
    HARD
}