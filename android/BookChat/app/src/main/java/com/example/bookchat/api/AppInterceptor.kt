package com.example.bookchat.api

import com.example.bookchat.App
import com.example.bookchat.kakao.KakaoSDK
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class AppInterceptor : Interceptor {
    lateinit var kakaoSDK : KakaoSDK

    @Throws(IOException::class)
    override fun intercept(
        chain: Interceptor.Chain // chain : 가로채지기 직전의 요청에 대한 정보가 모두 들어있음
    ): Response{
        //구조가 좀 비효율적임 수정하면 좋을 듯
        kakaoSDK = KakaoSDK(App.instance.applicationContext) //컨택스트 가능한지 오류 확인해야함
        val kakaoIdToken = kakaoSDK.getIdToken() //카카오 구글 분기해서 보내야함
        val googleIdToken = " " //수정해야함
        val idToken = kakaoIdToken ?: googleIdToken
        
            return with(chain) {
            val tokenAddedRequest = request().newBuilder() //앞에 요청 내용 모두 복사 (리퀘스트는 불변객체이기 때문)
                .addHeader("Authorization", idToken) //헤더에 토큰 추가
                .build() //생성
            proceed(tokenAddedRequest) //서버로부터 새로운 Request 정보로 요청을 보낸 뒤 받은 응답 값
        } //체인에 추가
    }
}