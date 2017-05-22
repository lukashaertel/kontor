package eu.metatools.wepwawet2

import eu.metatools.wepwawet2.components.HashRevTable
import eu.metatools.wepwawet2.components.TreeRevDeque
import eu.metatools.wepwawet2.dsls.debugOut
import eu.metatools.wepwawet2.dsls.nonEmpty
import eu.metatools.wepwawet2.dsls.otherwise
import eu.metatools.wepwawet2.tools.*
import eu.metatools.wepwawet2.tracker.Ctor
import eu.metatools.wepwawet2.tracker.Tracker
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import java.util.*

/**
 * Abstract engine class, uses [mapper] to transfer and identify networked entities, [tracker] to keep track of
 * changes from impulses and updates, and [rev] to state the current revision.
 */
class Wepwawet(val mapper: Mapper, val net: Net) {

    private val ctorIdStack = Stack<Id>()

    private data class SignOff(val rev: Rev)

    /**
     * Tracker for change and impact tracking.
     */
    internal val tracker = Tracker()

    /**
     * Table of all known entities.
     */
    internal val entityTable = EntityTable()

    /**
     * Table of all registered impulses.
     */
    private val impulseTable = ImpulseTable()

    /**
     * Table of all registered reacts.
     */
    private val reactTable = ReactTable()

    /**
     * Tracking
     */
    private val trackingTable = TrackingTable()

    /**
     * Table of repossessment.
     */
    private val repoTable = RepoTable()

    /**
     * Receive buffer
     */
    private val receiving = arrayListOf<CallNet>()


    /**
     * The current revision value.
     */
    internal var rev: Rev = 0L

    /**
     * Offers a call net to the tracking page.
     */
    private fun offerTracking(callNet: CallNet) {
        trackingTable.getOrPut(callNet.rev, { arrayListOf() }).add(callNet)
    }

    /**
     * Runs a bound constructor.
     */
    private fun <R : Entity> create(args: Args, ctor: (Node) -> R): R {
        // Get network safe identity
        val id = ctorIdStack nonEmpty { it } otherwise { net.getAndLeaseId() }

        // Track construction
        tracker.ctor(id, mapper.mapCtor(cast(ctor)), argsOf())

        // Construct for given entries
        return ctor(Node(this, id, HashRevTable())).also {
            entityTable[id] = it
        }
    }

    /**
     * Creates the entity in tracked mode via the given constructor.
     */
    fun <R : Entity> create(ctor: (Node) -> R) =
            create(argsOf(), ctor)

    /**
     * Creates the entity in tracked mode via the given constructor.
     */
    fun <R : Entity, T1> create(ctor: (Node, T1) -> R, t1: T1) =
            create(argsOf(t1)) { ctor(it, t1) }

    /**
     * Creates the entity in tracked mode via the given constructor.
     */
    fun <R : Entity, T1, T2> create(ctor: (Node, T1, T2) -> R, t1: T1, t2: T2) =
            create(argsOf(t1, t2)) { ctor(it, t1, t2) }

    /**
     * Creates the entity in tracked mode via the given constructor.
     */
    fun <R : Entity, T1, T2, T3> create(ctor: (Node, T1, T2, T3) -> R, t1: T1, t2: T2, t3: T3) =
            create(argsOf(t1, t2, t3)) { ctor(it, t1, t2, t3) }

    fun evict(entity: Entity) {
        tracker.dtor(entity.node.id)
        repoTable.getOrPut(rev, ::ArrayList) += entity.node.id
    }

    /**
     * Registers an abstract impulse.
     */
    internal fun registerImpulse(propId: PropId, block: Entity.(List<Any?>) -> Unit) {
        impulseTable[propId] = block
    }

    /**
     * Registers an abstract react.
     */
    internal fun registerReact(depends: List<PropId>, block: Entity.() -> Unit) {
        reactTable.register(depends, block)
    }


