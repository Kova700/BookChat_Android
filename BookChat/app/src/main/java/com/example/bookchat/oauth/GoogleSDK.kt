package com.example.bookchat.oauth

import android.content.Context
import android.content.Intent
import com.example.bookchat.BuildConfig.GOOGLE_SERVER_CLIENT_ID
import com.example.bookchat.data.IdToken
import com.example.bookchat.data.response.NeedToGoogleLoginException
import com.example.bookchat.data.response.TokenDoseNotExistException
import com.example.bookchat.utils.DataStoreManager
import com.example.bookchat.utils.OAuth2Provider.GOOGLE
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN


object GoogleSDK {

    private val googleSignInOptions by lazy {
        GoogleSignInOptions
            .Builder(DEFAULT_SIGN_IN)
            .requestIdToken(GOOGLE_SERVER_CLIENT_ID)
            .build()
    }

    suspend fun googleLogin(context :Context){
        val userAccount = getUserGoogleAccount(context) ?: throw NeedToGoogleLoginException()
        saveIdToken(userAccount.idToken )
    }

    private fun getUserGoogleAccount(context :Context): GoogleSignInAccount? =
        GoogleSignIn.getLastSignedInAccount(context)

    fun getSignInIntent(context :Context): Intent {
        val mGoogleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions)
        return mGoogleSignInClient.signInIntent
    }

    private suspend fun saveIdToken(token : String?){
        token ?: throw TokenDoseNotExistException()
        DataStoreManager.saveIdToken(IdToken("Bearer $token", GOOGLE) )
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