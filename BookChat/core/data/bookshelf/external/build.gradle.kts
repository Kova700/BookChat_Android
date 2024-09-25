plugins {
	id("convention.android.library")
	id("convention.android.hilt")
}

android {
	namespace = "com.kova700.bookchat.core.data.bookshelf.external"
}

dependencies {
	implementation(project(":core:data:common"))
	implementation(project(":core:data:search:book:external"))
}