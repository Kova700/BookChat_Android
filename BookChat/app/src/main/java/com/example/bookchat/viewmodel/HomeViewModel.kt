package com.example.bookchat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.data.User
import com.example.bookchat.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val userRepository: UserRepository) : ViewModel(){
    private val _user = MutableStateFlow<User?>(User("","","",1))
    val user = _user.asStateFlow()

    init {
        getUserInfo()
    }

    private fun getUserInfo() = viewModelScope.launch {
        runCatching{ userRepository.getUserProfile() }
            .onSuccess { userDto -> _user.value = userDto }
    }

}