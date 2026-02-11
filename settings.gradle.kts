pluginManagement {
    repositories {
        // แก้ไข: ลบ content { ... } ออกให้เหลือแค่นี้
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "My Application" // หรือชื่อโปรเจกต์ของคุณ
include(":app")
