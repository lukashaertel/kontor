package eu.metatools.kontor.tools

import java.util.*

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