package eu.metatools.wepwawet.tracking

import eu.metatools.wepwawet.applyTop
import eu.metatools.wepwawet.requireApplyTop
import java.util.*

/**
 * Tracks calls of type [C] and their dependencies on [D], where dependency usage should be indicated using [touch].
 */
class Tracker<C, D> {
    /**
     * Internal representation that supports mutation operations on the members.
     */
    private data class MutableTrace<C, D>(
            val call: C,
            val dependencies: MutableSet<D>,
            val nested: MutableSet<MutableTrace<C, D>>) {
        /**
         * Converts the mutable trace to an immutable trace.
         */
        fun toTrace(): Trace<C, D> =
                Trace(call, dependencies.toSet(), nested.map(MutableTrace<C, D>::toTrace).toSet())
    }

    /**
     * Internal stack of mutable traces.
     */
    private val stack = Stack<MutableTrace<C, D>>()

    /**
     * Enters a [call].
     */
    fun enter(call: C) {
        val m = MutableTrace<C, D>(call, hashSetOf(), hashSetOf())
        stack.applyTop { nested += m }
        stack.push(m)
    }

    /**
     * Touches [dependency] in the current topmost call.
     */
    fun touch(dependency: D) {
        stack.requireApplyTop {
            dependencies += dependency
        }
    }

    /**
     * Leaves the current topmost call, returns the resulting trace.
     */
    fun leave(): Trace<C, D> {
        return stack.pop().toTrace()
    }

    /**
     * Tracks dependencies in [block] for [call]. Returns the resulting trace.
     */
    inline fun trackIn(call: C, block: () -> Unit): Trace<C, D> {
        try {
            enter(call)
            block()
        } finally {
            return leave()
        }
    }

    /**
     * Tracks dependencies in [block] for [call]. Returns the block's result.
     */
    inline fun <T> trackLet(call: C, block: () -> T): T {
        try {
            enter(call)
            return block()
        } finally {
            leave()
        }
    }
}