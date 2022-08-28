package com.example.bookchat.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import com.example.bookchat.R
import com.example.bookchat.databinding.ActivitySelectTasteBinding
import com.example.bookchat.utils.Constants.TAG
import com.google.android.material.chip.Chip

class SelectTasteActivity : AppCompatActivity() {
    private lateinit var binding :ActivitySelectTasteBinding
    private lateinit var tasteList :ArrayList<String>

        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_select_taste)
        with(binding){
            activity = this@SelectTasteActivity
        }
            tasteList = ArrayList()

    }

    fun clickTasteChip(view : View){
        when(view){
            is Chip -> {
                val taste = view.text.toString()
                if (!tasteList.contains(taste)){
                    tasteList.add(taste)
                }else{
                    tasteList.remove(taste)
                }
                Log.d(TAG, "SelectTasteActivity: clickTasteChip() - called - tasteList : $tasteList")
            }
        }
    }

    fun clickBackBtn() {
        finish()
    }
}