import java.io.FileInputStream
import java.util.Properties

var properties = Properties().apply {
	load(FileInputStream("./core/stomp/chatting/internal/local.properties"))
}

plugins {
	id("convention.android.library")
	id("convention.android.hilt")
	alias(libs.plugins.kotlin.serialization)
}

android {
	namespace = "com.kova700.bookchat.core.stomp.internal"

	defaultConfig.buildConfigField(
		type = "String",
		name = "STOMP_CONNECTION_URL",
		value = properties.getProperty("STOMP_CONNECTION_URL")
	)
	buildFeatures.buildConfig = true
}

dependencies {
	implementation(libs.bundles.krossbow.stomp)
	implementation(libs.kotlinx.serialization.json)
	implementation(libs.bundles.unit.test)

	implementation(project(":core:stomp:chatting:external"))
	implementation(project(":core:stomp:stomp"))
	implementation(project(":core:data:channel:external"))
	implementation(project(":core:data:chat:external"))
	implementation(project(":core:data:client:external"))
	implementation(project(":core:data:bookchat-token:external"))
	implementation(project(":core:data:user:external"))
	implementation(project(":core:network-manager:external"))
	implementation(project(":core:domain"))
	implementation(project(":core:util"))
	implementation(project(":core:notification:chat:external"))
}