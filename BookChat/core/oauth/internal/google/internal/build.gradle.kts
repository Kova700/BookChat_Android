import java.io.FileInputStream
import java.util.Properties

var properties = Properties().apply {
	load(FileInputStream("./core/oauth/internal/google/internal/local.properties"))
}

plugins {
	id("convention.android.library")
	id("convention.android.hilt")
}

android {
	namespace = "com.kova700.bookchat.core.oauth.internal.google.internal"

	defaultConfig.buildConfigField(
		type = "String",
		name = "GOOGLE_SERVER_CLIENT_ID",
		value = properties.getProperty("GOOGLE_SERVER_CLIENT_ID")
	)
	buildFeatures.buildConfig = true
}

dependencies {
	implementation(project(":core:oauth:internal:google:external"))
	implementation(libs.bundles.google.login)
}