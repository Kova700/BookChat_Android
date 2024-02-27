package com.example.bookchat.domain.model

private const val LOADING_STATUS_CODE = -1
private const val RETRY_REQUIRED_STATUS_CODE = 0
private const val FAILURE_STATUS_CODE = 1
private const val SUCCESS_STATUS_CODE = 2

enum class ChatStatus(val code: Int) {
	LOADING(LOADING_STATUS_CODE),
	RETRY_REQUIRED(RETRY_REQUIRED_STATUS_CODE),
	FAILURE(FAILURE_STATUS_CODE),
	SUCCESS(SUCCESS_STATUS_CODE);

	companion object{
		fun getType(statusCode: Int) = when (statusCode) {
			LOADING.code -> LOADING
			RETRY_REQUIRED.code -> RETRY_REQUIRED
			FAILURE.code -> FAILURE
			SUCCESS.code -> SUCCESS
			else -> null
		}
	}
}