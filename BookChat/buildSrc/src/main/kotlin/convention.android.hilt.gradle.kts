import gradle.configure.libs
import org.gradle.kotlin.dsl.dependencies

plugins {
	id("com.google.devtools.ksp")
	id("dagger.hilt.android.plugin")
}

dependencies {
	"ksp"(libs.findLibrary("hilt-android-compiler").get())
	"implementation"(libs.findLibrary("hilt-android").get())
}