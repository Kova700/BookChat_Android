package com.example.bookchat.viewmodel

import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bookchat.utils.Constants.TAG
import com.example.bookchat.utils.ReadingTaste
import com.google.android.material.chip.Chip

class SelectTasteViewModel :ViewModel() {

    private val selectedTastes = ArrayList<ReadingTaste>()
    private val _isTastesEmpty = MutableLiveData<Boolean>()

    val isTastesEmpty : LiveData<Boolean>
        get() = _isTastesEmpty


    fun clickTaste(view: View) {
        val chip = view as Chip
        val taste = chip.text.toString()
        if (selectedTastes.contains(tasteConverter[taste])) selectedTastes.remove(tasteConverter[taste])
        else selectedTastes.add(tasteConverter[taste]!!)
        emptyCheck()
        Log.d(TAG, "SelectTasteViewModel: clickTaste() - selectedTastes : $selectedTastes")
    }

    private fun emptyCheck() {
        if (selectedTastes.isEmpty()){
            _isTastesEmpty.value = true
            return
        }
        _isTastesEmpty.value = false
    }

    val tasteConverter = hashMapOf<String,ReadingTaste>(
        "경제" to ReadingTaste.Economy , "철학" to ReadingTaste.Philosophy , "역사" to ReadingTaste.History ,
        "여행" to ReadingTaste.Travel , "건강" to ReadingTaste.Health , "취미" to ReadingTaste.Hobby ,
        "인문" to ReadingTaste.Humanities , "소설" to ReadingTaste.Novel, "예술" to ReadingTaste.Art ,
        "디자인" to ReadingTaste.Design , "개발" to ReadingTaste.Development , "과학" to ReadingTaste.Science ,
        "잡지" to ReadingTaste.Magazine , "종교" to ReadingTaste.Religion , "인물" to ReadingTaste.Character
    )

}