package com.example.bookchat.oauth.google

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.example.bookchat.domain.model.IdToken
import com.example.bookchat.domain.model.OAuth2Provider.GOOGLE
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import javax.inject.Inject

class GoogleLoginClient @Inject constructor(
	private val googleSignInOptions: GoogleSignInOptions
){
	fun login(context: Context, resultLauncher: ActivityResultLauncher<Intent>) {
		resultLauncher.launch(getSignInIntent(context))
	}

	private fun getSignInIntent(context: Context): Intent {
		val mGoogleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions)
		return mGoogleSignInClient.signInIntent
	}

	fun getIdTokenFromResultIntent(data: Intent?): IdToken {
		val googleSignInAccount = GoogleSignIn.getSignedInAccountFromIntent(data).result
		return IdToken("$ID_TOKEN_PREFIX ${googleSignInAccount.idToken}", GOOGLE)
	}

	private fun logOut(context: Context) {
		val mGoogleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions)
		mGoogleSignInClient.signOut().addOnCompleteListener { task -> }
	}

	private fun withdraw(context: Context) {
		val mGoogleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions)
		mGoogleSignInClient.revokeAccess().addOnCompleteListener { task ->
		}
	}

	companion object {
		private const val ID_TOKEN_PREFIX = "Bearer"
	}

}