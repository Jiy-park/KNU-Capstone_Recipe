package com.example.capstone_recipe.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.LayoutInflater
import android.widget.NumberPicker
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.example.capstone_recipe.Login
import com.example.capstone_recipe.R

class DialogFunc {
    companion object{
        fun settingDialog(context:Context){ // 환경설정
            val dialog = DialogInterface(context)
            val view = LayoutInflater.from(context).inflate(R.layout.dialog_setting, null)
            dialog.title = "환경설정"

            view.findViewById<TextView>(R.id.tvDeveloperInfo).setOnClickListener {
                dialog.dismiss()
                val intent = Intent(context, DeveloperInfo::class.java)
                context.startActivity(intent)
            }

            view.findViewById<TextView>(R.id.tvLogOut).setOnClickListener {
                dialog.dismiss()
                val intent = Intent(context, Login::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(intent)
            }

            dialog.view = view
            dialog.setDialogSize(DialogInterface.SIZE.NORMAL, null)
            dialog.initDialog()
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun timerDialog(context:Context, hour:Int = 0, min:Int = 0, sec:Int = 0){ // 타이머
            val dialog = DialogInterface(context)
            val view = LayoutInflater.from(context).inflate(R.layout.dialog_timer, null)
            dialog.title = "타이머"

            val vibrator =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                    vibratorManager.defaultVibrator
                }
                else {
                    @Suppress("DEPRECATION")
                    context.getSystemService(VIBRATOR_SERVICE) as Vibrator
                }


            val hourPicker = view.findViewById<NumberPicker>(R.id.numberPickerHour) // 시간
            hourPicker.setFormatter { value ->
                String.format("%02d", value)
            }
            hourPicker.minValue = 0
            hourPicker.maxValue = 99
            hourPicker.value = hour
            hourPicker.setOnValueChangedListener { _, _, _ ->
                    vibrator.vibrate(VibrationEffect.createOneShot(50L,100,))
            }
            val minutePicker = view.findViewById<NumberPicker>(R.id.numberPickerMinute) // 분
            minutePicker.setFormatter { value ->
                String.format("%02d", value)
            }
            minutePicker.minValue = 0
            minutePicker.maxValue = 59
            minutePicker.value = min
            minutePicker.setOnValueChangedListener { _, _, _ ->
                vibrator.vibrate(VibrationEffect.createOneShot(50L,100,))
            }

            val secondPicker = view.findViewById<NumberPicker>(R.id.numberPickerSecond) // 초
            secondPicker.setFormatter { value ->
                String.format("%02d", value)
            }
            secondPicker.minValue = 0
            secondPicker.maxValue = 59
            secondPicker.value = sec
            secondPicker.setOnValueChangedListener { _, _, _ ->
                vibrator.vibrate(VibrationEffect.createOneShot(50L,100,))
            }

            dialog.view = view
            dialog.setDialogSize(DialogInterface.SIZE.NORMAL, null)
            dialog.initDialog()
        }
    }
}