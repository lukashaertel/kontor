package eu.metatools.wepwawet

import java.util.*

inline fun <T> Stack<T>.applyTop(block: T.() -> Unit) {
    if (isNotEmpty())
        block(peek())
}

inline fun <T> Stack<T>.requireApplyTop(block: T.() -> Unit) {
    block(peek())
}

inline fun <T, U : Any> Stack<T>.letTop(block: T.() -> U): U? {
    if (isNotEmpty())
        return block(peek())
    else
        return null
}