plugins {
	id("convention.android.library")
	id("convention.android.hilt")
}

android {
	namespace = "com.kova700.bookchat.core.database.chatting.external"
}

dependencies {
	ksp(libs.androidx.room.compiler)
	implementation(libs.bundles.room)
	implementation(project(":core:data:channel:external"))
	implementation(project(":core:data:user:external"))
	implementation(project(":core:data:chat:external"))
	implementation(project(":core:util"))
}