package com.example.bookchat.domain.model

const val LOADING_CHAT_STATUS_CODE = -1
const val RETRY_REQUIRED_CHAT_STATUS_CODE = 0
const val FAILURE_CHAT_STATUS_CODE = 1
const val SUCCESS_CHAT_STATUS_CODE = 2

enum class ChatStatus(val code: Int) {
	LOADING(LOADING_CHAT_STATUS_CODE),
	RETRY_REQUIRED(RETRY_REQUIRED_CHAT_STATUS_CODE),
	FAILURE(FAILURE_CHAT_STATUS_CODE),
	SUCCESS(SUCCESS_CHAT_STATUS_CODE);

	companion object {
		fun getType(statusCode: Int) = when (statusCode) {
			LOADING.code -> LOADING
			RETRY_REQUIRED.code -> RETRY_REQUIRED
			FAILURE.code -> FAILURE
			SUCCESS.code -> SUCCESS
			else -> null
		}
	}
}