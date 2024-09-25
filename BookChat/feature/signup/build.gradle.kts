plugins {
	id("convention.android.feature")
}
android {
	namespace = "com.kova700.bookchat.feature.signup"
	buildFeatures.viewBinding = true
}
dependencies{
	implementation(project(":core:domain"))
	implementation(project(":core:data:client:external"))
	implementation(project(":core:data:user:external"))
	implementation(project(":core:data:common"))
}