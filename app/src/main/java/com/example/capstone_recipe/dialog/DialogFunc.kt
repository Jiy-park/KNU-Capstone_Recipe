package com.example.capstone_recipe.dialog

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.widget.NumberPicker
import android.widget.TextView
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

            dialog.view = view
            dialog.setDialogSize(DIALOG_SIZE.NORMAL, null)
            dialog.initDialog()
        }

        fun timerDialog(context:Context, hour:Int = 0, min:Int = 0, sec:Int = 0){ // 타이머
            val dialog = DialogInterface(context)
            val view = LayoutInflater.from(context).inflate(R.layout.dialog_timer, null)
            dialog.title = "타이머"

            val hourPicker = view.findViewById<NumberPicker>(R.id.numberPickerHour) // 시간
            hourPicker.setFormatter { value ->
                String.format("%02d", value)
            }
            hourPicker.minValue = 0
            hourPicker.maxValue = 99
            hourPicker.value = hour

            val minutePicker = view.findViewById<NumberPicker>(R.id.numberPickerMinute) // 분
            minutePicker.setFormatter { value ->
                String.format("%02d", value)
            }
            minutePicker.minValue = 0
            minutePicker.maxValue = 59
            minutePicker.value = min

            val secondPicker = view.findViewById<NumberPicker>(R.id.numberPickerSecond) // 초
            secondPicker.setFormatter { value ->
                String.format("%02d", value)
            }
            secondPicker.minValue = 0
            secondPicker.maxValue = 59
            secondPicker.value = sec

            dialog.view = view
            dialog.setDialogSize(DIALOG_SIZE.NORMAL, null)
            dialog.initDialog()
        }
    }
}