package com.example.bookchat.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bookchat.data.User
import com.example.bookchat.repository.UserRepository
import com.example.bookchat.utils.Constants.TAG

class MainViewModel(private val userRepository: UserRepository) : ViewModel(){

    private val _user = MutableLiveData<User>()

    val user : LiveData<User>
        get() = _user

    fun activityInitialization(){
        getUserInfo()
    }

    fun getUserInfo(){
        if (_user.value == null){
            userRepository.getUserProfile{ user: User -> _user.value = user}   //user 받아올 콜백 메서드 전달
            Log.d(TAG, "MainViewModel: getUserInfo() - 값 불러오기 완료")
        }
    }

}