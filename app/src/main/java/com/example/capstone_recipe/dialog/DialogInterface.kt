package com.example.capstone_recipe.dialog

import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.Toast
import com.example.capstone_recipe.databinding.DialogFrameBinding

class DialogInterface(context: Context): Dialog(context){
    private val binding by lazy { DialogFrameBinding.inflate(layoutInflater) }
    var title:String = "TEST"
    lateinit var view:View
    var topCloseVisible = false

    fun initDialog(){
        setContentView(binding.root)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        binding.tvDialogTitle.text = title
        binding.ivDialogTopClose.visibility = if(topCloseVisible) View.VISIBLE else View.INVISIBLE
        binding.layerDialogMainContent.addView(view)
        show()
    }

    fun setDialogSize(fixedSize: SIZE?, variableSize: Int?){
        fixedSize?.run {
            when(fixedSize){
                SIZE.SMALL ->{ // 150dp
                    binding.layerDialogMainContent.layoutParams.height = 150.dpToPx()
                }
                SIZE.NORMAL ->{ // 250dp
                    binding.layerDialogMainContent.layoutParams.height = 250.dpToPx()
                }
            }
        }
    }

    private fun Int.dpToPx(): Int {
        return (this * Resources.getSystem().displayMetrics.density).toInt()
    }
    enum class SIZE{
        SMALL,
        NORMAL
    }
}


