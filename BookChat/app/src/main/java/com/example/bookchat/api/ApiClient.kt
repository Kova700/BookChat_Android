package com.example.bookchat.api

import com.example.bookchat.utils.Constants.DOMAIN
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type


object ApiClient {

    //싱글톤인가? 계속해서 객체를 만들고 있는데?
    fun getApiClient(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(DOMAIN)
            .client(provideOkHttpClient(AppInterceptor())) //OkHttp 클라이언트 주입
            .addConverterFactory(NullOnEmptyConverterFactory()) //응답 코드만 넘어와도 오류 안뜨게 인터셉터 수정
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun provideOkHttpClient(
        interceptor: Interceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()
    }

    class NullOnEmptyConverterFactory : Converter.Factory() {
        override fun responseBodyConverter(type: Type?, annotations: Array<Annotation>?, retrofit: Retrofit?): Converter<ResponseBody, *>? {
            val delegate = retrofit!!.nextResponseBodyConverter<Any>(this, type!!, annotations!!)
            return Converter<ResponseBody, Any> {
                if (it.contentLength() == 0L) return@Converter
                delegate.convert(it)
            }
        }
    }

}