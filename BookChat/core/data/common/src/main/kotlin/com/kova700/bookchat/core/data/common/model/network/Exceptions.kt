package com.kova700.bookchat.core.data.common.model.network

import java.io.IOException

class ForbiddenException(errorBody: String?) : IOException(errorBody)
class TokenRenewalFailException : IOException("TokenRenewal request is fail")
class BadRequestException(errorBody: String?) : IOException(errorBody)