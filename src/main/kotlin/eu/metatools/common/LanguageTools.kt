package eu.metatools.common

/**
 * Calls the block if the receiver is of type [T], otherwise does not apply the block.
 */
inline fun <reified T> Any?.applyIfIs(block: T.() -> Unit) {
    if (this is T) block(this)
}

/**
 * Composes two consumers sequentially.
 */
inline infix fun (() -> Unit).then(crossinline other: () -> Unit) = { ->
    this()
    other()
}

/**
 * Composes two consumers sequentially.
 */
inline infix fun <T> ((T) -> Unit).then(crossinline other: (T) -> Unit) = { t: T ->
    this(t)
    other(t)
}

/**
 * Composes two consumers sequentially.
 */
inline infix fun <T, U> ((T, U) -> Unit).then(crossinline other: (T, U) -> Unit) = { t: T, u: U ->
    this(t, u)
    other(t, u)
}