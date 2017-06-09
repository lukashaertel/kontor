package eu.metatools.common

import java.util.*

/**
 * Returns true with the given [chance].
 */
fun Random.randomTrue(chance: Double) =
        nextDouble() <= chance

/**
 * Returns true with the given [chance].
 */
fun randomTrue(chance: Double) = Random().randomTrue(chance)

/**
 * Picks one of the values to return randomly.
 */
fun <T> Random.randomOf(vararg ts: T) =
        ts[nextInt(ts.size)]

/**
 * Picks one of the values to return randomly.
 */
fun <T> randomOf(vararg ts: T) = Random().randomOf(*ts)

/**
 * Picks one of the function to invoke randomly.
 */
fun <T> Random.randomOf(vararg ts: () -> T) =
        ts[nextInt(ts.size)]()

/**
 * Picks one of the function to invoke randomly.
 */
fun <T> randomOf(vararg ts: () -> T) = Random().randomOf(*ts)