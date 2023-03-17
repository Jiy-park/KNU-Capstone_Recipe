package com.example.capstone_recipe.fragments

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import com.example.capstone_recipe.MainActivity
import com.example.capstone_recipe.databinding.FragmentMainBinding

class MainFragment : Fragment() {
    private lateinit var binding: FragmentMainBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mActivity = activity as MainActivity
        binding.run{
            btnToFriends.setOnClickListener { mActivity.replaceFragment(MainActivity.FRAGMENTS.FRIENDS) }
            btnToProfile.setOnClickListener { mActivity.replaceFragment(MainActivity.FRAGMENTS.PROFILE) }
            btnToSetting.setOnClickListener { mActivity.replaceFragment(MainActivity.FRAGMENTS.SETTING) }
            editSearchBar.setOnEditorActionListener(object : TextView.OnEditorActionListener{
                override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                    if (actionId == EditorInfo.IME_ACTION_DONE){
                        Toast.makeText(binding.root.context, "${binding.editSearchBar.text}", Toast.LENGTH_SHORT).show()
                        return false
                    }
                    return false
                }
            })
        }

    }
}