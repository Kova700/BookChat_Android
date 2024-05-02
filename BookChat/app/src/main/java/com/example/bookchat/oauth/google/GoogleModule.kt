package com.example.bookchat.oauth.google

import com.example.bookchat.BuildConfig
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
class GoogleModule {

	@Singleton
	@Provides
	fun provideGoogleSignInOptions(): GoogleSignInOptions {
		return GoogleSignInOptions
			.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
			.requestIdToken(BuildConfig.GOOGLE_SERVER_CLIENT_ID)
			.build()
	}

	@Singleton
	@Provides
	fun provideGoogleLoginClient(googleSignInOptions: GoogleSignInOptions): GoogleLoginClient {
		return GoogleLoginClient(googleSignInOptions)
	}
}