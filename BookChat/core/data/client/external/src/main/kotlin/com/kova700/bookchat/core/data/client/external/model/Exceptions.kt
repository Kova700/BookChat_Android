package com.kova700.bookchat.core.data.client.external.model

import java.io.IOException

class NeedToSignUpException(errorBody: String?) : IOException(errorBody)
class NeedToDeviceWarningException(errorBody: String?) : IOException(errorBody)
class AlreadySignedUpException(errorBody: String?) : IOException(errorBody)
