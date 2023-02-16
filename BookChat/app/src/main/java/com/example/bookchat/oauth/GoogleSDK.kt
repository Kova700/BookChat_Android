package com.example.bookchat.oauth

import android.content.Context
import android.content.Intent
import com.example.bookchat.BuildConfig.GOOGLE_SERVER_CLIENT_ID
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

    private fun signOut(context :Context) {
        val mGoogleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions)
        mGoogleSignInClient.signOut().addOnCompleteListener { task ->

        }
    }

    private fun withdrawAccess(context :Context) {
        val mGoogleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions)
        mGoogleSignInClient.revokeAccess().addOnCompleteListener { task ->

        }
    }
}