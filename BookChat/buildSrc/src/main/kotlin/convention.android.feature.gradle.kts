import gradle.configure.libs

plugins {
	id("convention.android.library")
	id("convention.android.hilt")
}

android {
	defaultConfig {
		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
	}
	buildFeatures.viewBinding = true
}

dependencies {
	implementation(project(":core:design-system"))
	implementation(libs.findLibrary("androidx-core").get())
	implementation(libs.findBundle("androidx-lifecycle").get())
	implementation(libs.findLibrary("appcompat").get())
	implementation(libs.findLibrary("material").get())
	testImplementation(libs.findBundle("unit-test").get())
	androidTestImplementation(libs.findBundle("android-test").get())
	debugImplementation(libs.findBundle("debug-test").get())
}
