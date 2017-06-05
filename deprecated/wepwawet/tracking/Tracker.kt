package eu.metatools.wepwawet.tracking

import eu.metatools.kontor.serialization.safeCast
import eu.metatools.wepwawet.tools.applyTop
import eu.metatools.wepwawet.tools.requireApplyTop
import org.funktionale.option.Option
import org.funktionale.option.Option.None
import org.funktionale.option.Option.Some
import java.util.*

/**
 * Tracks calls of type [Call] and their dependencies on [Dep], where dependency usage should be indicated using [touch].
 */
class Tracker {
    /**
     * Internal representation that supports mutation operations on the members.
     */
    private data class MutableTrace<R>(
            val call: Call,
            val dependencies: MutableSet<Dep>,
            val nested: MutableSet<MutableTrace<*>>) {
        var result: Option<R> = None

        /**
         * Converts the mutable trace to an immutable trace.
         */
        fun toTrace(): Trace<R> =
                Trace(call, result.get(), dependencies.toSet(), nested.map(MutableTrace<*>::toTrace).toSet())
    }

    /**
     * Internal stack of mutable traces.
     */
    private val stack = Stack<MutableTrace<*>>()

    /**
     * Enters a [call].
     */
    fun <R> enter(call: Call) {
        val m = MutableTrace<R>(call, hashSetOf(), hashSetOf())
        stack.applyTop { nested += m }
        stack.push(m)
    }

    /**
     * Touches [dependency] in the current topmost call.
     */
    fun touch(dependency: Dep) {
        stack.requireApplyTop {
            dependencies += dependency
        }
    }

    /**
     * Leaves the current topmost call, returns the resulting trace.
     */
    fun <R> leave(result: R): Trace<R> {
        val k = safeCast<MutableTrace<R>>(stack.pop())
        k.result = Some(result)
        return k.toTrace()
    }

    /**
     * Tracks dependencies in [block] for [call]. Returns the resulting trace.
     */
    inline fun <R> trackIn(call: Call, block: () -> R): Trace<R> {
        enter<R>(call)
        val r = block()
        return leave(r)
    }

    /**
     * Tracks dependencies in [block] for [call]. Returns the block's result.
     */
    inline fun <R> trackLet(call: Call, block: () -> R): R {
        enter<R>(call)
        val r = block()
        leave(r).stats()
        return r
    }
}