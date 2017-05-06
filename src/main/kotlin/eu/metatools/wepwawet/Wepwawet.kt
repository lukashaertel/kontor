package eu.metatools.wepwawet

import org.funktionale.option.getOrElse
import java.util.*
import kotlin.reflect.KProperty

/**
 * Created by pazuzu on 5/5/17.
 */
class Wepwawet<I>(val registry: Registry<I>) {
    val calls = Stack<Action<*, I>>()

    var time: Long = 0

    operator fun contains(item: Entity<I>) = false

    fun register(item: Entity<I>) {

    }

    fun release(item: Entity<I>) {
    }

    fun <T> staticGet(receiver: Entity<I>, property: KProperty<*>, config: Config<T>): T {
        return registry
                .get(receiver.id, time, property) {
                    @Suppress("UNCHECKED_CAST")
                    val r = it as T; r
                }
                .getOrElse { error("Interpolation behaviour") }
    }

    fun <T> staticInit(receiver: Entity<I>, property: KProperty<*>, value: T, config: Config<T>) {
        registry.set(receiver.id, time, property, value)
    }

    fun <T> dynamicGet(receiver: Entity<I>, property: KProperty<*>, config: Config<T>): T {
        return registry
                .get(receiver.id, time, property) {
                    @Suppress("UNCHECKED_CAST")
                    val r = it as T; r
                }
                .getOrElse { TODO("Interpolation behaviour") }
    }

    fun <T> dynamicSet(receiver: Entity<I>, property: KProperty<*>, value: T, config: Config<T>) {
        registry.set(receiver.id, time, property, value)
    }

    fun <T> dynamicInit(receiver: Entity<I>, property: KProperty<*>, value: T, config: Config<T>) {
        registry.set(receiver.id, time, property, value)
    }


    fun stats() = registry.stats(time)

    fun trackedExecute(receiver: Entity<I>, property: KProperty<*>, action: Action<*, I>) {
        calls.push(action)
        action.execute() // TODO Track variables
        calls.pop()
    }
}