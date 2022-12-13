package com.example.bookchat.api

import com.example.bookchat.utils.Constants.DOMAIN
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitBuilder {

    fun getApiClient(): Retrofit {
        return Retrofit.Builder()
//            .baseUrl("https://webhook.site/") //API 테스트
            .baseUrl(DOMAIN)
            .client(provideOkHttpClient(AppInterceptor())) //OkHttp 클라이언트 주입
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun provideOkHttpClient(
        interceptor: Interceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addNetworkInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)) //로그용 임시
            .addInterceptor(interceptor)
            .build()
    }
}