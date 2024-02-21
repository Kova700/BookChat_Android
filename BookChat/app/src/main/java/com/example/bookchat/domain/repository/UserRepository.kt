package com.example.bookchat.domain.repository

import com.example.bookchat.data.User
import com.example.bookchat.data.UserSignUpDto

interface UserRepository {
	suspend fun signIn(approveChangingDevice: Boolean = false)
	suspend fun getUserProfile() : User
	suspend fun signUp(userSignUpDto: UserSignUpDto)
	suspend fun signOut()
	suspend fun withdraw()
	suspend fun checkForDuplicateUserName(nickName: String)
}