pluginManagement {
    repositories {
        maven {
            name = "Garden of Fancy"
            url = uri("https://maven.gofancy.wtf/releases")
        }
        maven {
            name = "forge"
            url = uri("https://maven.minecraftforge.net")
        }
        gradlePluginPortal()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id.startsWith("org.jetbrains.kotlin")) useVersion("1.9.+")
            else when (requested.id.id) {
                "net.minecraftforge.gradle.forge" -> useModule("net.minecraftforge.gradle:ForgeGradle:6.+")
                "org.jetbrains.dokka" -> useVersion("1.+")
                "com.jfrog.bintray" -> useVersion("1.+")
                "com.jfrog.artifactory" -> useVersion("4.+")
            }
        }
    }
}

rootProject.name = "LibrarianLib"