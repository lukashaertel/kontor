package eu.metatools.wepwawet.components

/**
 * Registry of (ID, Time, Property) to value assignments.
 */
interface RevisionTable {
    /**
     * Write [value] to the parent for [id]'s [property] at [time].
     */
    fun set(id: Int, time: Long, property: kotlin.reflect.KProperty<*>, value: Any?)

    /**
     * Clears the value of [id]'s [property] at [time].
     */
    fun clear(id: Int, time: Long, property: kotlin.reflect.KProperty<*>)

    /**
     * Clears the values of [id]'s [property] in [range].
     */
    fun clearAll(id: Int, range: LongRange, property: kotlin.reflect.KProperty<*>)

    /**
     * Clears the values of [id]'s [property].
     */
    fun clearAll(id: Int, property: kotlin.reflect.KProperty<*>)

    /**
     * Gets the value from the parent for [id]'s [property] at [time], proceeds with [handle] or returns null.
     */
    fun <T> get(id: Int, time: Long, property: kotlin.reflect.KProperty<*>, handle: (Any?) -> T): org.funktionale.option.Option<T>

    /**
     * Gets the value from the parent for [id]'s [property] in [range], proceeds with [handle] or returns null.
     */
    fun <T> getAll(id: Int, range: LongRange, property: kotlin.reflect.KProperty<*>, handle: (Map<Long, Any?>) -> T): T

    /**
     * Gets the value from the parent for [id]'s [property] , proceeds with [handle] or returns null.
     */
    fun <T> getAll(id: Int, property: kotlin.reflect.KProperty<*>, handle: (Map<Long, Any?>) -> T): T

    /**
     * Prints the stats of the parent, might be unsupported and just print the [toString].
     */
    fun stats(currentTime: Long) {
        println(toString())
    }
}