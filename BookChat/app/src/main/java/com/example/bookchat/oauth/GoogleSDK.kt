package com.example.bookchat.oauth

import android.content.Context
import android.content.Intent
import com.example.bookchat.BuildConfig.GOOGLE_SERVER_CLIENT_ID
import com.example.bookchat.data.IdToken
import com.example.bookchat.data.response.TokenDoseNotExistException
import com.example.bookchat.utils.DataStoreManager
import com.example.bookchat.utils.OAuth2Provider
import com.example.bookchat.utils.OAuth2Provider.GOOGLE
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN

object GoogleSDK {

    private val googleSignInOptions by lazy {
        GoogleSignInOptions
            .Builder(DEFAULT_SIGN_IN)
            .requestIdToken(GOOGLE_SERVER_CLIENT_ID)
            .build()
    }

    fun getSignInIntent(context :Context): Intent {
        val mGoogleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions)
        return mGoogleSignInClient.signInIntent
    }

    fun getSignedInAccountFromIntent(data :Intent?) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        saveGoogleIdToken(task.result.idToken)
    }

    private fun saveGoogleIdToken(idToken: String?) {
        idToken ?: throw TokenDoseNotExistException() // 좀 이상한데
        DataStoreManager.saveIdToken(IdToken("Bearer $idToken", GOOGLE))
    }

    private fun signOut(context :Context) {
        val mGoogleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions)
        mGoogleSignInClient.signOut().addOnCompleteListener { task -> }
    }

    private fun withdrawAccess(context :Context) {
        val mGoogleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions)
        mGoogleSignInClient.revokeAccess().addOnCompleteListener { task ->
        }
    }
}