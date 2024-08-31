import java.io.FileInputStream
import java.util.Properties

var properties = Properties().apply {
	load(FileInputStream("local.properties"))
}

plugins {
	id("convention.android.application")
	id("convention.android.hilt")
	alias(libs.plugins.secrets.gradle.plugin)
	alias(libs.plugins.google.fcm)
	alias(libs.plugins.aboutlibraries)
}

android {
	namespace = "com.example.bookchat"
	defaultConfig {
		applicationId = "com.example.bookchat"
		val majorVersion = libs.versions.major.get()
		val minorVersion = libs.versions.minor.get()
		val hotfixVersion = libs.versions.hotfix.get()
		versionCode = libs.versions.versionCode.get().toInt()
		versionName = "$majorVersion.$minorVersion.$hotfixVersion"
	}
	buildFeatures {
		buildConfig = true
	}
}

dependencies {
	implementation(project(":core:design-system"))
	implementation(libs.bundles.androidx.default)
	testImplementation(libs.bundles.unit.test)
	androidTestImplementation(libs.bundles.android.test)
	// Jetpack Navigation
	implementation(libs.bundles.jetpack.navigation)
	// Kakao Login
	implementation(libs.kakao.oauth.sdk)
	// Google Login
	implementation(libs.bundles.google.login)
	//Firebase BoM(Bill of Materials)
	implementation(platform(libs.firebase.bom))
	//Firebase Analytics
	implementation(libs.firebase.analytics.ktx)
	//Firebase FCM
	implementation(libs.firebase.messaging.ktx)
	// Glide
	implementation(libs.glide)
	ksp(libs.glide.compiler)
	//Image Cropper
	implementation(libs.android.image.cropper)
	//Preferences DataStore
	implementation(libs.androidx.datastore.preferences)
	//SimpleRatingBar
	implementation(libs.simpleratingbar)
	//Hilt
	implementation(libs.hilt.android)
	ksp(libs.hilt.android.compiler)
	//Flex box
	implementation(libs.flexbox)
	//Facebook.Shimmer
	implementation(libs.shimmer)
	//Krossbow-Stomp
	implementation(libs.bundles.krossbow.stomp)
	//Retrofit & OkHttp
	implementation(libs.bundles.retrofit)
	implementation(platform(libs.okhttp.bom))
	//Room
	implementation(libs.bundles.room)
	ksp(libs.androidx.room.compiler)
	// WorkManager
	implementation(libs.bundles.workmanager)
	//aboutlibraries-License
	implementation(libs.aboutlibraries)
}