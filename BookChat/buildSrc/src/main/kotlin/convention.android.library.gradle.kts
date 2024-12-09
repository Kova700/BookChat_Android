import gradle.configure.configureKotlinAndroid
import gradle.configure.libs

plugins {
	id("com.android.library")
	kotlin("android")
}

android {
	configureKotlinAndroid(this)
	defaultConfig.targetSdk = libs.findVersion("targetSdk").get().requiredVersion.toInt()
	defaultConfig {
		consumerProguardFiles("proguard-rules.pro")
	}
}