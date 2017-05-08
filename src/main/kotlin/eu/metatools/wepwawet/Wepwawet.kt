package eu.metatools.wepwawet

import eu.metatools.wepwawet.tracking.Tracker
import org.funktionale.option.getOrElse
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

// TODO Network synchronized identities [newId]
class Wepwawet<I>(val registry: Registry<I>, val newId: () -> I) {
    interface Call

    class CallSimulated : Call {
        override fun toString() = "<<simulated>>"
    }

    class CallInit : Call {
        override fun toString() = "<<init>>"
    }

    data class CallImpulse(val kProperty: KProperty<*>) : Call {
        override fun toString() = "<<impulse ${kProperty.name}>>"
    }

    interface Dep

    data class DepInit(val kProperty: KProperty<*>) : Dep {
        override fun toString() = "init ${kProperty.name}"
    }

    data class DepRead(val kProperty: KProperty<*>) : Dep {
        override fun toString() = "read ${kProperty.name}"
    }

    data class DepWrite(val kProperty: KProperty<*>) : Dep {
        override fun toString() = "write ${kProperty.name}"
    }

    private val deps = Tracker<Call, Dep>()


    var time: Long = 0

    operator fun contains(item: Entity<I>) = false

    fun register(item: Entity<I>) {

    }

    fun release(item: Entity<I>) {
    }

    // TODO Constructor behaviour
    fun <T> staticInit(receiver: Entity<I>, property: KProperty<*>, value: T, config: Config<T>) {
        deps.touch(DepInit(property))
        registry.set(receiver.id, time, property, value)
    }

    fun <T> dynamicInit(receiver: Entity<I>, property: KProperty<*>, value: T, config: Config<T>) {
        deps.touch(DepInit(property))
        registry.set(receiver.id, time, property, value)
    }

    fun <T> staticGet(receiver: Entity<I>, property: KProperty<*>, config: Config<T>): T {
        deps.touch(DepRead(property))
        return registry
                .get(receiver.id, time, property) {
                    @Suppress("UNCHECKED_CAST")
                    val r = it as T; r
                }
                .getOrElse { error("Interpolation behaviour") }
    }

    fun <T> dynamicGet(receiver: Entity<I>, property: KProperty<*>, config: Config<T>): T {
        deps.touch(DepRead(property))
        return registry
                .get(receiver.id, time, property) {
                    @Suppress("UNCHECKED_CAST")
                    val r = it as T; r
                }
                .getOrElse { TODO("Interpolation behaviour") }
    }

    fun <T> dynamicSet(receiver: Entity<I>, property: KProperty<*>, value: T, config: Config<T>) {
        deps.touch(DepWrite(property))
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
        val touched = deps.trackIn(CallImpulse(property)) {
            receiver.impulse(value)
        }
        touched.stats()
    }

    fun <R : Entity<I>> obtain(provider: (Wepwawet<I>, I) -> R) =
            deps.trackLet(CallInit()) {
                provider(this, newId()).apply {
                    register(this)
                }
            }

    fun <R : Entity<I>, T> obtain(provider: (Wepwawet<I>, I, T) -> R, t: T) =
            deps.trackLet(CallInit()) {
                provider(this, newId(), t).apply {
                    register(this)
                }
            }

    fun <R : Entity<I>, T1, T2> obtain(provider: (Wepwawet<I>, I, T1, T2) -> R, t1: T1, t2: T2) =
            deps.trackLet(CallInit()) {
                provider(this, newId(), t1, t2).apply {
                    register(this)
                }
            }

    fun simulateImpulse(block: () -> Unit) {
        deps.trackIn(CallSimulated()) {
            block()
        }
    }

    fun stats() = registry.stats(time)

}