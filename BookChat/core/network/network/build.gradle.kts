import java.io.FileInputStream
import java.util.Properties

var properties = Properties().apply {
	load(FileInputStream("./core/network/network/local.properties"))
}

plugins {
	id("convention.android.library")
	id("convention.android.hilt")
}

android {
	namespace = "com.kova700.bookchat.core.network.network"
	defaultConfig.buildConfigField(
		type = "String",
		name = "DOMAIN",
		value = properties.getProperty("DOMAIN")
	)
	defaultConfig.buildConfigField(
		type = "String",
		name = "TOKEN_RENEWAL_URL",
		value = properties.getProperty("TOKEN_RENEWAL_URL")
	)
	buildFeatures.buildConfig = true
}

dependencies {
	implementation(libs.bundles.retrofit)
	implementation(platform(libs.okhttp.bom))
	implementation(libs.kotlinx.serialization.json)
	implementation(project(":core:data:bookchat-token:external"))
	implementation(project(":core:datastore:bookchat-token"))
	implementation(project(":core:data:common"))
	implementation(project(":core:fcm:forced-logout"))
}