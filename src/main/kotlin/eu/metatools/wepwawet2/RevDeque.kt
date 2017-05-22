package eu.metatools.wepwawet2

/**
 * A revision deque, used to sort calls.
 */
interface RevDeque {
    /**
     * True if there is no element left.
     */
    fun isEmpty(): Boolean

    /**
     * True if there are elements left.
     */
    fun isNotEmpty() = !isEmpty()

    /**
     * Takes an element from the rev deque, this is the first element of the lowest revision.
     */
    fun poll(): CallNet

    /**
     * Offers a call net to the deque at the call net's revision.
     */
    fun offer(callNet: CallNet)
}