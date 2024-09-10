plugins {
	id("convention.android.library")
	id("convention.android.hilt")
}

android {
	namespace = "com.kova700.bookchat.core.database.chatting.external"
}

dependencies {
	implementation(project(":core:data:channel:external"))
	implementation(project(":core:data:user:external"))
	implementation(project(":core:data:chat:external"))
	implementation(project(":core:util"))
	implementation(libs.bundles.room)
	ksp(libs.androidx.room.compiler)
}