plugins {
	id("convention.android.library")
	id("convention.android.hilt")
}

android {
	namespace = "com.kova700.bookchat.core.oauth.external"
}

dependencies {
	implementation(project(":core:oauth:internal:google:external"))
	implementation(project(":core:oauth:internal:kakao:external"))
	implementation(project(":core:data:oauth:external"))
}