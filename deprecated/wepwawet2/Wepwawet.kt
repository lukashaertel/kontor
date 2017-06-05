package eu.metatools.wepwawet2

import com.google.common.collect.ImmutableSortedMap
import eu.metatools.wepwawet2.components.*
import eu.metatools.wepwawet2.data.*
import eu.metatools.wepwawet2.util.WepwawetEntityCallException
import eu.metatools.wepwawet2.util.WepwawetEntityException
import eu.metatools.wepwawet2.util.cast
import eu.metatools.wepwawet2.util.offerSafe
import java.util.*

/**
 * Abstract engine class, uses [mapper] to transfer and identify networked entities, [tracker] to keep track of
 * changes from impulses and updates, and [major] to state the current revision.
 */
class Wepwawet(val mapper: Mapper, val net: Net) {
    private data class NetSignOff(val rev: Rev)

    private data class NetCall(val rev: Rev, val call: Call)


    /**
     * Table of all registered impulses.
     */
    internal val impulseRegistry = hashMapOf<PropId, Entity.(List<Any?>) -> Unit>()

    /**
     * Table of all known entities.
     */
    internal val entityTable = EntityTable()


    /**
     * Tracking
     */
    internal val trackingTable = CallTable()

    internal val receiving = TreeMap<Rev, Call>()

    // Dependency tracking

    internal val reads = arrayListOf<Access>()

    internal val writes = arrayListOf<Access>()

    internal val creates = arrayListOf<Id>()

    internal val demands = arrayListOf<Id>()

    internal val evicts = arrayListOf<Id>()

    private fun resetTracking() {
        reads.clear()
        writes.clear()
        creates.clear()
        demands.clear()
        evicts.clear()
    }

    // Time system

    internal var perUpdate: PerUpdate = 0

    internal var perImpulse: PerImpulse = 0

    internal var perCreate: PerCreate = 0

    internal var resolution: Resolution = 0

    var now: Rev
        get() = Rev(perUpdate, perImpulse, perCreate, resolution)
        set(value) {
            perUpdate = value.perUpdate
            perImpulse = value.perImpulse
            perCreate = value.perCreate
            resolution = value.resolution
        }

    /**
     * Runs a bound constructor for it's result.
     */
    private

    fun <R : Entity> handleCreate(provider: (Node) -> R, createId: CreateId, args: List<Any?>): R {
        // Get current time and entity id
        val time = now
        val id = now

        // Construct for given arguments and track
        val entity = provider(Node(this, id, createId, args))
        entityTable.constructAt(time, id, entity)
        creates += id

        perCreate++
        return entity
    }

    /**
     * Evicts the entity at the current time.
     */
    fun evict(entity: Entity) {
        // Get current time and entity id
        val time = now
        val id = entity.node.id

        // Destruct and track
        entityTable.destructAt(time, entity.node.id, entity)
        demands += id
        evicts += id
    }

    /**
     * Creates the entity in tracked mode via the given constructor.
     */
    fun <R : Entity> create(ctor: (Node) -> R) =
            handleCreate(ctor, mapper.mapCreate(cast(ctor)), listOf())

    /**
     * Creates the entity in tracked mode via the given constructor.
     */
    fun <R : Entity, T1> create(ctor: (Node, T1) -> R, t1: T1) =
            handleCreate({ ctor(it, t1) }, mapper.mapCreate(cast(ctor)), listOf(t1))

    /**
     * Creates the entity in tracked mode via the given constructor.
     */
    fun <R : Entity, T1, T2> create(ctor: (Node, T1, T2) -> R, t1: T1, t2: T2) =
            handleCreate({ ctor(it, t1, t2) }, mapper.mapCreate(cast(ctor)), listOf(t1, t2))

    /**
     * Creates the entity in tracked mode via the given constructor.
     */
    fun <R : Entity, T1, T2, T3> create(ctor: (Node, T1, T2, T3) -> R, t1: T1, t2: T2, t3: T3) =
            handleCreate({ ctor(it, t1, t2, t3) }, mapper.mapCreate(cast(ctor)), listOf(t1, t2, t3))

