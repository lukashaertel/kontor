package eu.metatools.wepwawet.tools

import kotlin.coroutines.experimental.suspendCoroutine

/**
 * Runs [block] for the result. If [block] fails in [E], tries to [fix] the state and retries the [block].
 */
inline suspend fun <T, reified E : Throwable> catchResumable(
        crossinline block: () -> T, crossinline fix: (E) -> Unit) = suspendCoroutine<T> {
    try {
        it.resume(block())
    } catch(t: Throwable) {
        if (t !is E)
            throw t

        fix(t)
        try {
            it.resume(block())
        } catch(t: Throwable) {
            it.resumeWithException(t)
        }
    }
}