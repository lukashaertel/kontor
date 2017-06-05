package eu.metatools.wepwawet.tools

import java.util.*

/**
 * If the stack is not empty, applies on the top value.
 */
inline fun <T> Stack<T>.applyTop(block: T.() -> Unit) {
    if (isNotEmpty())
        block(peek())
}

/**
 * Applies on the top value or throws [EmptyStackException].
 */
inline fun <T> Stack<T>.requireApplyTop(block: T.() -> Unit) {
    block(peek())
}

/**
 * Lets with the top value or returns null.
 */
inline fun <T, U : Any> Stack<T>.letTop(block: T.() -> U): U? {
    if (isNotEmpty())
        return block(peek())
    else
        return null
}