package com.example.bookchat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.bookchat.repository.BookRepository
import com.example.bookchat.repository.UserRepository

// 뷰모델에 인자를 넘겨주기 위한 팩토리 메서드
class ViewModelFactory() : ViewModelProvider.Factory {
    private lateinit var bookRepository : BookRepository
    private lateinit var userRepository : UserRepository

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        when{
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                userRepository = UserRepository()
                return HomeViewModel(userRepository) as T
            }

            modelClass.isAssignableFrom(SearchResultViewModel::class.java) -> {
                bookRepository = BookRepository()
                return SearchResultViewModel(bookRepository) as T
            }

            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                userRepository = UserRepository()
                return LoginViewModel(userRepository) as T
            }

            modelClass.isAssignableFrom(SelectTasteViewModel::class.java) -> {
                userRepository = UserRepository()
                return SelectTasteViewModel(userRepository) as T
            }

            modelClass.isAssignableFrom(SignUpViewModel::class.java) -> {
                userRepository = UserRepository()
                return SignUpViewModel(userRepository) as T
            }

            else -> throw IllegalArgumentException("unknown model class : ${modelClass}")
        }
    }
}