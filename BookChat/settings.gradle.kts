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
include(":core:datastore:datastore")

include(":core:datastore:appsetting")
include(":core:data:appsetting:external")
include(":core:data:appsetting:internal")

include(":core:datastore:searchhistory")
include(":core:data:searchhistory:internal")
include(":core:data:searchhistory:external")

include(":core:datastore:deviceinfo")
include(":core:data:deviceinfo:internal")
include(":core:data:deviceinfo:external")

include(":core:datastore:notificationinfo")
include(":core:data:notificationinfo:internal")
include(":core:data:notificationinfo:external")

include(":core:datastore:bookchat-token")
include(":core:data:bookchat-token:internal")
include(":core:data:bookchat-token:external")

include(":core:database:chatting:internal")
include(":core:database:chatting:external")

include(":core:data:channel:internal")
include(":core:data:channel:external")

include(":core:stomp:stomp")
include(":core:stomp:chatting:internal")
include(":core:stomp:chatting:external")

include(":core:network:network")
include(":core:network:bookchat")

include(":core:network-manager:internal")
include(":core:network-manager:external")

include(":core:oauth:external")
include(":core:oauth:internal:kakao:external")
include(":core:oauth:internal:kakao:internal")
include(":core:oauth:internal:google:external")
include(":core:oauth:internal:google:internal")

include(":core:data:oauth:external")
include(":core:data:oauth:internal")

include(":core:data:user:external")
include(":core:data:user:internal")

include(":core:data:chat:external")
include(":core:data:chat:internal")

include(":core:util")

include(":core:notification:util")
include(":core:notification:chat:internal")
include(":core:notification:chat:external")

include(":core:data:fcm-token:internal")
include(":core:data:fcm-token:external")

include(":core:fcm:service")
include(":core:fcm:chat")
include(":core:fcm:renew-fcm-token")
include(":core:fcm:forced-logout")

include(":core:domain")

include(":core:data:client:external")
include(":core:data:client:internal")

include(":core:data:bookreport:internal")
include(":core:data:bookreport:external")
include(":core:data:agony:internal")
include(":core:data:agony:external")
include(":core:data:common")
include(":core:data:agonyrecord:internal")
include(":core:data:agonyrecord:external")
include(":core:data:bookshelf:internal")
include(":core:data:bookshelf:external")
include(":core:data:search:book:internal")
include(":core:data:search:book:external")
include(":core:data:search:channel:internal")
include(":core:data:search:channel:external")
include(":core:network:util")

include(":core:navigation")

include(":feature:main")
include(":feature:login")
include(":feature:mypage")
include(":feature:image-crop")
include(":feature:splash")
include(":feature:home")
include(":feature:search")
include(":feature:channel-list")
include(":feature:bookshelf")
include(":feature:agony")
include(":feature:bookreport")
include(":feature:channel")
include(":feature:create-channel")
include(":feature:signup")
