plugins {
	id("convention.android.feature")
}

android {
	namespace = "com.kova700.bookchat.feature.channel"
	buildFeatures.viewBinding = true
}

dependencies {
	implementation(project(":core:domain"))
	implementation(project(":core:data:user:external"))
	implementation(project(":core:data:channel:external"))
	implementation(project(":core:data:client:external"))
	implementation(project(":core:data:chat:external"))
	implementation(project(":core:stomp:chatting:external"))
	implementation(project(":core:network-manager:external"))
	implementation(project(":core:notification:chat:external"))
	implementation(project(":core:remote-config"))
}
