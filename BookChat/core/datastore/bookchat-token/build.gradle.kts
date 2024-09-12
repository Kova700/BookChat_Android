plugins {
	id("convention.android.library")
	id("convention.android.hilt")
	alias(libs.plugins.kotlin.serialization)
}

android {
	namespace = "com.kova700.bookchat.core.datastore.bookchat_token"
}

dependencies {
	implementation(project(":core:datastore:datastore"))
	implementation(project(":core:data:bookchat-token:external"))
	implementation(libs.androidx.datastore.preferences)
	implementation(libs.kotlinx.serialization.json)
	testImplementation(libs.bundles.unit.test)
	androidTestImplementation(libs.bundles.android.test)
}