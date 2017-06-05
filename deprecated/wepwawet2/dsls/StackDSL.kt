package eu.metatools.wepwawet2.dsls

import java.util.*


/**
 * The pending result of an [nonEmpty] execution.
 */
interface StackPending<T>

/**
 * Variant of [StackPending] where the non-empty branch was executed.
 */
class StackNonEmpty<T>(val value: T) : StackPending<T>

/**
 * Variant of [StackPending] where empty branch should be executed
 */
class StackEmpty<T> : StackPending<T>


/**
 * Opens an [IfDebugPending] block, executing the [block] given if [debug] is true or awaiting a call to [otherwise].
 */
inline infix fun <T, R> Stack<T>.nonEmpty(block: (T) -> R) =
        if (isEmpty())
            StackEmpty<R>()
        else
            StackNonEmpty(block(pop()))

/**
 * If not debug, the [block] will be executed for result, otherwise, the [IfDebugTrue.value] will be used.
 */
inline infix fun <R> StackPending<R>.otherwise(block: () -> R) =
        if (this is StackNonEmpty)
            value
        else
            block()
