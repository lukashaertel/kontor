package eu.metatools.wepwawet2

import eu.metatools.wepwawet2.components.HashRevTable
import eu.metatools.wepwawet2.dsls.ifDebug
import eu.metatools.wepwawet2.dsls.nonEmpty
import eu.metatools.wepwawet2.dsls.otherwise
import eu.metatools.wepwawet2.tools.cast
import eu.metatools.wepwawet2.tracker.Ctor
import eu.metatools.wepwawet2.tracker.Read
import eu.metatools.wepwawet2.tracker.Tracker
import eu.metatools.wepwawet2.tracker.Write
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import java.util.*

/**
 * Abstract engine class, uses [mapper] to transfer and identify networked entities, [tracker] to keep track of
 * changes from impulses and updates, and [rev] to state the current revision.
 */
class Wepwawet(val mapper: Mapper, val net: Net) {
    private data class ResolvedCtor(val ctor: Ctor, val result: Entity)

    private val ctorStack = Stack<ResolvedCtor>()

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
     * The current revision value.
     */
    var rev: Rev = 0L

    private fun <R : Entity> create(args: Args, ctor: (Node) -> R): R {
        return ctorStack nonEmpty { (c, r) ->
            // Get Constructor id, this is for checking in debug mode
            val ctorId = mapper.mapCtor(cast(ctor))

            // Check in debug branch for proper sequence
            ifDebug {
                if (ctorId != c.ctorId)
                    error("Mismatching call sequence, expecting ${c.ctorId}, got $ctorId")
                if (args != c.args)
                    error("Mismatching argument sequence, expecting ${c.args}, got $args")
            }

            // Return the result that has been pre-resolved
            cast<R>(r).also {
                entityTable[c.id] = it
            }
        } otherwise {
            // Get network safe identity
            val id = net.getAndLeaseId()

            // Track construction
            tracker.ctor(id, mapper.mapCtor(cast(ctor)), argsOf())

            // Construct for given entries
            ctor(Node(this, id, HashRevTable())).also {
                entityTable[id] = it
            }
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

    /**
     * Registers an abstract impulse with an abstract precondition.
     */
    internal fun registerImpulse(propId: PropId, block: Entity.(List<Any?>) -> Unit) {
        impulseTable[propId] = block
    }

    /**
     * Runs a concrete impulse passing along the abstract arguments.
     */
    internal fun runImpulse(id: Id, propId: PropId, args: List<Any?>, block: () -> Unit) {

        // Actually this would be contained in the conceptual tracking, but since there is just one root call and the
        // context is this very method only, it is omitted.
        // --- tracker.rootCall(id, propId, args)

        // Reset and execute block
        tracker.reset()
        block()

        // Dispatch net to net
        dispatchImpulse(ImpulseNet(rev, id, propId, args, tracker.reads, tracker.writes, tracker.ctors))
    }

    /**
     * An impulse dependency net.
     */
    data class ImpulseNet(
            val rev: Rev,
            val id: Id,
            val propId: PropId,
            val args: List<Any?>,
            val reads: List<Read>,
            val writes: List<Write>,
            val ctors: List<Ctor>)

    /**
     * Dispatches an impulse to network.
     */
    private fun dispatchImpulse(impulseNet: ImpulseNet) {
        // TODO Send to network
        println("Dispatching: $impulseNet")

        runBlocking {
            net.outbound.send(impulseNet)
            println("Dispatched")
        }
    }

    fun testReceive() = runBlocking {
        val msg = net.inbound.receiveOrNull()
        if (msg is ImpulseNet)
            handleImpulse(msg)
    }

    /**
     * Handles incoming impulse.
     */
    private fun handleImpulse(impulseNet: ImpulseNet) = impulseNet.apply {
        // Resolve constructors and put results into stack
        for (ctor in impulseNet.ctors)
            ctorStack += ResolvedCtor(ctor, mapper.unmapCtor(ctor.ctorId).call(*ctor.args) as Entity)

        // Get target entity and target impulse
        val target = entityTable.getValue(id)
        val block = impulseTable.getValue(propId)

        // Execute
        tracker.reset()
        target.block(args)

        // For debug branch check same accesses
        ifDebug {
            if (tracker.reads != impulseNet.reads)
                error("Mismatching reads, expecting ${impulseNet.reads}, got ${tracker.reads}")
            if (tracker.writes != impulseNet.writes)
                error("Mismatching writes, expecting ${impulseNet.writes}, got ${tracker.writes}")
        }

        // TODO Invalidate all following impulses already executed
    }

}