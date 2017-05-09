package eu.metatools.wepwawet

import eu.metatools.wepwawet.calls.*
import eu.metatools.wepwawet.components.EntityTable
import eu.metatools.wepwawet.tracking.Tracker
import eu.metatools.wepwawet.delegates.*
import eu.metatools.wepwawet.components.RevisionTable
import eu.metatools.wepwawet.net.Net
import eu.metatools.wepwawet.tools.diff
import org.funktionale.option.getOrElse
import kotlin.reflect.KProperty


// TODO Network synchronized identities [newId]
class Wepwawet(
        val revisionTable: RevisionTable,
        val entityTable: EntityTable,
        val net: Net) {

    private val tracker = Tracker()

    var time: Long = 0

    operator fun contains(item: Entity) = entityTable[item.id] == item

    fun register(item: Entity) {
        if (entityTable.putIfAbsent(item.id, item) != null)
            throw IllegalStateException("$item maps to already registered identity")
    }

    fun release(item: Entity) {
        if (entityTable.remove(item.id, item))
            net.releaseId(item.id)
        else
            throw IllegalStateException("$item is not registered")
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

    // TODO Minify call stacks, a lot of var/val behaviour is local and does not need indirection
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
        val reads = touched.allDependencies.filterIsInstance<DepRead>().map { it.kProperty }.toSet()
        val (under, over) = reads diff update.depends
        if (under.isNotEmpty())
            throw IllegalStateException("Under-declared dependencies for ${property.name}: $under")
        if (over.isNotEmpty())
            println("Over-declared dependencies for ${property.name}: $over")
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

    fun stats() =
            revisionTable.stats(time)

    fun start(block: () -> Unit) {
        // TODO: Lobby data, i.e., game configuration needs to be exchanged and provided to block.
        // TODO: [block] runs as the initialization function for a synchronous start and serves as game initialization
        // TODO: [block] does not run on late join, instead, [revisionTable] and `calls to revert` are transferred.
    }

    fun stop() {}
}