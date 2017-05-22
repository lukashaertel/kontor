package eu.metatools.wepwawet2.dsls

/**
 * Set this to true if debug level verification is desired.
 */
var debug = true


/**
 * The pending result of an [ifDebug] execution.
 */
interface IfDebugPending<T>

/**
 * Variant of [IfDebugPending] where the debug branch was executed.
 */
class IfDebugTrue<T>(val value: T) : IfDebugPending<T>

/**
 * Variant of [IfDebugPending] where non-debug branch should be executed
 */
class IfDebugFalse<T> : IfDebugPending<T>

/**
 * Opens an [IfDebugPending] block, executing the [block] given if [debug] is true or awaiting a call to [otherwise].
 */
inline fun <R> ifDebug(block: () -> R) =
        if (debug)
            IfDebugTrue(block())
        else
            IfDebugFalse<R>()

/**
 * If not debug, the [block] will be executed for result, otherwise, the [IfDebugTrue.value] will be used.
 */
inline infix fun <R> IfDebugPending<R>.otherwise(block: () -> R) =
        if (this is IfDebugTrue)
            value
        else
            block()

/**
 * Selects [debugTrue] if [debug] is true, [debugFalse] otherwise.
 */
fun <T> selectDebug(debugTrue: T, debugFalse: T) =
        if (debug)
            debugTrue
        else
            debugFalse