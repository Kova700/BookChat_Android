plugins {
	id("convention.android.library")
	id("convention.android.hilt")
}

android {
	namespace = "com.kova700.amazonbookstorepractice.core.datastore.datastore"
}

dependencies {
	implementation(libs.androidx.datastore.preferences)
}