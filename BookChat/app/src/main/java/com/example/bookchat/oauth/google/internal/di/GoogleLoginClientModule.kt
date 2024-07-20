package com.example.bookchat.oauth.google.internal.di

import androidx.credentials.GetCredentialRequest
import com.example.bookchat.BuildConfig
import com.example.bookchat.oauth.google.external.GoogleLoginClient
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
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
	fun provideGetGoogleIdOption(): GetGoogleIdOption {
		return GetGoogleIdOption.Builder()
			.setFilterByAuthorizedAccounts(true)
			.setServerClientId(BuildConfig.GOOGLE_SERVER_CLIENT_ID)
			.setAutoSelectEnabled(true)
			.build()
	}

	@Singleton
	@Provides
	fun provideGetCredentialRequest(
		googleIdOption: GetGoogleIdOption,
	): GetCredentialRequest {
		return GetCredentialRequest.Builder()
			.addCredentialOption(googleIdOption)
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