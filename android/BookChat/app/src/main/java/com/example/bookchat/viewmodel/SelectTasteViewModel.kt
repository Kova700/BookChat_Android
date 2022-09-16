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
        "경제" to ReadingTaste.ECONOMY , "철학" to ReadingTaste.PHILOSOPHY , "역사" to ReadingTaste.HISTORY ,
        "여행" to ReadingTaste.TRAVEL , "건강" to ReadingTaste.HEALTH , "취미" to ReadingTaste.HOBBY ,
        "인문" to ReadingTaste.HUMANITIES , "소설" to ReadingTaste.NOVEL, "예술" to ReadingTaste.ART ,
        "디자인" to ReadingTaste.DESIGN , "개발" to ReadingTaste.DEVELOPMENT , "과학" to ReadingTaste.SCIENCE ,
        "잡지" to ReadingTaste.MAGAZINE , "종교" to ReadingTaste.RELIGION , "인물" to ReadingTaste.CHARACTER
    )

}