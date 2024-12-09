import gradle.configure.configureKotlinAndroid
import gradle.configure.libs

plugins {
	id("com.android.application")
	kotlin("android")
}

android {
	configureKotlinAndroid(this)
	defaultConfig.targetSdk = libs.findVersion("targetSdk").get().requiredVersion.toInt()

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