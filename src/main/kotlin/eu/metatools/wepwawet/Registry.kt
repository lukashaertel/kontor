package eu.metatools.wepwawet

import org.funktionale.option.Option
import kotlin.reflect.KProperty

interface Registry<in I> {
    /**
     * Write [value] to the parent for [id]'s [property] at [time].
     */
    fun set(id: I, time: Long, property: KProperty<*>, value: Any?)

    /**
     * Clears the value of [id]'s [property] at [time].
     */
    fun clear(id: I, time: Long, property: KProperty<*>)

    /**
     * Clears the values of [id]'s [property] in [range].
     */
    fun clearAll(id: I, range: LongRange, property: KProperty<*>)

    /**
     * Clears the values of [id]'s [property].
     */
    fun clearAll(id: I, property: KProperty<*>)

    /**
     * Gets the value from the parent for [id]'s [property] at [time], proceeds with [handle] or returns null.
     */
    fun <T> get(id: I, time: Long, property: KProperty<*>, handle: (Any?) -> T): Option<T>

    /**
     * Gets the value from the parent for [id]'s [property] in [range], proceeds with [handle] or returns null.
     */
    fun <T> getAll(id: I, range: LongRange, property: KProperty<*>, handle: (Map<Long, Any?>) -> T): T

    /**
     * Gets the value from the parent for [id]'s [property] , proceeds with [handle] or returns null.
     */
    fun <T> getAll(id: I, property: KProperty<*>, handle: (Map<Long, Any?>) -> T): T

    /**
     * Prints the stats of the parent, might be unsupported and just print the [toString].
     */
    fun stats(currentTime:Long) {
        println(toString())
    }
}