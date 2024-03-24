package com.example.bookchat.data.di

import com.example.bookchat.BuildConfig.DOMAIN
import com.example.bookchat.data.network.AppInterceptor
import com.example.bookchat.data.network.BookChatApi
import com.example.bookchat.data.network.EnumConverterFactory
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RetrofitModule {

	@Provides
	@Singleton
	fun provideRetrofit(
		okHttpClient: OkHttpClient,
		gsonConverterFactory: GsonConverterFactory,
		enumConverterFactory: EnumConverterFactory
	): BookChatApi {
		return Retrofit.Builder()
//    .baseUrl("https://webhook.site/") //API 테스트
			.baseUrl(DOMAIN)
			.client(okHttpClient)
			.addConverterFactory(gsonConverterFactory)
			.addConverterFactory(enumConverterFactory)
			.build()
			.create(BookChatApi::class.java)
	}

	@Provides
	@Singleton
	fun provideOkHttpClient(
		interceptor: Interceptor,
		httpLoggingInterceptor: HttpLoggingInterceptor,
	): OkHttpClient {
		return OkHttpClient.Builder()
			.addNetworkInterceptor(httpLoggingInterceptor)
			.addInterceptor(interceptor)
			.build()
	}

	@Provides
	@Singleton
	fun provideGsonConverterFactory(gson: Gson): GsonConverterFactory {
		return GsonConverterFactory.create(gson)
	}

	@Provides
	@Singleton
	fun provideGson(): Gson {
		return Gson()
	}

	@Provides
	@Singleton
	fun provideAppInterceptor(): Interceptor = AppInterceptor()

	@Provides
	@Singleton
	fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor =
		HttpLoggingInterceptor()
			.apply { level = HttpLoggingInterceptor.Level.BODY }
}