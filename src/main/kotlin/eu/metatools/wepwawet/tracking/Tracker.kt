package eu.metatools.wepwawet.tracking

import eu.metatools.kontor.serialization.safeCast
import eu.metatools.wepwawet.applyTop
import eu.metatools.wepwawet.requireApplyTop
import org.funktionale.option.Option
import org.funktionale.option.Option.None
import org.funktionale.option.Option.Some
import java.util.*

/**
 * Tracks calls of type [C] and their dependencies on [D], where dependency usage should be indicated using [touch].
 */
class Tracker<C, D> {
    /**
     * Internal representation that supports mutation operations on the members.
     */
    private data class MutableTrace<C, D, R>(
            val call: C,
            val dependencies: MutableSet<D>,
            val nested: MutableSet<MutableTrace<C, D, *>>) {
        var result: Option<R> = None

        /**
         * Converts the mutable trace to an immutable trace.
         */
        fun toTrace(): Trace<C, D, R> =
                Trace(call, result.get(), dependencies.toSet(), nested.map(MutableTrace<C, D, *>::toTrace).toSet())
    }

    /**
     * Internal stack of mutable traces.
     */
    private val stack = Stack<MutableTrace<C, D, *>>()

    /**
     * Enters a [call].
     */
    fun <R> enter(call: C) {
        val m = MutableTrace<C, D, R>(call, hashSetOf(), hashSetOf())
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
    fun <R> leave(result: R): Trace<C, D, R> {
        val k = safeCast<MutableTrace<C, D, R>>(stack.pop())
        k.result = Some(result)
        return k.toTrace()
    }

    /**
     * Tracks dependencies in [block] for [call]. Returns the resulting trace.
     */
    inline fun <R> trackIn(call: C, block: () -> R): Trace<C, D, R> {
        enter<R>(call)
        val r = block()
        return leave(r)
    }

    /**
     * Tracks dependencies in [block] for [call]. Returns the block's result.
     */
    inline fun <R> trackLet(call: C, block: () -> R): R {
        enter<R>(call)
        val r = block()
        leave(r).stats()
        return r
    }
}