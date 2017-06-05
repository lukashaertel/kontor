package eu.metatools.wepwawet.util

import kotlin.reflect.KProperty

/**
 * Abstract interface for providers so that they can be implemented by an anonymous object.
 */
interface Provider<in R, out T> {
    operator fun provideDelegate(receiver: R, property: KProperty<*>): T
}

/**
 * Inline variant of providing a delegate.
 * @param R The receiver type
 * @param T The delegate type
 * @param block The block to run for the delegate
 */
inline fun <R, T> provideDelegate(crossinline block: (R, KProperty<*>) -> T) = object : Provider<R, T> {
    override fun provideDelegate(receiver: R, property: KProperty<*>): T {
        return block(receiver, property)
    }
}