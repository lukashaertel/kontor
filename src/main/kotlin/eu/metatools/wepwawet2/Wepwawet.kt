package eu.metatools.wepwawet2

import eu.metatools.wepwawet2.components.HashRevTable
import eu.metatools.wepwawet2.tools.cast

/**
 * Abstract engine class, uses [mapper] to transfer and identify networked entities, [tracker] to keep track of
 * changes from impulses and updates, and [rev] to state the current revision.
 */
class Wepwawet(val mapper: Mapper, val net: Net) {
    /**
     * Tracker for change and impact tracking.
     */
    internal val tracker = Tracker()

    /**
     * Table of all known entities.
     */
    internal val entityTable = EntityTable()

    /**
     * The current revision value.
     */
    var rev: Rev = 0L

    /**
     * Creates the entity in tracked mode via the given constructor.
     */
    fun <R : Entity> create(ctor: (Node) -> R): R {
        // Get network safe identity
        val id = net.getAndLeaseId()

        // Track construction
        tracker.ctor(id, mapper.mapCtor(cast(ctor)), listOf())

        // Construct for given entries
        return ctor(Node(this, id, HashRevTable())).also {
            entityTable[id] = it
        }
    }

    /**
     * Creates the entity in tracked mode via the given constructor.
     */
    fun <R : Entity, T1> create(ctor: (Node, T1) -> R, t1: T1): R {
        // Get network safe identity
        val id = net.getAndLeaseId()

        // Track construction
        tracker.ctor(id, mapper.mapCtor(cast(ctor)), listOf(t1))

        // Construct for given entries
        return ctor(Node(this, id, HashRevTable()), t1).also {
            entityTable[id] = it
        }
    }

    /**
     * Creates the entity in tracked mode via the given constructor.
     */
    fun <R : Entity, T1, T2> create(ctor: (Node, T1, T2) -> R, t1: T1, t2: T2): R {
        // Get network safe identity
        val id = net.getAndLeaseId()

        // Track construction
        tracker.ctor(id, mapper.mapCtor(cast(ctor)), listOf(t1, t2))

        // Construct for given entries
        return ctor(Node(this, id, HashRevTable()), t1, t2).also {
            entityTable[id] = it
        }
    }

    /**
     * Creates the entity in tracked mode via the given constructor.
     */
    fun <R : Entity, T1, T2, T3> create(ctor: (Node, T1, T2, T3) -> R, t1: T1, t2: T2, t3: T3): R {
        // Get network safe identity
        val id = net.getAndLeaseId()

        // Track construction
        tracker.ctor(id, mapper.mapCtor(cast(ctor)), listOf(t1, t2, t3))

        // Construct for given entries
        return ctor(Node(this, id, HashRevTable()), t1, t2, t3).also {
            entityTable[id] = it
        }
    }

    /**
     * Registers an abstract impulse with an abstract precondition.
     */
    fun registerImpulse(propId: PropId, block: Entity.(List<Any?>) -> Unit, pre: (Entity.() -> Boolean)?) {
        println("I know $propId as a call $block with precondition $pre")
    }

    /**
     * Begins running an impulse. TODO: Maybe pass the function object instead?
     */
    fun beginImpulse(id: Id, propId: PropId, args: List<Any?>) {
        println("BEGIN: $id.$propId($args)")
        tracker.reset()
    }

    /**
     * Ends running an impulse.
     */
    fun endImpulse() {
        println("END: $tracker")
    }
}