package eu.metatools.wepwawet2.components

import eu.metatools.wepwawet2.CallNet
import eu.metatools.wepwawet2.Rev
import eu.metatools.wepwawet2.RevDeque
import java.util.*

/**
 * Basic reference implementation of [RevDeque], not meant to provide high performance..
 */
class TreeRevDeque : RevDeque {
    /**
     * Backing for the values, associated by their revision.
     */
    private val backing: NavigableMap<Rev, Deque<CallNet>>

    /**
     * Creates an empty [TreeRevDeque].
     */
    constructor() {
        backing = TreeMap<Rev, Deque<CallNet>>()
    }

    /**
     * Creates a [TreeRevDeque] with the elements offered to.
     */
    constructor(elements: Iterable<CallNet>) {
        backing = TreeMap<Rev, Deque<CallNet>>(elements.groupBy(CallNet::rev).mapValues { (_, v) -> LinkedList(v) })
    }

    override fun isEmpty() = backing.isEmpty()

    override fun poll(): CallNet {
        val (k, cs) = backing.firstEntry()
        val c = cs.poll()
        if (cs.isEmpty())
            backing.remove(k)
        return c
    }

    override fun offer(callNet: CallNet) {
        backing.getOrPut(callNet.rev, { LinkedList() }).offer(callNet)
    }

}