package com.example.bookchat.oauth.oauthclient.internal.google.external.exception

import java.io.IOException

class GoogleLoginClientCancelException : IOException()
class GoogleLoginFailException(errorBody: String?) : IOException(errorBody)