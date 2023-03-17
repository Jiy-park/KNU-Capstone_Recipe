package com.example.capstone_recipe

import android.graphics.Matrix
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.capstone_recipe.databinding.ActivityLoginBinding
import com.google.android.material.shape.ShapeAppearancePathProvider.PathListener
import com.google.android.material.shape.ShapePath
import com.sothree.slidinguppanel.PanelSlideListener
import com.sothree.slidinguppanel.PanelState

class Login : AppCompatActivity() {
    private val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.btnSignIn.setOnClickListener {
            binding.signIn.visibility = View.VISIBLE
            binding.signUp.visibility = View.GONE
            binding.slidingLayout.panelState = PanelState.EXPANDED
        }
        binding.btnSignUp.setOnClickListener {
            binding.signUp.visibility = View.VISIBLE
            binding.signIn.visibility = View.GONE
            binding.slidingLayout.panelState = PanelState.EXPANDED
        }
        binding.slidingLayout.addPanelSlideListener(object :PanelSlideListener{
            override fun onPanelSlide(panel: View, slideOffset: Float) {}

            override fun onPanelStateChanged(panel: View, previousState: PanelState, newState: PanelState) {
                if(newState == PanelState.EXPANDED){
                    binding.ivBackgroundHead.visibility = View.VISIBLE
                }
                else{
                    binding.ivBackgroundHead.visibility = View.GONE
                }
            }
        })
    }
}