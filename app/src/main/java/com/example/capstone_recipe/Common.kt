package com.example.capstone_recipe

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.net.Uri
import android.util.DisplayMetrics
import android.util.Log
import java.text.SimpleDateFormat

//class Common {
//    /***
//     * 이미지 파일의 경로 생성 : 파이어베이스 스토리지에 올릴 용도
//     * - 이미지 분류 : 1.유저 이미지(프로필, 백그라운드) 2. 레시피 이미지(스텝 별, 대표 )
//     *   - 프로필          : users_image/$userId/profile/$userId_$yyyyMMdd_HHmm.jpeg
//     *   - 백 그라운드      : users_image/$userId/back/$userId_$yyyyMMdd_HHmm.jpeg
//     *   - 스텝 별 이미지   : recipe_image/$recipeId/step/$userId_$recipeId_$yyyyMMdd_HHmm_$step.jpeg ->
//     *   - 대표 이미지      : recipe_image/$recipeId/main/$userId_$recipeId_$yyyyMMdd_HHmm.jpeg
//     */
//    companion object Classification{
//        const val USER_PROFILE = 0
//        const val USER_BACKGROUND = 1
//        const val RECIPE_STEP = 2
//        const val RECIPE_MAIN = 3
//    }
//    @SuppressLint("SimpleDateFormat")
//    fun makeImageFilePath(activity: Activity, classification: Classification, userId:String, recipeId: String?, uri: Uri): String {
//        val path = when(classification){
//            USER_PROFILE->{
//                "user_image/$userId/profile/"
//            }
//
//        }
//
//
//        val mimeType = activity.contentResolver?.getType(uri) ?: "/none" //마임타입 ex) images/jpeg
//        val ext = mimeType.split("/")[1] //확장자 ex) jpeg
//        val time = SimpleDateFormat("yyyyMMdd_HHmm").format(System.currentTimeMillis())
//        return "${path}/${userId}_${timeSuffix}.$ext" // 파일 경로
//    }
//
//
//    fun convertPixelsToDp(px: Float, context: Context): Float { // px to dp
//        val resources = context.resources
//        val metrics: DisplayMetrics = resources.displayMetrics
//        return px / (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
//    }
//}