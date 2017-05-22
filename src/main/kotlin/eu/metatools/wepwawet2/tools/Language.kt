package eu.metatools.wepwawet2.tools

import com.sun.org.apache.xpath.internal.operations.Bool
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.coroutines.experimental.buildIterator

/**
 * For each argument [ts] applies [block].
 */
inline fun <T> each(vararg ts: T, block: T.() -> Unit) {
    for (t in ts) t.block()
}

/**
 * Casts to a generic [T], suppressing checkedness issues.
 */
fun <T> cast(any: Any?): T {
    @Suppress("unchecked_cast")
    val r = any as T
    return r
}

@Suppress("nothing_to_inline")
inline infix fun <T> Iterable<T>.intersects(other: Collection<T>) = any { it in other }

/**
 * Creates a synchronized list.
 */
fun <E> syncList(): MutableList<E> =
        CopyOnWriteArrayList<E>()

inline fun <E> MutableIterable<E>.forRemoving(block: (E) -> Boolean) {
    val it = iterator()
    while (it.hasNext())
        if (block(it.next()))
            it.remove()
}

/**
 * While iterating, the host iterable will have the matching items removed.
 */
fun <E : Any> MutableIterable<E>.filterRemoving(block: (E) -> Boolean) = Iterable {
    val host = iterator()
    object : AbstractIterator<E>() {
        override fun computeNext() {
            var had = false
            while (host.hasNext()) {
                val next = host.next()
                if (block(next)) {
                    host.remove()
                    setNext(next)
                    return
                }
            }

            done()
        }

    }
}