plugins {
	id("convention.android.application")
	id("convention.android.hilt")
	alias(libs.plugins.secrets.gradle.plugin)
	alias(libs.plugins.google.fcm)
	alias(libs.plugins.aboutlibraries)
}

android {
	namespace = "com.kova700.bookchat"
	defaultConfig {
		applicationId = "com.kova700.bookchat"
		val majorVersion = libs.versions.major.get()
		val minorVersion = libs.versions.minor.get()
		val hotfixVersion = libs.versions.hotfix.get()
		versionCode = libs.versions.versionCode.get().toInt()
		versionName = "$majorVersion.$minorVersion.$hotfixVersion"
	}
}

dependencies {
	implementation(libs.bundles.androidx.default)
// WorkManager
	implementation(libs.bundles.workmanager)
	ksp(libs.androidx.hilt.compiler)

	//splash는 default Activity라서 지우면 안됨
	implementation(project(":feature:splash"))
	//아래 feature 지우면 Navigation 에러남
	implementation(project(":feature:agony"))
	implementation(project(":feature:bookreport"))
	implementation(project(":feature:channel"))
	implementation(project(":feature:create-channel"))
	implementation(project(":feature:image-crop"))
	implementation(project(":feature:login"))
	implementation(project(":feature:main"))
	implementation(project(":feature:signup"))

	implementation(project(":core:design-system"))
	implementation(project(":core:fcm:forced-logout"))
	implementation(project(":core:fcm:service"))
	implementation(project(":core:stomp:chatting:internal"))
	implementation(project(":core:network-manager:internal"))
	implementation(project(":core:oauth:external"))
	implementation(project(":core:oauth:internal:kakao:internal"))
	implementation(project(":core:oauth:internal:google:internal"))
	implementation(project(":core:notification:chat:internal"))
	implementation(project(":core:database:chatting:internal"))
	implementation(project(":core:data:appsetting:internal"))
	implementation(project(":core:data:searchhistory:internal"))
	implementation(project(":core:data:deviceinfo:internal"))
	implementation(project(":core:data:notificationinfo:internal"))
	implementation(project(":core:data:bookchat-token:internal"))
	implementation(project(":core:data:channel:internal"))
	implementation(project(":core:data:user:internal"))
	implementation(project(":core:data:client:internal"))
	implementation(project(":core:data:fcm-token:internal"))
	implementation(project(":core:data:chat:internal"))
	implementation(project(":core:data:search:channel:internal"))
	implementation(project(":core:data:search:book:internal"))
	implementation(project(":core:data:bookreport:internal"))
	implementation(project(":core:data:agony:internal"))
	implementation(project(":core:data:agonyrecord:internal"))
	implementation(project(":core:data:bookshelf:internal"))
	implementation(project(":core:data:oauth:internal"))
}