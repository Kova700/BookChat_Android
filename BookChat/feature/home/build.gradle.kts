plugins {
	id("convention.android.feature")
}

android {
	namespace = "com.kova700.bookchat.feature.home"
	buildFeatures.viewBinding = true
}

dependencies {
	//Flex box
	implementation(libs.flexbox)
	//Facebook.Shimmer
	implementation(libs.shimmer)
	implementation(project(":core:data:user:external"))
	implementation(project(":core:data:chat:external"))
	implementation(project(":core:data:common"))
	implementation(project(":core:data:client:external"))
	implementation(project(":core:data:channel:external"))
	implementation(project(":core:data:search:book:external"))
	implementation(project(":core:data:bookshelf:external"))
}