plugins {
	id("convention.android.feature")
}
android {
	namespace = "com.kova700.bookchat.feature.createchannel"
	buildFeatures.viewBinding = true
}
dependencies {
	implementation(project(":feature:search"))
	implementation(project(":core:data:search:book:external"))
	implementation(project(":core:data:channel:external"))
}
