package eu.metatools.wepwawet.utils

/**
 * Associates values of an iterable of pairs by identity.
 */
fun <K, V> Iterable<Pair<K, V>>.associate() =
        associate { it }

/**
 *  Associates values of an iterable to many pairs.
 */
inline fun <E, K, V> Iterable<E>.flatAssociate(block: (E) -> Iterable<Pair<K, V>>) =
        flatMap(block).associate()

/**
 * Inverts the mapping.
 */
fun <K, V> Map<K, V>.invert() =
        entries.associate { (k, v) -> v to k }