plugins {
	id("convention.android.library")
	id("convention.android.hilt")
}

android {
	namespace = "com.kova700.bookchat.core.database.chatting.internal"
}

dependencies {
	implementation(project(":core:data:channel:external"))
	implementation(project(":core:database:chatting:external"))
	implementation(libs.bundles.room)
	implementation(project(":core:data:chat:external"))
	ksp(libs.androidx.room.compiler)
	implementation(libs.kotlinx.serialization.json)
}