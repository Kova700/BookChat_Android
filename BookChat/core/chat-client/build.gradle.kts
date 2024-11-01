plugins {
	id("convention.android.library")
	id("convention.android.hilt")
}

android {
	namespace = "com.kova700.bookchat.core.chat_client"
}

dependencies {
	implementation(project(":core:stomp:chatting:external"))
	implementation(project(":core:network-manager:external"))
	implementation(project(":core:util"))
	implementation(project(":core:data:client:external"))
	implementation(project(":core:data:channel:external"))
	implementation(project(":core:domain"))
	implementation(project(":core:data:chat:external"))
}