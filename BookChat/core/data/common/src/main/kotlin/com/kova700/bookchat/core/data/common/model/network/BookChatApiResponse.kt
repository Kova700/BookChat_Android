package com.kova700.bookchat.core.data.common.model.network

sealed interface BookChatApiResult<out T> {
	val code: Int
	val locationHeader: Long?
	val message: String?
	val body: String?

	data class Success<T>(
		override val code: Int,
		override val locationHeader: Long?,
		override val message: String? = null,
		override val body: String? = null,
		val data: T,
	) : BookChatApiResult<T>

	data class Failure<T>(
		override val code: Int,
		override val locationHeader: Long?,
		override val message: String? = null,
		override val body: String? = null,
	) : BookChatApiResult<T>
}