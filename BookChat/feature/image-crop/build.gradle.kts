plugins {
	id("convention.android.feature")
}

android {
	namespace = "com.kova700.bookchat.feature.imagecrop"
	buildFeatures.viewBinding = true
}

dependencies {
	//Image Cropper
	implementation(libs.android.image.cropper)
	implementation(project(":core:util"))
}