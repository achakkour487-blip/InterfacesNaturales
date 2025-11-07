pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://jitpack.io") // 🔹 NECESARIO PARA SceneView
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google() // 🔹 Para MLKit, CameraX, etc.
        mavenCentral()
        maven("https://jitpack.io") // 🔹 Para SceneView
    }
}

rootProject.name = "reconocimientopostural"
include(":app")
