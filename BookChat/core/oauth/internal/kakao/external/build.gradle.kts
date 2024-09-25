import java.io.FileInputStream
import java.util.Properties

var properties = Properties().apply {
	load(FileInputStream("./core/oauth/internal/kakao/external/local.properties"))
}

plugins {
	id("convention.android.library")
	id("convention.android.hilt")
}

android {
	namespace = "com.kova700.bookchat.core.oauth.internal.kakao.external"

	defaultConfig.buildConfigField(
		type = "String",
		name = "KAKAO_APP_KEY",
		value = properties.getProperty("KAKAO_APP_KEY")
	)
	buildFeatures.buildConfig = true
}

dependencies {
	implementation(libs.kakao.oauth.sdk)
	implementation(project(":core:data:oauth:external"))
}