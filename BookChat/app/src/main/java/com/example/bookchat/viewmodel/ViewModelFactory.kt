package com.example.bookchat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.bookchat.repository.AgonizeRepository
import com.example.bookchat.repository.UserRepository

class ViewModelFactory : ViewModelProvider.Factory {
    private val userRepository by lazy { UserRepository() }
    private val agonizeRepository by lazy { AgonizeRepository() }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        when{
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                return HomeViewModel(userRepository) as T
            }

            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                return LoginViewModel(userRepository) as T
            }

            modelClass.isAssignableFrom(SelectTasteViewModel::class.java) -> {
                return SelectTasteViewModel(userRepository) as T
            }

            modelClass.isAssignableFrom(SignUpViewModel::class.java) -> {
                return SignUpViewModel(userRepository) as T
            }

            modelClass.isAssignableFrom(MyPageViewModel::class.java) -> {
                return MyPageViewModel(userRepository) as T
            }
            modelClass.isAssignableFrom(AgonizeHistoryViewModel::class.java) -> {
                return AgonizeHistoryViewModel(agonizeRepository) as T
            }
            else -> throw IllegalArgumentException("unknown model class : ${modelClass}")
        }
    }
}