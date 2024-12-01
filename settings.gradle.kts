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
            url = uri("https://repository.map.naver.com/archive/maven")
        }
        maven {
            url = uri("https://jitpack.io")
        }
        maven {
            url = uri("https://maven.google.com")
        }
    }
}

rootProject.name = "My Application333"
include(":app")