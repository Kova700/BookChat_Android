package com.example.bookchat.oauth.internal.google.external

import android.content.Context
import android.credentials.GetCredentialException.TYPE_USER_CANCELED
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.example.bookchat.oauth.external.model.OAuth2Provider.GOOGLE
import com.example.bookchat.oauth.external.model.IdToken
import com.example.bookchat.oauth.external.model.IdToken.Companion.ID_TOKEN_PREFIX
import com.example.bookchat.oauth.internal.google.external.exception.GoogleLoginClientCancelException
import com.example.bookchat.oauth.internal.google.external.exception.GoogleLoginFailException
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import javax.inject.Inject

class GoogleLoginClient @Inject constructor(
	private val getCredentialRequest: GetCredentialRequest,
) {

	suspend fun login(activityContext: Context): IdToken {
		val credentialManager = CredentialManager.create(activityContext)
		return runCatching {
			credentialManager.getCredential(
				request = getCredentialRequest,
				context = activityContext,
			)
		}.onFailure { handelError(it) }
			.getOrThrow().getIdToken()
	}

	suspend fun logout(activityContext: Context) {
		clearCredentialState(activityContext)
	}

	suspend fun withdraw(activityContext: Context) {
		clearCredentialState(activityContext)
	}

	private suspend fun clearCredentialState(activityContext: Context) {
		val credentialManager = CredentialManager.create(activityContext)
		credentialManager.clearCredentialState(ClearCredentialStateRequest())
	}

	private fun GetCredentialResponse.getIdToken(): IdToken {
		if (credential !is CustomCredential
			|| credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
		) throw Exception("Unexpected type of credential")

		runCatching {
			GoogleIdTokenCredential.createFrom(credential.data).idToken
		}.onFailure { throw Exception("Received an invalid google id token response") }
			.getOrThrow().run { return IdToken("$ID_TOKEN_PREFIX $this", GOOGLE) }
	}

	private fun handelError(exception: Throwable) {
		when (exception) {
			is GetCredentialException -> if (exception.type == TYPE_USER_CANCELED) throw GoogleLoginClientCancelException()
			else -> throw GoogleLoginFailException(exception.message)
		}
	}
}