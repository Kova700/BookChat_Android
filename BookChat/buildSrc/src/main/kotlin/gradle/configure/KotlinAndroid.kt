package gradle.configure

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal fun Project.configureKotlinAndroid(commonExtension: CommonExtension<*, *, *, *, *, *>) {
	with(commonExtension) {
		compileSdk = libs.findVersion("compileSdk").get().requiredVersion.toInt()

		defaultConfig {
			minSdk = libs.findVersion("minSdk").get().requiredVersion.toInt()
			testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
		}

		compileOptions {
			sourceCompatibility = JavaVersion.VERSION_17
			targetCompatibility = JavaVersion.VERSION_17
		}
	}
	configureKotlin()
}

private fun Project.configureKotlin() {
	tasks.withType<KotlinCompile>().configureEach {
		compilerOptions {
			jvmTarget.set(JvmTarget.JVM_17)
		}
	}
}