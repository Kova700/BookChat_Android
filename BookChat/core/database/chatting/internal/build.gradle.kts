plugins {
	id("convention.android.library")
	id("convention.android.hilt")
}

android {
	namespace = "com.kova700.bookchat.core.database.chatting.internal"
}

dependencies {
	ksp(libs.androidx.room.compiler)
	implementation(libs.bundles.room)
	implementation(libs.kotlinx.serialization.json)
	implementation(project(":core:database:chatting:external"))
	implementation(project(":core:data:channel:external"))
	implementation(project(":core:data:chat:external"))
	implementation(project(":core:data:user:external"))
}