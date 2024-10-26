plugins {
	id("convention.android.feature")
}

android {
	namespace = "com.kova700.bookchat.feature.mypage"
	buildFeatures.viewBinding = true
}

dependencies {
	//aboutlibraries-License
	implementation(libs.aboutlibraries)
	implementation(project(":core:data:user:external"))
	implementation(project(":core:oauth:external"))
	implementation(project(":core:data:client:external"))
	implementation(project(":core:domain"))
	implementation(project(":core:data:appsetting:external"))
	implementation(project(":core:chat-client"))
}