    /**
     * Invalidates all calls in [invalidated] and plays back all calls in [playlist].
     */
    private fun play(invalidated: NavigableMap<Rev, Call>, playlist: NavigableMap<Rev, Call>) {
        // Store time
        val previous = now

        // Unplay all invalidated calls
        for ((time, call) in invalidated) {
            for ((id, propId) in call.writes)
                entityTable.getAt(time, id)!!.node.revert(time, propId)

            for (id in call.creates)
                entityTable.revert(time, id)

            for (id in call.evicts)
                entityTable.revert(time, id)
        }

        // Play all new calls
        for ((time, call) in playlist) {
            // Get target entity
            val target = entityTable.getAt(time, call.id)

            // If it exists at that time
            if (target != null)
                try {
                    // Get impulse to execute
                    val block = impulseRegistry.getValue(call.propId)

                    // Set target revision
                    now = time

                    // Reset tracking and run
                    resetTracking()
                    target.block(call.args)

                    // Reset the dependencies of the call
                    trackingTable.substitute(time, call.copy(
                            reads = reads.toList(),
                            writes = writes.toList(),
                            creates = creates.toList(),
                            demands = demands.toList(),
                            evicts = evicts.toList()
                    ))
                } catch(t: Throwable) {
                    throw WepwawetEntityCallException(time, target, call, "Error in replayed block", t)
                }
        }

        // Reset
        now = previous
    }

    /**
     * Weaves in locally for an already executed call at the current revision.
     */
    private fun localWeave(time: Rev, call: Call) {
        // Insert only this call
        val invalidated = trackingTable.insert(ImmutableSortedMap.of(time, call))

        // Playlist is equal to invalidation list, since call is already executed
        play(invalidated, invalidated)
    }

    /**
     * Sends the call to the network for the current revision.
     */
    private fun localSend(time: Rev, callNet: Call) {
        net.outbound.offerSafe(NetCall(time, callNet))
    }

    /**
     * Runs a concrete impulse passing along the abstract arguments.
     */
    internal fun runImpulse(entity: Entity, propId: PropId, args: List<Any?>, block: () -> Unit) {
        // Get current time
        val time = now


        // Reset tracking and run block
        resetTracking()

        try {
            block()
        } catch(t: Throwable) {
            throw WepwawetEntityException(time, entity,
                    "Error running local impulse ${mapper.unmapProp(propId).name}", t)
        }

        // Get the relevant sync variables
        val call = Call(entity.node.id,
                propId,
                args,
                reads.toList(),
                writes.toList(),
                creates.toList(),
                demands.toList(),
                evicts.toList())

        // Weave single item and send
        localWeave(time, call)
        localSend(time, call)

        perImpulse++
    }

    /**
     * TODO: This should be handled by net, should also run [update]
     */
    fun <G : Entity> start(res: Resolution, lobby: Lobby, game: (Node, Lobby) -> G): G {
        // Initialize and transfer variables
        now = Rev(0, 0, 0, 0)


        // Construct for given entries
        return game(Node(this, now), lobby).also {
            entityTable.constructAt(now, now, it)

            // Increase the per-constructor variable
            now = now.copy(perCreate = now.perCreate.inc(), resolution = res)
        }
    }

    val sod = 2000
    /**
     * Handles receiving of network data and execution of remote sign off.
     */
    private fun netReceive() {
        // Poll from network without blocking
        for (m in generateSequence { net.inbound.poll() }) when (m) {
            is NetCall -> {
                receiving.put(m.rev, m.call)
            }

            is NetSignOff -> {
//                trackingTable.signOff(m.rev - sod)
//                entityTable.signOff(m.rev - sod)
            }
        }
    }

    // TODO: Decoupling of receiving

    /**
     * Weaves in all received calls that were executed by other clients.
     */
    private fun netWeave() {
        // Insert in tracking table, find all invalidated
        val invalidated = trackingTable.insert(receiving)

        // Add invalidated to receiving as a buffer for the playlist
        receiving.putAll(invalidated)

        // Invalidate calculated calls and playback with all calls
        play(invalidated, receiving)

        receiving.clear()
    }

    /**
     * Handles signing off the values that cannot change from this participant.
     */
    private fun netSignOff() {
        net.outbound.offerSafe(NetSignOff(now))
    }

    /**
     * Updates to a new revision, executing all received call nets to that point.
     */
    fun update(to: PerUpdate) {
        //println("$now: ${entityTable.idsAt(now)}")
        // Poll and handle impulses by other participants
        netReceive()
        netWeave()

        // Sign off where local participant is at now
        netSignOff()

        // Set new revision
        if (to != now.perUpdate)
            now = now.copy(to, 0, 0)

    }
}
