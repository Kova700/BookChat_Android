plugins {
	id("convention.android.feature")
}

android {
	namespace = "com.kova700.bookchat.feature.image_crop"
	buildFeatures.viewBinding = true
}

dependencies {
	//Image Cropper
	implementation(libs.android.image.cropper)
	implementation(project(":core:util"))
}