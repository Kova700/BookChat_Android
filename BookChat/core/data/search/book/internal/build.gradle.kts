plugins {
	id("convention.android.library")
	id("convention.android.hilt")
}

android {
	namespace = "com.kova700.bookchat.core.data.search.book.internal"
}

dependencies {
	implementation(project(":core:data:search:book:external"))
	implementation(project(":core:network:bookchat"))
}