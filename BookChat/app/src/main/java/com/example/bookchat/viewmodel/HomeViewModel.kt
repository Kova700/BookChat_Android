package com.example.bookchat.viewmodel

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.App
import com.example.bookchat.R
import com.example.bookchat.data.User
import com.example.bookchat.repository.UserRepository
import com.example.bookchat.response.NetworkIsNotConnectedException
import com.example.bookchat.response.ResponseBodyEmptyException
import com.example.bookchat.response.TokenExpiredException
import com.example.bookchat.response.UnauthorizedOrBlockedUserException
import com.example.bookchat.utils.Constants
import kotlinx.coroutines.launch

class HomeViewModel(private val userRepository: UserRepository) : ViewModel(){
    lateinit var goLoginActivityCallBack : () -> Unit
    private var recursiveChecker = false //임시 (구조 개선 필요)

    private val _user = MutableLiveData<User?>(User("","","",1))

    val user : LiveData<User?>
        get() = _user

    fun activityInitialization(){
        getUserInfo()
    }
    private fun getUserInfo() = viewModelScope.launch {
        runCatching{ userRepository.getUserProfile() }
            .onSuccess { userDto -> _user.value = userDto }
    }

    fun requestWithdraw() = viewModelScope.launch {
        runCatching { userRepository.withdraw() }
            .onSuccess {
                Toast.makeText(App.instance.applicationContext,"회원이 탈퇴되었습니다(임시)",Toast.LENGTH_SHORT).show()
                goLoginActivityCallBack() //로그인 액티비티로 이동 (임시)
            }
            .onFailure {
                failHandler(it)
                Toast.makeText(App.instance.applicationContext,"회원이 탈퇴 실패 (임시)",Toast.LENGTH_SHORT).show()
            }
    }

    private fun requestTokenRenewal() = viewModelScope.launch {
        Log.d(Constants.TAG, "LoginViewModel: requestTokenRenewal() - called")
        runCatching{ userRepository.requestTokenRenewal() }
            .onSuccess { if (recursiveChecker == false) requestWithdraw(); recursiveChecker = true } //두번쨰 실패했을때는 아무것도 알 수 없음..
            .onFailure {
                failHandler(it)
                Log.d(Constants.TAG, "LoginViewModel: requestTokenRenewal() - onFailure : $it")
            }
    }

    //Status Code별 Exception handle (임시)
    private fun failHandler(exception: Throwable) {
        when(exception){
            is TokenExpiredException -> requestTokenRenewal()
            is UnauthorizedOrBlockedUserException -> {
                Log.d(Constants.TAG, "LoginViewModel: failHandler() - unauthorizedOrBlockedUserException")
            }
            is ResponseBodyEmptyException -> {
                Log.d(Constants.TAG, "LoginViewModel: failHandler() - ResponseBodyEmptyException")
            }
            is NetworkIsNotConnectedException -> {
                //추후에 스낵바 혹은 유튜브처럼 구현
                Toast.makeText(App.instance.applicationContext,
                    R.string.message_error_network_toast, Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(App.instance.applicationContext, R.string.message_error_else_toast, Toast.LENGTH_SHORT).show()
                Log.d(Constants.TAG, "LoginViewModel: failHandler() - $exception")}
        }
    }

}