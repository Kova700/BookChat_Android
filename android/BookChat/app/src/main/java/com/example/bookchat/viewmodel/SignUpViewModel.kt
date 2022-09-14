package com.example.bookchat.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SignUpViewModel :ViewModel() {

    private var _isNotTextShort = MutableLiveData<Boolean>()
    private var _isNotTextDuplicate = MutableLiveData<Boolean>()
    var _isSpecialCharInText = MutableLiveData<Boolean>()

    val isNotShort : LiveData<Boolean>
        get() = _isNotTextShort

    val isNotDuplicate : LiveData<Boolean>
        get() = _isNotTextDuplicate

}