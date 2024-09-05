plugins {
	id("convention.android.library")
	id("convention.android.hilt")
}

android {
	namespace = "com.kova700.bookchat.core.oauth:internal:kakao:external"
}

dependencies {
	implementation(project(":core:oauth:external"))
	implementation(project(":core:data:oauth:external"))
	implementation(libs.kakao.oauth.sdk)
}