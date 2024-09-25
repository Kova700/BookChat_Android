plugins {
	`kotlin-dsl`
}

dependencies {
	implementation(libs.gradle.plugin.android)
	implementation(libs.gradle.plugin.kotlin)
	implementation(libs.gradle.plugin.ksp)
	implementation(libs.gradle.plugin.hilt)
}