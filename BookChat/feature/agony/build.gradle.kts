plugins {
	id("convention.android.feature")
}

android {
	namespace = "com.kova700.bookchat.feature.agony"
	buildFeatures.viewBinding = true
}

dependencies {
	//Facebook.Shimmer
	implementation(libs.shimmer)
	implementation(project(":core:data:bookshelf:external"))
	implementation(project(":core:data:agony:external"))
	implementation(project(":core:data:agonyrecord:external"))
	implementation(project(":core:data:user:external"))
	implementation(project(":core:data:search:book:external"))
	implementation(project(":core:data:common"))
}