package eu.metatools.wepwawet

import kotlin.reflect.KProperty

/**
 * Abstract delegate.
 */
interface Delegate<in R, T> {
    /**
     * Gets the value.
     */
    operator fun getValue(r: R, p: KProperty<*>): T

    /**
     * Sets the value.
     */
    operator fun setValue(receiver: R, property: KProperty<*>, value: T) {
        throw IllegalStateException("Property $property is not settable")
    }
}

/**
 * Provider of a delegate.
 */
interface Provider<in R, T> {
    /**
     * Provides the delegate.
     */
    operator fun provideDelegate(receiver: R, property: KProperty<*>): Delegate<R, T>
}

/**
 * Implements a provider of a delegate.
 */
inline fun <R, T> Provider(crossinline provideDelegate: (R, KProperty<*>) -> Delegate<R, T>) = object : Provider<R, T> {
    override fun provideDelegate(receiver: R, property: KProperty<*>) =
            provideDelegate(receiver, property)

}