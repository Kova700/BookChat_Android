plugins {
	id("convention.android.library")
	id("convention.android.hilt")
}

android {
	namespace = "com.kova700.bookchat.core.domain"
}

dependencies {
	implementation(project(":core:data:bookchat-token:external"))
	implementation(project(":core:data:fcm-token:external"))
	implementation(project(":core:data:chat:external"))
	implementation(project(":core:data:channel:external"))
	implementation(project(":core:data:client:external"))
	implementation(project(":core:data:oauth:external"))
	implementation(project(":core:data:user:external"))
	implementation(project(":core:data:agonyrecord:external"))
	implementation(project(":core:data:search:book:external"))
	implementation(project(":core:data:search:channel:external"))
	implementation(project(":core:data:searchhistory:external"))
	implementation(project(":core:data:bookshelf:external"))
	implementation(project(":core:data:deviceinfo:external"))
	implementation(project(":core:data:agony:external"))
	implementation(project(":core:data:notificationinfo:external"))
	implementation(project(":core:data:searchhistory:external"))
	implementation(project(":core:notification:chat:external"))
}
