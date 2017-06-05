package eu.metatools.wepwawet2.util

/**
 * A mutation consisting of [Add] or [Remove] as alternative.
 */
abstract class Mutation<out V> internal constructor() {
    abstract val element: V
}

/**
 * An add mutation.
 */
data class Add<out V>(override val element: V) : Mutation<V>()

/**
 * A remove mutation.
 */
data class Remove<out V>(override val element: V) : Mutation<V>()