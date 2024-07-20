package com.example.bookchat.data.network.model.response

import java.io.IOException

// TODO :따로 예외로 구분해서 특정 작업을 해야하는 경우만 커스텀 예외를 만들고
//      예외에 대한 로직이 따로 없는 경우 기존에 Kotlin/Java에서 정의된 예외에 메세지를 넣는 방식으로 사용하게 추후 수정

class BadRequestException(errorBody: String?) : IOException(errorBody)
class ResponseBodyEmptyException(errorBody: String?) : IOException(errorBody)
class ForbiddenException(errorBody: String?) : IOException(errorBody)
class NeedToSignUpException(errorBody: String?) : IOException(errorBody)
class NeedToDeviceWarningException(errorBody: String?) : IOException(errorBody)
class NetworkIsNotConnectedException : IOException("network is not connected.")
class FCMTokenDoseNotExistException : IOException("Saved FCMToken does not exist")
class TokenDoseNotExistException : IOException("Saved BookChatToken does not exist")
class ChannelIsFullException : IOException("Channel is full")
class TokenRenewalFailException : IOException("TokenRenewal request is fail")
class BookReportDoseNotExistException : IOException("BookReport does not exist")