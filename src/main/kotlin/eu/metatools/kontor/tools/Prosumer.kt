package eu.metatools.kontor.tools

import kotlin.reflect.KMutableProperty0

/**
 * Producer and consumer in one.
 */
interface Prosumer<T> {
    companion object {
        /**
         * Discards incoming messages and provides [Unit].
         */
        val DISCARD = object : Prosumer<Unit> {
            override fun get() = Unit

            override fun set(value: Unit) {}
        }
    }

    /**
     * Gets the value from the producer part.
     */
    fun get(): T

    /**
     * Receives the value as the consumer part.
     */
    fun set(value: T)
}

/**
 * Converts a mutable property to a prosumer.
 */
fun <T> KMutableProperty0<T>.toProsumer() = object : Prosumer<T> {
    override fun get(): T {
        return this@toProsumer.get()
    }

    override fun set(value: T) {
        this@toProsumer.set(value)
    }
}