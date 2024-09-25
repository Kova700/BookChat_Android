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
	implementation(project(":core:navigation"))
	implementation(project(":core:util"))
	implementation(project(":core:domain"))
	implementation(libs.findBundle("androidx-default").get())
	implementation(libs.findBundle("jetpack-navigation").get())
	testImplementation(libs.findBundle("unit-test").get())
	androidTestImplementation(libs.findBundle("android-test").get())
}