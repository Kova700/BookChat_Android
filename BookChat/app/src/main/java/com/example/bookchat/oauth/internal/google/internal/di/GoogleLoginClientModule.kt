package com.example.bookchat.oauth.internal.google.internal.di

import androidx.credentials.GetCredentialRequest
import com.example.bookchat.BuildConfig
import com.example.bookchat.oauth.internal.google.external.GoogleLoginClient
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class GoogleLoginClientModule {

	@Singleton
	@Provides
	fun provideGetSignInWithGoogleOption(): GetSignInWithGoogleOption {
		return GetSignInWithGoogleOption.Builder(BuildConfig.GOOGLE_SERVER_CLIENT_ID)
			.build()
	}

	@Singleton
	@Provides
	fun provideGetCredentialRequest(
		getSignInWithGoogleOption: GetSignInWithGoogleOption,
	): GetCredentialRequest {
		return GetCredentialRequest.Builder()
			.addCredentialOption(getSignInWithGoogleOption)
			.build()
	}

	@Singleton
	@Provides
	fun provideGoogleLoginClient(
		getCredentialRequest: GetCredentialRequest,
	): GoogleLoginClient {
		return GoogleLoginClient(getCredentialRequest)
	}
}