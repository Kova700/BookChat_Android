package com.example.bookchat.domain.repository

import com.example.bookchat.data.UserSignUpDto
import com.example.bookchat.domain.model.BookChatToken
import com.example.bookchat.domain.model.IdToken
import com.example.bookchat.domain.model.User

interface ClientRepository {
	suspend fun signIn(idToken: IdToken, approveChangingDevice: Boolean = false)
	suspend fun getClientProfile(): User
	suspend fun signUp(userSignUpDto: UserSignUpDto)
	suspend fun signOut(needAServer: Boolean = false)
	suspend fun withdraw()
	suspend fun checkForDuplicateUserName(nickName: String)
	suspend fun renewFCMToken(token: String)
	suspend fun getBookChatToken(): BookChatToken?
	suspend fun saveBookChatToken(token: BookChatToken)
}