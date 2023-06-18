package com.example.capstone_recipe.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.LayoutInflater
import android.widget.*
import androidx.annotation.RequiresApi
import com.example.capstone_recipe.Login
import com.example.capstone_recipe.Preference
import com.example.capstone_recipe.R

class DialogFunc {
    companion object{
        @SuppressLint("UseSwitchCompatOrMaterialCode")
        fun settingDialog(context:Context){ // 환경설정
            val dialog = DialogInterface(context)
            val pref = Preference(context)
            val view = LayoutInflater.from(context).inflate(R.layout.dialog_setting, null)
            dialog.title = "환경설정"

            val switchTTS = view.findViewById<Switch>(R.id.switchSpeak)
            switchTTS.isChecked = pref.getUseTTS()

            val switchSTT = view.findViewById<Switch>(R.id.switchListen)
            switchSTT.isChecked = pref.getUseSTT()

            val switchMsg = view.findViewById<Switch>(R.id.switchMSG)
            switchMsg.isChecked = pref.getUseCloudMsg()

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

            view.findViewById<Button>(R.id.btnDialogOK).setOnClickListener {
                dialog.dismiss()
                pref.setUseTTS(switchTTS.isChecked)
                pref.setUseSTT(switchSTT.isChecked)
                pref.setUseCloudMsg(switchMsg.isChecked)
            }

            view.findViewById<Button>(R.id.btnDialogCancel).setOnClickListener {
                dialog.dismiss()
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

        @SuppressLint("MissingInflatedId", "SetTextI18n")
        fun deleteRecipeDialog(context: Context,  userId: String, recipeId: String, okClick: (String, String)->Unit){
            val dialog = DialogInterface(context)
            val view = LayoutInflater.from(context).inflate(R.layout.dialog_delete_recipe, null)
            dialog.title = "레시피 삭제"
            view.findViewById<TextView>(R.id.tvWarningText).text = "레시피를 \n삭제하실 건가요?"
            view.findViewById<Button>(R.id.btnDialogOK).setOnClickListener {
                dialog.dismiss()
                okClick(userId, recipeId)
            }
            view.findViewById<Button>(R.id.btnDialogCancel).setOnClickListener {
                dialog.dismiss()
            }
            dialog.view = view
            dialog.setDialogSize(DialogInterface.SIZE.SMALL, null)
            dialog.initDialog()
        }

        @SuppressLint("MissingInflatedId")
        fun settingToxiDialog(context: Context, callback: (speed: Int, tone: Int, responsiveness: Int)->Unit){
            val dialog = DialogInterface(context)
            val pref = Preference(context)
            val view = LayoutInflater.from(context).inflate(R.layout.dialog_toxi_setting, null)
            dialog.title = "토시 설정"

            val speakSpeed = view.findViewById<SeekBar>(R.id.seekbarSpeakSpeed)
            speakSpeed.progress = pref.getSpeakSpeed()// =  pref.getSpeakSpeed()

            val voiceTone = view.findViewById<SeekBar>(R.id.seekbarVoiceTone)
            voiceTone.progress =  pref.getVoiceTone()

            val responsiveness = view.findViewById<SeekBar>(R.id.seekbarRecognitionResponsiveness)
            responsiveness.progress =  pref.getResponsiveness()

            var afterSpeed = speakSpeed.progress
            var afterTone = voiceTone.progress
            var afterResponsiveness = responsiveness.progress

            val seekBarListener =  object: SeekBar.OnSeekBarChangeListener{
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    when(seekBar?.id){
                        R.id.seekbarSpeakSpeed -> { afterSpeed = seekBar.progress }
                        R.id.seekbarVoiceTone -> { afterTone = seekBar.progress }
                        R.id.seekbarRecognitionResponsiveness -> { afterResponsiveness = seekBar.progress }
                    }
                }
            }

            speakSpeed.setOnSeekBarChangeListener(seekBarListener)
            voiceTone.setOnSeekBarChangeListener(seekBarListener)
            responsiveness.setOnSeekBarChangeListener(seekBarListener)

            view.findViewById<Button>(R.id.btnDialogOK).setOnClickListener {
                pref.setSpeakSpeed(afterSpeed)
                pref.setVoiceTone(afterTone)
                pref.setResponsiveness(afterResponsiveness)

                dialog.dismiss()
                callback(afterSpeed, afterTone, afterResponsiveness)
            }

            view.findViewById<Button>(R.id.btnDialogCancel).setOnClickListener {
                dialog.dismiss()
            }

            dialog.view = view
            dialog.setDialogSize(DialogInterface.SIZE.NORMAL, null)
            dialog.initDialog()
        }

    }
}