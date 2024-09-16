plugins {
	id("convention.android.library")
	id("convention.android.hilt")
}

android {
	namespace = "com.kova700.bookchat.core.network.bookchat"
}

dependencies {
	implementation(libs.bundles.retrofit)
	implementation(platform(libs.okhttp.bom))
	implementation(libs.firebase.firestore.ktx)

	implementation(project(":core:network:network"))
	implementation(project(":core:data:common"))
	implementation(project(":core:data:bookshelf:external"))
	implementation(project(":core:data:chat:external"))
	implementation(project(":core:data:user:external"))
	implementation(project(":core:oauth:external"))
	implementation(project(":core:data:search:book:external"))
	implementation(project(":core:data:channel:external"))
	implementation(project(":core:data:search:channel:external"))
	implementation(project(":core:data:client:external"))
	implementation(project(":core:data:bookchat-token:external"))
	implementation(project(":core:data:bookreport:external"))
	implementation(project(":core:data:agonyrecord:external"))
	implementation(project(":core:data:agony:external"))
	implementation(project(":core:util"))
}