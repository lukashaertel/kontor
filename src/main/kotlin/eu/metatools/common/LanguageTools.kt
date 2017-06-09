package eu.metatools.common

/**
 * Calls the block if the receiver is of type [T], otherwise does not apply the block.
 */
inline fun <reified T> Any?.applyIfIs(block: T.() -> Unit) {
    if (this is T) block(this)
}