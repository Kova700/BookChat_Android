package com.example.bookchat.response

import java.io.IOException

class BadRequestException(errorBody :String?) : IOException(errorBody)
class ResponseBodyEmptyException(errorBody :String?) : IOException(errorBody)
class TokenExpiredException(errorBody :String?) : IOException(errorBody)
class UnauthorizedOrBlockedUserException(errorBody :String?) : IOException(errorBody)
class NeedToSignUpException(errorBody :String?) : IOException(errorBody)
class NetworkIsNotConnectedException() : IOException("network is not connected.")