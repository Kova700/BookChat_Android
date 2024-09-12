plugins {
	id("convention.android.library")
	id("convention.android.hilt")
}

android {
	namespace = "com.kova700.bookchat.core.data.bookshelf.internal"
}

dependencies {
	implementation(project(":core:data:bookshelf:external"))
	implementation(project(":core:data:search:book:external"))
	implementation(project(":core:network:bookchat"))
	implementation(project(":core:data:common"))
}