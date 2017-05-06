package eu.metatools.wepwawet

import org.funktionale.option.Option
import java.util.*
import kotlin.reflect.KProperty

/**
 * A basic parent using a tree map of hash maps to store values.
 */
class MapRegistry<in I> : Registry<I> {
    /**
     * The value to return for a non-existent key
     */
    private val nonexistent = Any()

    /**
     * The main map.
     */
    private val main = TreeMap<Long, MutableMap<Pair<I, KProperty<*>>, Any?>>()

    override fun set(id: I, time: Long, property: KProperty<*>, value: Any?) {
        main.getOrPut(time) { hashMapOf() }.put(id to property, value)
    }

    override fun clear(id: I, time: Long, property: KProperty<*>) {
        main[time]?.remove(id to property)
    }

    override fun clearAll(id: I, range: LongRange, property: KProperty<*>) {
        if (range.step != 1L)
            clearAllArb(id, range, property)
        else
            clearAllConsec(id, range, property)
    }


    /**
     * [clearAll] in a non-consecutive range.
     */
    private fun clearAllArb(id: I, range: LongRange, property: KProperty<*>) {
        for (time in range)
            clear(id, time, property)
    }

    /**
     * [clearAll] in a consecutive range.
     */
    private fun clearAllConsec(id: I, range: LongRange, property: KProperty<*>) {
        for (vs in main.subMap(range.start, true, range.endInclusive, true).values)
            vs.remove(id to property)
    }

    override fun clearAll(id: I, property: KProperty<*>) {
        for (vs in main.values)
            vs.remove(id to property)
    }

    override fun <T> get(id: I, time: Long, property: KProperty<*>, handle: (Any?) -> T): Option<T> {
        // Get map entry
        val m = main[time] ?: return Option.None
        val v = m.getOrDefault(id to property, nonexistent)

        // Check if it was not present, allows dealing with explicit null values
        if (v === nonexistent)
            return Option.None
        else
            return Option.Some(handle(v))
    }

    override fun <T> getAll(id: I, range: LongRange, property: KProperty<*>, handle: (Map<Long, Any?>) -> T): T {
        if (range.step != 1L)
            return getAllArb(id, range, property, handle)
        else
            return getAllConsec(id, range, property, handle)
    }


    /**
     * [getAll] in a non-consecutive range.
     */
    private fun <T> getAllArb(id: I, range: LongRange, property: KProperty<*>, handle: (Map<Long, Any?>) -> T): T {
        val all = main
                .filterKeys { it in range }
                .mapValues { (_, v) ->
                    v.getOrDefault(id to property, nonexistent)
                }
                .filterValues {
                    it !== nonexistent
                }

        return handle(all)
    }


    /**
     * [getAll] in a consecutive range.
     */
    private fun <T> getAllConsec(id: I, range: LongRange, property: KProperty<*>, handle: (Map<Long, Any?>) -> T): T {
        val all = main
                .subMap(range.start, true, range.endInclusive, true)
                .mapValues { (_, v) ->
                    v.getOrDefault(id to property, nonexistent)
                }
                .filterValues {
                    it !== nonexistent
                }

        return handle(all)
    }

    override fun <T> getAll(id: I, property: KProperty<*>, handle: (Map<Long, Any?>) -> T): T {
        val all = main
                .mapValues { (_, v) ->
                    v.getOrDefault(id to property, nonexistent)
                }
                .filterValues {
                    it !== nonexistent
                }

        return handle(all)
    }

    /**
     * Prints the the asserted and current time, as well as the entity assignments.
     */
    override fun stats(currentTime: Long) {
        // Get two axis
        val defs = main.values
                .flatMap { it.keys }
                .distinct()
                .groupBy { it.first }
                .mapValues { it.value.map { it.second } }
        val times = main.keys
                .distinct()


        for ((id, props) in defs) {
            println("Entry $id:")
            for (prop in props) {
                print(" ${prop.name} = ")
                var sep = false
                for (time in times) {
                    if (sep) print(' ')
                    if (time == currentTime) print('[')
                    print("$time: ${main[time]?.get(id to prop) ?: "---"}")
                    if (time == currentTime) print(']')
                    sep = true
                }
                println()
            }
        }
    }
}