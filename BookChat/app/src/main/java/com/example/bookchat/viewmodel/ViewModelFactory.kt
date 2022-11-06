package com.example.bookchat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.bookchat.repository.BookRepository
import com.example.bookchat.repository.UserRepository

// 뷰모델에 인자를 넘겨주기 위한 팩토리 메서드
class ViewModelFactory(val searchKeyword : String = "") : ViewModelProvider.Factory {
    private val userRepository by lazy { UserRepository() }
    private val bookRepository by lazy { BookRepository() }

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

            modelClass.isAssignableFrom(SearchViewModel::class.java) -> {
                return SearchViewModel(bookRepository) as T
            }

            modelClass.isAssignableFrom(SearchDetailViewModel::class.java) -> {
                return SearchDetailViewModel(bookRepository,searchKeyword) as T
            }

            modelClass.isAssignableFrom(SearchTapBookDialogViewModel::class.java) -> {
                return SearchTapBookDialogViewModel(bookRepository) as T
            }

            else -> throw IllegalArgumentException("unknown model class : ${modelClass}")
        }
    }
}