    private val lastImpulses = hashMapOf<PropId, Rev>()
    /**
     * Runs a concrete impulse passing along the abstract arguments.
     */
    internal fun runImpulse(entity: Entity, propId: PropId, args: List<Any?>, block: () -> Unit) {
        //TODO Better behaviour for this
        if (lastImpulses.getOrElse(propId, { Long.MIN_VALUE }) == rev) {
            debugOut { "Skipping impulse repeat." }
            return
        }
        lastImpulses[propId] = rev


        // Reset tracking and run block
        tracker.reset()
        block()

        // Add call net to sending buffer and insert into tracking
        CallNet(rev, entity.node.id, propId, args, tracker.reads, tracker.writes, tracker.ctors, tracker.dtors).also {
            offerTracking(it)
            net.outbound.offerSafe(it)
        }
    }

    /**
     * Run reacts generated from current tracker.
     */
    private fun runReacts() {
        error("Unsupported")
        val etw = tracker.writes.groupBy({ entityTable.getValue(it.id) }) { it.propId }
        for ((e, t) in etw)
            for (b in reactTable.find(t))
                e.b()

        // TODO: Transitive reacts
    }


    var lastSignOff: Rev? = null

    var lastExecutedSignOff: Rev? = null

    /**
     * TODO: This should be handled by net, should also run [update]
     */
    fun <G : Entity> start(lobby: Lobby, game: (Node, Lobby) -> G): G {
        val id = net.getRootId()
        // Construct for given entries
        return game(Node(this, id, HashRevTable()), lobby).also {
            entityTable[id] = it
        }
    }

    /**
     * Updates to a new revision, executing all received call nets to that point.
     */
    fun update(newRev: Rev) {
        println("Update")
        // TODO: There's still a bug where a call is inserted and revoked mutually
        // I bet this whole thing needs a rework

        // Poll from network without blocking
        for (m in generateSequence { net.inbound.poll() })
            if (m is CallNet)
            // Offer call net to the receive buffer
                receiving += m
            else if (m is SignOff)
            // Handle sign off
                lastSignOff = m.rev - 250 // TODO Should be done by server


        // If a sign off is present, execute it
        val lso = lastSignOff
        if (lso != null && lso != lastExecutedSignOff) {
            trackingTable.signOff(lso)
            entityTable.signOff(lso)
            repoTable.signOffIn(entityTable, lso)
            lastExecutedSignOff = lso
        }

        // Get the initial deque from the receive buffer by removing up to the new revision.
        val insert = TreeRevDeque(receiving.filterRemoving { it.rev <= newRev })

        // While there are inserts to do
        while (insert.isNotEmpty()) {
            // Take one and execute in that time step
            val i = insert.poll()
            rev = i.rev

            // Skip call nets that are on revoked entities
            if (i.id in entityTable) {
                for ((id) in i.ctors)
                    ctorIdStack += id

                // Get target entity and target impulse
                val target = entityTable.getValue(i.id)
                val block = impulseTable.getValue(i.propId)

                // Reset tracking and run block
                tracker.reset()
                target.block(i.args)
            }

            // Retract invalidated calls
            trackingTable.tailMap(i.rev).entries.forRemoving { (k, v) ->
                // Get the remaining calls to check, usually all if not in same revision
                val it = if (k != i.rev) v else v.subList(v.indexOf(i) + 1, v.size)

                // Remove all calls with invalidated dependencies, add them to the inserts
                for (j in it.filterRemoving { it.inSet intersects i.outSet }) {
                    // Revoke all their writes, if entity is still present
                    for ((id, propId) in j.writes)
                        entityTable.getValue(id).node.revTable.removeAt(propId, j.rev)
                    // Revoke all their constructions
                    for ((id) in j.ctors) {
                        entityTable.remove(id)
                        println("revoking $id")
                    }

                    // Revoke all their destructions on that time slot
                    for ((id) in j.dtors) {
                        repoTable[j.rev]?.remove(id)
                    }

                    // Offer for reinsert
                    insert.offer(j)
                }

                // Remove lists that are now empty
                v.isEmpty()
            }
            // Insert into tracking
            offerTracking(i)
        }

        // Set new revision
        rev = newRev
        net.outbound.offerSafe(SignOff(rev))

    }

}