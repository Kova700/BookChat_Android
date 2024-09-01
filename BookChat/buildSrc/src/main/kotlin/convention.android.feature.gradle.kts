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
	implementation(libs.findBundle("androidx-default").get())
	testImplementation(libs.findBundle("unit-test").get())
	androidTestImplementation(libs.findBundle("android-test").get())
}
