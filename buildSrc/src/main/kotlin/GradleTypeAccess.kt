import org.gradle.api.Project
import org.gradle.api.Named
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.named

typealias CommonConfigPlugin = com.teamwizardry.gradle.CommonConfigPlugin
typealias CommonConfigExtension = com.teamwizardry.gradle.CommonConfigExtension
typealias LibLibModulePlugin = com.teamwizardry.gradle.module.LibLibModulePlugin
typealias ModuleExtension = com.teamwizardry.gradle.module.ModuleExtension
typealias ModPublishingPlugin = com.teamwizardry.gradle.publish.ModPublishingPlugin
typealias ModPublishingExtension = com.teamwizardry.gradle.publish.ModPublishingExtension

typealias GenerateFabricModJson = com.teamwizardry.gradle.task.GenerateFabricModJson
typealias ValidateMixinApplication = com.teamwizardry.gradle.task.ValidateMixinApplication
typealias ShadowSources = com.teamwizardry.gradle.task.ShadowSources
typealias CopyFreemarker = com.teamwizardry.gradle.task.CopyFreemarker
typealias RestyleDokka = com.teamwizardry.gradle.task.RestyleDokka
typealias ReplaceTextInPlace = com.teamwizardry.gradle.task.ReplaceTextInPlace


inline fun <reified T: Named> Project.namedAttribute(value: String): T = objects.named(T::class.java, value)

fun Configuration.canBe(consumed: Boolean, resolved: Boolean) {
    isCanBeConsumed = consumed
    isCanBeResolved = resolved
}

/**
 * Add a dependency to the `shade` configuration. This automatically disables transitive dependencies.
 */
fun DependencyHandler.shade(dependencyNotation: Any): Dependency? {
    val dep = add("shade", dependencyNotation)
    if(dep is ModuleDependency) {
        dep.isTransitive = false
    }
    return dep
}

fun Project.configureFabricModJson(block: GenerateFabricModJson.() -> Unit) {
    this.tasks.named("generateFabricMod", block)
}

fun Project.configureFabricTestModJson(block: GenerateFabricModJson.() -> Unit) {
    this.tasks.named("generateFabricTestMod", block)
}
