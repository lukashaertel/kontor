package eu.metatools.wepwawet2.util

import com.google.common.base.Predicate

import com.google.common.base.Predicates.*
import com.google.common.collect.Maps
import org.funktionale.collections.prependTo
import java.util.*

inline fun <T> T?.applyNotNull(block: T.() -> Unit) {
    if (this != null)
        this.block()
}

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


inline fun <E> MutableIterable<E>.forRemoving(block: (E) -> Boolean) {
    val it = iterator()
    while (it.hasNext())
        if (block(it.next()))
            it.remove()
}

fun <K, V> SortedMap<K, V>.poll(): Pair<K, V> {
    val fk = firstKey()!!
    val fv = remove(fk)!!
    return fk to fv
}

/**
 * While iterating, the host iterable will have the matching items removed.
 */
fun <E : Any> MutableIterable<E>.filterRemoving(block: (E) -> Boolean) = Iterable {
    val host = iterator()
    object : AbstractIterator<E>() {
        override fun computeNext() {
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

fun <E> iterableOf(vararg es: E) =
        Iterable { es.iterator() }

fun <E> Iterable<E>.chunks(first: Int, size: Int) =
        iterableOf(take(first).toList()) + drop(first).chunks(size)

/**
 * Divides the iterable into chunks
 */
fun <E> Iterable<E>.chunks(size: Int): Iterable<List<E>> = Iterable {
    val host = iterator()
    object : AbstractIterator<List<E>>() {
        val status = arrayListOf<E>()
        override fun computeNext() {
            status.clear()
            while (host.hasNext() && status.size < size)
                status += host.next()
            if (status.size > 0)
                setNext(status.toList())
            else
                done()
        }

    }
}

inline fun <E : Comparable<E>> compare(block: () -> Pair<E, E>): Int {
    val (left, right) = block()
    return left.compareTo(right)
}

inline infix fun <E : Comparable<E>> Int.thenCompare(block: () -> Pair<E, E>): Int {
    if (this == 0)
        return compare(block)
    else
        return this
}


fun main(args: Array<String>) {
    println(listOf(1, 2, 3, 4, 56, 4, 2).chunks(1, 2).map { it.toList() })
}


@Suppress("nothing_to_inline")
inline fun <K, V> NavigableMap<K, V>.filterKeysView(keyPredicate: Predicate<K>): NavigableMap<K, V> =
        Maps.filterKeys(this, keyPredicate)

@Suppress("nothing_to_inline")
inline fun <K, V> NavigableMap<K, V>.filterValuesView(valuePredicate: Predicate<V>): NavigableMap<K, V> =
        Maps.filterValues(this, valuePredicate)


@Suppress("nothing_to_inline")
inline fun <K, V> NavigableMap<K, V>.filterKeysView(crossinline keyPredicate: (K) -> Boolean): NavigableMap<K, V> =
        Maps.filterKeys(this) { input -> keyPredicate(input!!) }

@Suppress("nothing_to_inline")
inline fun <K, V> NavigableMap<K, V>.filterValuesView(crossinline valuePredicate: (V) -> Boolean): NavigableMap<K, V> =
        Maps.filterValues(this, { input -> valuePredicate(input!!) })


@Suppress("nothing_to_inline")
inline fun <T> notIn(collection: Collection<T>): Predicate<T> =
        not(`in`(collection))