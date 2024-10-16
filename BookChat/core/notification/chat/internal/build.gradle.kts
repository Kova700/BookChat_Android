plugins {
	id("convention.android.library")
	id("convention.android.hilt")
}

android {
	namespace = "com.kova700.bookchat.core.notification.chat.internal"
}

dependencies {
	implementation(project(":core:design-system"))
	implementation(project(":core:notification:chat:external"))
	implementation(project(":core:notification:util"))
	implementation(project(":core:data:channel:external"))
	implementation(project(":core:data:chat:external"))
	implementation(project(":core:data:user:external"))
	implementation(project(":core:data:notificationinfo:external"))
	implementation(project(":core:stomp:chatting:external"))
	implementation(project(":feature:main"))
	implementation(project(":core:util"))
	implementation(project(":core:navigation"))
	implementation(project(":feature:channel"))
}