plugins {
	id("convention.android.library")
	id("convention.android.hilt")
	alias(libs.plugins.kotlin.serialization)
}

android {
	namespace = "com.kova700.bookchat.core.datastore.searchhistory"
}

dependencies {
	implementation(project(":core:datastore:datastore"))
	implementation(project(":core:data:searchhistory:external"))
	implementation(libs.androidx.datastore.preferences)
	implementation(libs.kotlinx.serialization.json)
	testImplementation(libs.bundles.unit.test)
	androidTestImplementation(libs.bundles.android.test)
}