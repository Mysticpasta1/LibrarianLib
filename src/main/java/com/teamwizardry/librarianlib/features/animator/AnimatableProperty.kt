package com.teamwizardry.librarianlib.features.animator

/**
 * A property that can be animated.
 * [keyPath] is simply a sequence of fields to follow, with additional "virtual" fields available when registered with
 * the [VirtualFieldAccessorHandler]. Call [VirtualFieldAccessorHandler.printAvailable], optionally passing it a type,
 * to see what virtual fields are available and what their types are.
 */
class AnimatableProperty<T : Any> private constructor(val target: Class<T>, val keyPath: String) {

    private val getter: (target: T) -> Any?
    private val setter: (target: T, value: Any?) -> Unit
    val type: Class<*>

    init {
        val response = generateGetterAndSetterForKeyPath(target, keyPath.split(".").toTypedArray())
        getter = response.first
        setter = response.second
        type = response.third
    }

    fun get(target: T): Any {
        return getter(target) ?: throw NullPointerException("Cannot have null value in animation!")
    }
    fun set(target: T, value: Any) {
        setter(target, value)
    }

    companion object {
        private var map = mutableMapOf<Pair<Class<*>, String>, AnimatableProperty<*>>()
        fun <T: Any> get(target: Class<T>, keyPath: String): AnimatableProperty<T> {
            val key = Pair(target, keyPath)
            if(key !in map) {
                map[key] = AnimatableProperty(target, keyPath)
            }

            @Suppress("UNCHECKED_CAST")
            return map[key] as AnimatableProperty<T>
        }
    }
}
