package com.example.bookchat.oauth.google.external.exception

import java.io.IOException

class GoogleLoginClientCancelException(errorBody: String?) : IOException(errorBody)
class GoogleLoginFailException(errorBody: String?) : IOException(errorBody)