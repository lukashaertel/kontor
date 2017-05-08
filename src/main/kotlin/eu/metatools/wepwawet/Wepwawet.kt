package eu.metatools.wepwawet

import org.funktionale.option.getOrElse
import java.util.*
import kotlin.reflect.KCallable
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty

/**
 * Created by pazuzu on 5/5/17.
 */

// TODO Network synchronized identities [newId]
class Wepwawet<I>(val registry: Registry<I>, val newId: () -> I) {
    private var call = null as KProperty<*>?

    private val depends = arrayListOf<KProperty<*>>()

    private val writes = arrayListOf<KProperty<*>>()

    var time: Long = 0

    operator fun contains(item: Entity<I>) = false

    fun register(item: Entity<I>) {

    }

    fun release(item: Entity<I>) {
    }

    // TODO Constructor behaviour
    fun <T> staticInit(receiver: Entity<I>, property: KProperty<*>, value: T, config: Config<T>) {
        registry.set(receiver.id, time, property, value)
    }

    fun <T> dynamicInit(receiver: Entity<I>, property: KProperty<*>, value: T, config: Config<T>) {
        registry.set(receiver.id, time, property, value)
    }

    fun <T> staticGet(receiver: Entity<I>, property: KProperty<*>, config: Config<T>): T {
        depends += property
        return registry
                .get(receiver.id, time, property) {
                    @Suppress("UNCHECKED_CAST")
                    val r = it as T; r
                }
                .getOrElse { error("Interpolation behaviour") }
    }

    fun <T> dynamicGet(receiver: Entity<I>, property: KProperty<*>, config: Config<T>): T {
        depends += property
        return registry
                .get(receiver.id, time, property) {
                    @Suppress("UNCHECKED_CAST")
                    val r = it as T; r
                }
                .getOrElse { TODO("Interpolation behaviour") }
    }

    fun <T> dynamicSet(receiver: Entity<I>, property: KProperty<*>, value: T, config: Config<T>) {
        writes += property
        registry.set(receiver.id, time, property, value)
    }


    // TODO Put this in a nice class
    private val fnReg = hashMapOf<KProperty<*>, Function2<*, *, Unit>>()

    fun <R : Entity<I>, T> registerImpulse(property: KProperty<*>, impulse: R.(T) -> Unit) {
        fnReg.put(property, impulse)
    }

    fun <R : Entity<I>, T> getImpulse(property: KProperty<*>): R.(T) -> Unit {
        @Suppress("UNCHECKED_CAST")
        val r = fnReg.getOrElse(property) { error("Unknown impulse $property") } as R.(T) -> Unit
        return r
    }

    fun <R : Entity<I>, T> impulseExecute(receiver: R, property: KProperty<*>, value: T, impulse: R.(T) -> Unit) {
        call = property

        receiver.impulse(value)

        println(call)
        println("Depends $depends")
        println("Writes $writes")

        call = null
        depends.clear()
        writes.clear()
    }

    inline fun <reified R : Entity<I>> obtain(provider: (Wepwawet<I>, I) -> R) =
            provider(this, newId()).apply { register(this) }

    inline fun <reified R : Entity<I>, T> obtain(provider: (Wepwawet<I>, I, T) -> R, t: T) =
            provider(this, newId(), t).apply { register(this) }

    inline fun <reified R : Entity<I>, T1, T2> obtain(provider: (Wepwawet<I>, I, T1, T2) -> R, t1: T1, t2: T2) =
            provider(this, newId(), t1, t2).apply { register(this) }


    fun stats() = registry.stats(time)

}