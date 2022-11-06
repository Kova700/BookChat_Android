package com.example.bookchat.api

import com.example.bookchat.data.Token
import com.example.bookchat.data.User
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface UserApiInterface {
    //API 테스트
    //@POST("/3862dd02-166b-4975-9a3c-1666ebd4fcfa")

    @GET("/v1/api/users/profile/nickname")
    suspend fun requestNameDuplicateCheck(
        @Query("nickname") nickname :String
    ) :Response<Unit>


    //회원가입
    @Multipart
    @POST("/v1/api/users/signup")
    suspend fun signUp(
        @Header("OIDC") idToken :String,
        @Part userProfileImage: MultipartBody.Part? = null,
        @Part("userSignUpRequest") userSignUpRequest: RequestBody
    ) :Response<Unit>

    @POST("/v1/api/users/signin")
    suspend fun signIn(
        @Header("OIDC") idToken :String,
        @Body oauth2Provider :RequestBody
    ) : Response<Token>

    @DELETE("/v1/api/users")
    suspend fun withdraw(
    ) : Response<Unit>

    @GET("/v1/api/users/profile")
    suspend fun getUserProfile(
    ) : Response<User>

}