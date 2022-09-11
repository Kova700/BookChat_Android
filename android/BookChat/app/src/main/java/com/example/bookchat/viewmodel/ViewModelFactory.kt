package com.example.bookchat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.bookchat.repository.BookRepository
import com.example.bookchat.repository.UserRepository

// 뷰모델에 인자를 넘겨주기 위한 팩토리 메서드
class ViewModelFactory() : ViewModelProvider.Factory {
    private lateinit var bookRepository : BookRepository
    private lateinit var userRepository : UserRepository

    //싱글톤으로 구조 수정 필요할 듯
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        when(modelClass){
            MainViewModel::class -> {
                userRepository = UserRepository()
                return MainViewModel(userRepository) as T
            }

            SearchResultViewModel::class -> {
                bookRepository = BookRepository()
                return SearchResultViewModel(bookRepository) as T
            }

            LoginViewModel::class ->{
                userRepository = UserRepository()
                return LoginViewModel(userRepository) as T
            }

            else -> throw IllegalArgumentException("unknown model class")
        }

    }
}