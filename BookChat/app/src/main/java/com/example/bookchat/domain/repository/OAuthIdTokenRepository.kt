package com.example.bookchat.domain.repository

import com.example.bookchat.domain.model.IdToken

interface OAuthIdTokenRepository {
	fun getIdToken(): IdToken
	fun saveIdToken(token: IdToken)
	fun clear()
}