plugins {
	id("convention.android.feature")
}
android {
	namespace = "com.kova700.bookchat.feature.channellist"
	buildFeatures.viewBinding = true
}
dependencies {
	implementation(project(":feature:channel"))
	implementation(project(":core:network-manager:external"))
	implementation(project(":core:data:channel:external"))
	implementation(project(":core:data:user:external"))
	implementation(project(":core:data:chat:external"))
}
