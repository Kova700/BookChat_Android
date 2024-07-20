package com.example.bookchat.oauth.kakao.external.exception

import java.io.IOException

class KakaoLoginUserCancelException : IOException()
class KakaoLoginFailException(errorBody: String?) : IOException(errorBody)
