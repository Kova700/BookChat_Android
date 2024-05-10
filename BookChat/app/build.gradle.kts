import java.io.FileInputStream
import java.util.Properties

var properties = Properties()
properties.load(FileInputStream("local.properties"))

plugins {
	id("com.android.application")
	id("org.jetbrains.kotlin.android")
	id("kotlin-kapt")
	id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
	id("com.google.dagger.hilt.android")
	id("com.google.gms.google-services")
}

android {
	namespace = "com.example.bookchat"
	compileSdk = 34

	defaultConfig {
		applicationId = "com.example.bookchat"
		minSdk = 24
		targetSdk = 34
		versionCode = 1
		versionName = "1.0"
		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
//		buildConfigField("String", "UNSPLASH_ACCESS_KEY", properties.getProperty("UNSPLASH_ACCESS_KEY"))
	}

	buildTypes {
		release {
			isMinifyEnabled = false
			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"),
				"proguard-rules.pro"
			)
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}
	kotlinOptions {
		jvmTarget = JavaVersion.VERSION_17.toString()
	}
	buildFeatures {
		dataBinding = true
		buildConfig = true
	}
}

dependencies {
	implementation("androidx.core:core-ktx:1.13.1")
	implementation("androidx.appcompat:appcompat:1.6.1")
	implementation("com.google.android.material:material:1.12.0")
	implementation("androidx.constraintlayout:constraintlayout:2.1.4")
	implementation("androidx.recyclerview:recyclerview:1.3.2")
	implementation("androidx.fragment:fragment-ktx:1.7.0")
	implementation("androidx.legacy:legacy-support-v4:1.0.0")
	testImplementation("junit:junit:4.13.2")
	androidTestImplementation("androidx.test.ext:junit:1.1.5")
	androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
	// Jetpack Navigation
	implementation("androidx.navigation:navigation-runtime-ktx:2.7.7")
	implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
	implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
	// Kakao Login
	implementation("com.kakao.sdk:v2-user:2.11.0")
	// Google Login
	implementation("com.google.android.gms:play-services-auth:21.1.1")
	//Firebase BoM(Bill of Materials)
	implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
	//Firebase Analytics
	implementation("com.google.firebase:firebase-analytics-ktx")
	//Firebase FCM
	implementation("com.google.firebase:firebase-messaging-ktx:24.0.0")
	// Retrofit
	implementation("com.squareup.retrofit2:retrofit:2.9.0")
	// Gson converter
	implementation("com.squareup.retrofit2:converter-gson:2.9.0")
	// Glide
	implementation("com.github.bumptech.glide:glide:4.16.0")
	kapt("com.github.bumptech.glide:compiler:4.13.0")
	// ViewModel
	implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
	//activity-ktx
	implementation("androidx.fragment:fragment-ktx:1.7.0")
	implementation("androidx.activity:activity-ktx:1.9.0")
	//Image Cropper
	implementation("com.github.CanHub:Android-Image-Cropper:4.3.1")
	//Preferences DataStore
	implementation("androidx.datastore:datastore-preferences:1.1.1")
	//SimpleRatingBar
	implementation("com.github.ome450901:SimpleRatingBar:1.5.1")
	//Hilt
	implementation("com.google.dagger:hilt-android:2.48.1")
	kapt("com.google.dagger:hilt-compiler:2.48.1")
	//Flex box
	implementation("com.google.android.flexbox:flexbox:3.0.0")
	//Facebook.Shimmer
	implementation("com.facebook.shimmer:shimmer:0.5.0")
	//krossbow-stomp
	implementation("org.hildan.krossbow:krossbow-stomp-core:5.1.0")
	implementation("org.hildan.krossbow:krossbow-websocket-okhttp:5.1.0")
	//OkHttp
	implementation(platform("com.squareup.okhttp3:okhttp-bom:4.10.0"))
	implementation("com.squareup.okhttp3:okhttp")
	implementation("com.squareup.okhttp3:logging-interceptor")
	//Room
	implementation("androidx.room:room-runtime:2.6.1")
	kapt("androidx.room:room-compiler:2.6.1")
	implementation("androidx.room:room-ktx:2.6.1")
	implementation("androidx.room:room-paging:2.6.1")
}

kapt {
	correctErrorTypes = true
}