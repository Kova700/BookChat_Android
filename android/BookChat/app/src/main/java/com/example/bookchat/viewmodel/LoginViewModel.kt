package com.example.bookchat.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.kakao.KakaoSDK
import com.example.bookchat.repository.UserRepository
import com.example.bookchat.utils.Constants.TAG
import com.kakao.sdk.auth.AuthApiClient
import kotlinx.coroutines.launch

class LoginViewModel(private val userRepository : UserRepository) : ViewModel(){
    lateinit var loginSuccessCallBack :() -> Unit //토큰 가져왔으면 페이지 이동 실행
    private val _isLoginResult = MutableLiveData<Boolean>()

    //카카오로부터 토큰 받아오기
    fun requestKakaoLogin(){
        startKakaoLogin()
        //받아온 결과에 따라 분기 처리
        //토큰을 카카오로부터 가져왔든 로컬로부터 가져왔든
        // 성공적으로 가져왔으면 서버에게 로그인 요청 보내기 (회원정보 달라고 요청)
            //서버가 토큰을 뜯어보고 회원가입된 유저가 아니다 싶으면 오류 응답 코드를 보냄
                // -> 회원가입 액티비티로 화면 이동
                    // -> 회원가입 API로 유저 ID 토큰과 사용자 기입 정보를 실어서 회원가입 진행
            //서버가 토큰 뜯어보고 회원가입된 유저다 싶으면 유저 정보를 실은 JSON을 응답으로 보내줌
                // -> 메인 액티비티로 화면이동
                    // -> 유저정보 화면에 뿌려줌

        //일단 회원가입부터 만들어보자


        // 성공적으로 가져오지 못했으면
        Log.d(TAG, "LoginViewModel: requestKakaoLogin() - called - AuthApiClient.instance.hasToken() : ${AuthApiClient.instance.hasToken()}")
        loginSuccessCallBack()
    }

    fun startKakaoLogin(){
        viewModelScope.launch {
            KakaoSDK.login()
        }
    }
    
    fun requestGoogleLogin(){
    }

    //카카오로부터 토큰 받아오고
    //받은 토큰으로 북챗 서버에 요청을 보내야함

}