plugins {
	id("convention.android.feature")
}

android {
	namespace = "com.kova700.bookchat.feature.bookreport"
	buildFeatures.viewBinding = true
}

dependencies{
	//SimpleRatingBar
	implementation(libs.simpleratingbar)
	implementation(project(":core:data:bookreport:external"))
	implementation(project(":core:data:bookshelf:external"))
}