plugins {
	id("convention.android.feature")
}

android {
	namespace = "com.kova700.bookchat.feature.main"
}

dependencies {
	implementation(project(":feature:home"))
	implementation(project(":feature:bookshelf"))
	implementation(project(":feature:search"))
	implementation(project(":feature:channel-list"))
	implementation(project(":feature:mypage"))
	implementation(project(":feature:login"))
	implementation(project(":core:network-manager:external"))
}