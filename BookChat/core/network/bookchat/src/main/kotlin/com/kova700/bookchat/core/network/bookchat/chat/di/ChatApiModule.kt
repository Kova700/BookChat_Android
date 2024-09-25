package com.kova700.bookchat.core.network.bookchat.chat.di

import com.kova700.bookchat.core.network.bookchat.chat.ChatApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
class ChatApiModule {

	@Provides
	fun provideChatApi(
		retrofit: Retrofit,
	): ChatApi {
		return retrofit.create(ChatApi::class.java)
	}
}