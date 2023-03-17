package com.example.capstone_recipe.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.capstone_recipe.R
import com.example.capstone_recipe.databinding.FragmentFriendsBinding

class FriendsFragment : Fragment() {
    private lateinit var binding:FragmentFriendsBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentFriendsBinding.inflate(inflater,container,false)
        return binding.root
    }
}
