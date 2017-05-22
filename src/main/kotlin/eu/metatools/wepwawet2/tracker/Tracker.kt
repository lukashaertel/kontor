package eu.metatools.wepwawet2.tracker

import eu.metatools.wepwawet2.Args
import eu.metatools.wepwawet2.CtorId
import eu.metatools.wepwawet2.Id
import eu.metatools.wepwawet2.PropId
import eu.metatools.wepwawet2.tools.each

/**
 * Tracker of changes.
 */
class Tracker {
    private val backingCtors = arrayListOf<Ctor>()

    private val backingReads = arrayListOf<Read>()

    private val backingWrites = arrayListOf<Write>()

    private val backingDtors = arrayListOf<Dtor>()

    /**
     * All tracked constructors since the last [reset].
     */
    val ctors get() = backingCtors.toList()

    /**
     * All tracked reads since the last [reset].
     */
    val reads get() = backingReads.toList()

    /**
     * All tracked writes since the last [reset].
     */
    val writes get() = backingWrites.toList()

    /**
     * All tracked destructions since the last [reset].
     */
    val dtors get() = backingDtors.toList()

    /**
     * Resets the tracker
     */
    fun reset() {
        each(backingCtors, backingReads, backingWrites) { clear() }
    }

    /**
     * Track a constructor call.
     */
    fun ctor(id: Id, ctorId: CtorId, args: Args) {
        backingCtors += Ctor(id, ctorId, args)
    }

    /**
     * Track a property read.
     */
    fun read(id: Id, propId: PropId) {
        backingReads += Read(id, propId)
    }

    /**
     * Track a property write.
     */
    fun write(id: Id, propId: PropId) {
        backingWrites += Write(id, propId)
    }

    fun dtor(id: Id) {
        backingDtors += Dtor(id)
    }

    override fun toString() = "Tracker(ctors=$ctors, reads=$reads, writes=$writes), dtors=$dtors)"
}