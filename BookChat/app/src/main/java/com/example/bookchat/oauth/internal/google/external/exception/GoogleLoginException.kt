package com.example.bookchat.oauth.internal.google.external.exception

import java.io.IOException

class GoogleLoginClientCancelException : IOException()
class GoogleLoginFailException(errorBody: String?) : IOException(errorBody)