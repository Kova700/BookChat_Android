plugins {
	id("convention.android.library")
	id("convention.android.hilt")
}
android {
	namespace = "com.kova700.core.design_system"
}
dependencies {
	implementation(libs.bundles.androidx.default)
	testImplementation(libs.bundles.unit.test)
	androidTestImplementation(libs.bundles.android.test)
}