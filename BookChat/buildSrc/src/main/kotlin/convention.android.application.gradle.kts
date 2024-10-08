import gradle.configure.configureKotlinAndroid

plugins {
	id("com.android.application")
	kotlin("android")
}

android {
	configureKotlinAndroid(this)
	defaultConfig.targetSdk = 34

	buildTypes {
		release {
			isMinifyEnabled = true
			isShrinkResources = true
			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"),
				"proguard-rules.pro"
			)
		}
	}
}