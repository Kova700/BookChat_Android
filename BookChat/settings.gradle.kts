pluginManagement {
	repositories {
		gradlePluginPortal()
		google()
		mavenCentral()
	}
}
dependencyResolutionManagement {
	repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
	repositories {
		google()
		mavenCentral()
		maven {
			url = java.net.URI("https://devrepo.kakao.com/nexus/content/groups/public/")
		} //Kakao SDK
		maven { url = java.net.URI("https://jitpack.io") } //Image Cropper
	}
}
rootProject.name = "BookChat"
include(":app")
include(":core:design-system")
include(":feature:main")
include(":core:datastore:datastore")
include(":core:datastore:appsetting")
include(":core:data:appsetting:external")
include(":core:data:appsetting:internal")
