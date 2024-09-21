plugins {
	id("convention.android.feature")
}
android {
	namespace = "com.kova700.bookchat.feature.bookshelf"
	buildFeatures.viewBinding = true
}
dependencies {
	//Flex box
	implementation(libs.flexbox)
	//Facebook.Shimmer
	implementation(libs.shimmer)
	//SimpleRatingBar
	implementation(libs.simpleratingbar)
	implementation(project(":core:data:bookshelf:external"))
	implementation(project(":core:data:search:book:external"))
	implementation(project(":core:data:common"))
}
