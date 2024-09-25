plugins {
	id("convention.android.library")
	id("convention.android.hilt")
}

android {
	namespace = "com.kova700.bookchat.core.data.searchhistory.internal"
}

dependencies {
	implementation(project(":core:datastore:searchhistory"))
	implementation(project(":core:data:searchhistory:external"))
}