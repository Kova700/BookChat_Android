package com.kova700.bookchat.core.network.network

import android.content.Context
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.kova700.bookchat.core.data.bookchat_token.external.repository.BookChatTokenRepository
import com.kova700.bookchat.core.network.network.BuildConfig.DOMAIN
import com.kova700.bookchat.core.network.network.converter.EnumConverterFactory
import com.kova700.bookchat.core.network.network.converter.ResultCallAdapterFactory
import com.kova700.bookchat.core.network.network.intercepter.BookChatNetworkInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RetrofitModule {
	private val contentType = "application/json".toMediaType()

	@Provides
	@Singleton
	fun provideJsonSerializer(): Json {
		return Json {
			ignoreUnknownKeys = true
			coerceInputValues = true
			encodeDefaults = true
			isLenient = true
		}
	}

	@Provides
	@Singleton
	fun provideRetrofit(
		okHttpClient: OkHttpClient,
		enumConverterFactory: EnumConverterFactory,
		jsonSerializer: Json,
	): Retrofit {
		return Retrofit.Builder()
//    .baseUrl("https://webhook.site/") //API 테스트
			.baseUrl(DOMAIN)
			.client(okHttpClient)
			.addConverterFactory(jsonSerializer.asConverterFactory(contentType))
			.addConverterFactory(enumConverterFactory)
			.addCallAdapterFactory(ResultCallAdapterFactory())
			.build()
	}

	@Provides
	@Singleton
	fun provideOkHttpClient(
		interceptor: Interceptor,
		httpLoggingInterceptor: HttpLoggingInterceptor,
	): OkHttpClient {
		return OkHttpClient.Builder()
			.addNetworkInterceptor(httpLoggingInterceptor) //TODO : [FixWaiting] 릴리즈 버전에서는 제거해야함
			.addInterceptor(interceptor)
			.build()
	}

	@Provides
	@Singleton
	fun provideAppInterceptor(
		bookChatTokenRepository: BookChatTokenRepository,
		jsonSerializer: Json,
		@ApplicationContext context: Context,
	): Interceptor {
		return BookChatNetworkInterceptor(
			bookChatTokenRepository = bookChatTokenRepository,
			jsonSerializer = jsonSerializer,
			context = context,
		)
	}

	@Provides
	@Singleton
	fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor =
		HttpLoggingInterceptor().apply {
			level = HttpLoggingInterceptor.Level.BODY
		}
}