package eu.metatools.wepwawet

import eu.metatools.wepwawet.calls.*
import eu.metatools.wepwawet.tracking.Tracker
import eu.metatools.wepwawet.delegates.*
import eu.metatools.wepwawet.components.RevisionTable
import eu.metatools.wepwawet.net.Net
import org.funktionale.option.getOrElse
import kotlin.reflect.KProperty


// TODO Network synchronized identities [newId]
class Wepwawet(
        val revisionTable: RevisionTable,
        val net: Net) {

    private val tracker = Tracker()

    var time: Long = 0

    operator fun contains(item: Entity) = false

    fun register(item: Entity) {

    }

    fun release(item: Entity) {
        net.releaseId(item.id)
    }

    fun <R : Entity> obtain(provider: (Wepwawet, Int) -> R) =
            tracker.trackLet(CallInit()) {
                provider(this, net.getAndLeaseId()).apply {
                    register(this)
                }
            }

    fun <R : Entity, T> obtain(provider: (Wepwawet, Int, T) -> R, t: T) =
            tracker.trackLet(CallInit()) {
                provider(this, net.getAndLeaseId(), t).apply {
                    register(this)
                }
            }

    fun <R : Entity, T1, T2> obtain(provider: (Wepwawet, Int, T1, T2) -> R, t1: T1, t2: T2) =
            tracker.trackLet(CallInit()) {
                provider(this, net.getAndLeaseId(), t1, t2).apply {
                    register(this)
                }
            }

    // TODO Constructor behaviour
    fun <R : Entity, T> staticInit(receiver: Entity, property: KProperty<*>, static: Static<R, T>) {
        tracker.touch(DepInit(property))
        revisionTable.set(receiver.id, time, property, static.config.default)
    }

    fun <R : Entity, T> dynamicInit(receiver: Entity, property: KProperty<*>, dynamic: Dynamic<R, T>) {
        tracker.touch(DepInit(property))
        revisionTable.set(receiver.id, time, property, dynamic.config.default)
    }

    fun <R : Entity, T> staticGet(receiver: Entity, property: KProperty<*>, static: Static<R, T>): T {
        tracker.touch(DepRead(property))
        return revisionTable
                .get(receiver.id, time, property) {
                    @Suppress("UNCHECKED_CAST")
                    val r = it as T; r
                }
                .getOrElse { error("Interpolation behaviour") }
    }

    fun <R : Entity, T> dynamicGet(receiver: Entity, property: KProperty<*>, dynamic: Dynamic<R, T>): T {
        tracker.touch(DepRead(property))
        return revisionTable
                .get(receiver.id, time, property) {
                    @Suppress("UNCHECKED_CAST")
                    val r = it as T; r
                }
                .getOrElse { TODO("Interpolation behaviour") }
    }

    fun <R : Entity, T> dynamicSet(receiver: Entity, property: KProperty<*>, value: T, dynamic: Dynamic<R, T>) {
        tracker.touch(DepWrite(property))
        revisionTable.set(receiver.id, time, property, value)
    }


    // TODO Put this in a nice class
    private val fnReg = hashMapOf<KProperty<*>, Function2<*, *, Unit>>()

    fun <R : Entity, T> registerStatic(property: KProperty<*>, static: Static<R, T>) {
        //TODO()
    }

    fun <R : Entity, T> registerDynamic(property: KProperty<*>, dynamic: Dynamic<R, T>) {
        //TODO()
    }

    fun <R : Entity> registerUpdate(property: KProperty<*>, update: Update<R>) {
        //TODO()
    }

    fun <R : Entity, T> registerImpulse(property: KProperty<*>, impulse: Impulse<R, T>) {
        fnReg.put(property, impulse.block)
    }


    fun <R : Entity, T> getImpulse(property: KProperty<*>): R.(T) -> Unit {
        @Suppress("UNCHECKED_CAST")
        val r = fnReg.getOrElse(property) { error("Unknown block $property") } as R.(T) -> Unit
        return r
    }

    fun <R : Entity> updateExecute(receiver: R, property: KProperty<*>, update: Update<R>) {
        val touched = tracker.trackIn(CallUpdate(property)) {
            update.block(receiver)
        }
        touched.stats()
    }

    fun <R : Entity, T> impulseExecute(receiver: R, property: KProperty<*>, value: T, impulse: Impulse<R, T>) {
        val touched = tracker.trackIn(CallImpulse(property)) {
            impulse.block(receiver, value)
        }
        touched.stats()
    }

    fun simulateImpulse(block: () -> Unit) {
        val touched = tracker.trackIn(CallSimulated()) {
            block()
        }
        touched.stats()
    }

    fun stats() = revisionTable.stats(time)

}