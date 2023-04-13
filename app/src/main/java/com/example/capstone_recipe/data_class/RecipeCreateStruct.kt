package com.example.capstone_recipe.data_class


data class Timer(
    var hour: Int = 0,
    var minute: Int = 0,
    var second: Int = 0
)
data class RecipeStep(
    var explanation: String = "",               // 단계 설명
    var imagePath: String? = null,                  // 단계 이미지
    var timer: Timer? = null                    // 타이머 설정 시간
)

data class Ingredient(                     // 레시피
    var name: String = "",                       // 재료의 이름
    var amount: String = ""                      // 재료의 양
)

data class RecipeBasicInfo(                     // 레시피 기본 정보
    var title: String = "",                     // 레시피 제목
    var intro: String = "",                     // 레시피 한 줄 소개
    var mainImagePath: String? = null,          // 레시피 대표 이미지
    var time: String = "",                      // 레시피 조리 시간
    var amount:String = "",                     // 레시피 요리 양
    var level: LEVEL = LEVEL.EASY,              // 레시피 난이도
    var shareOption: SHARE = SHARE.ONLY_ME      // 레시피 공개 대상
)

enum class SHARE{
    ONLY_ME,
    ONLY_FRIENDS,
    ALL
}

enum class LEVEL(val toKor:String){
    EASY("쉬움"),
    NORMAL("보통"),
    HARD("어려움")
}

enum class FROM{
    USER,
    API
}