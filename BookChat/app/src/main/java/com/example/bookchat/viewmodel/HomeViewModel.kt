package com.example.bookchat.viewmodel

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.App
import com.example.bookchat.data.User
import com.example.bookchat.repository.UserRepository
import com.example.bookchat.utils.Constants.TAG
import kotlinx.coroutines.launch

class HomeViewModel(private val userRepository: UserRepository) : ViewModel(){
    lateinit var goLoginActivityCallBack : () -> Unit

    private val _user = MutableLiveData<User?>(User("","","",1))

    val user : LiveData<User?>
        get() = _user

    fun activityInitialization(){
        getUserInfo()
    }

    fun getUserInfo(){
        val cachedUser = App.instance.getCachedUser()
        // 이거 뭔가 userRepository에서 가져와야할거 같은데
        Log.d(TAG, "MainViewModel: getUserInfo() - cachedUser : $cachedUser")
        cachedUser?.let { user -> _user.value = user }
        Log.d(TAG, "MainViewModel: getUserInfo() - user : ${user.value}")
    }

    fun requestWithdraw() = viewModelScope.launch {
        runCatching { userRepository.withdraw() }
            .onSuccess {
                Toast.makeText(App.instance.applicationContext,"회원이 탈퇴되었습니다(임시)",Toast.LENGTH_SHORT).show()
                goLoginActivityCallBack() //로그인 액티비티로 이동 (임시)
            }
            .onFailure {  Toast.makeText(App.instance.applicationContext,"회원이 탈퇴 실패 (임시)",Toast.LENGTH_SHORT).show()}
    }

}