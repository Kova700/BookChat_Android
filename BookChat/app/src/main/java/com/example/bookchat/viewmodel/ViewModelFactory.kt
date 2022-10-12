package com.example.bookchat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.bookchat.repository.BookRepository
import com.example.bookchat.repository.DupCheckRepository
import com.example.bookchat.repository.UserRepository

// 뷰모델에 인자를 넘겨주기 위한 팩토리 메서드
class ViewModelFactory() : ViewModelProvider.Factory {
    private lateinit var bookRepository : BookRepository
    private lateinit var userRepository : UserRepository
    private lateinit var dupCheckRepository : DupCheckRepository

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
                return SelectTasteViewModel() as T
            }

            modelClass.isAssignableFrom(SignUpViewModel::class.java) -> {
                dupCheckRepository = DupCheckRepository()
                return SignUpViewModel(dupCheckRepository) as T
            }

            else -> throw IllegalArgumentException("unknown model class : ${modelClass}")
        }
    }
}