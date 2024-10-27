package com.kova700.bookchat.core.data.chat.external.model

const val LOADING_CHAT_STATE_CODE = -1
const val RETRY_REQUIRED_CHAT_STATE_CODE = 0
const val FAILURE_CHAT_STATE_CODE = 1
const val SUCCESS_CHAT_STATE_CODE = 2

enum class ChatState(val code: Int) {
	LOADING(LOADING_CHAT_STATE_CODE),
	RETRY_REQUIRED(RETRY_REQUIRED_CHAT_STATE_CODE),
	FAILURE(FAILURE_CHAT_STATE_CODE),
	SUCCESS(SUCCESS_CHAT_STATE_CODE);

	companion object {
		fun getType(stateCode: Int) = entries.find { it.code == stateCode }
	}
}