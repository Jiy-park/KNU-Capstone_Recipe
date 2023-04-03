package com.example.capstone_recipe.dialog

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.widget.TextView
import com.example.capstone_recipe.R

class DialogFunc {
    companion object{
        fun settingDialog(context:Context){
            val dialog = DialogInterface(context)
            val view = LayoutInflater.from(context).inflate(R.layout.setting_dialog, null)
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
    }
}