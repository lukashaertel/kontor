package eu.metatools.wepwawet2

import eu.metatools.wepwawet2.tools.each

/**
 * Tracker of changes.
 */
class Tracker {
    /**
     * Tracks construction of classes of [classId] via constructor [ctorId], with arguments [args] and assigned the
     * given identity [id].
     */
    data class Ctor(val id: Id, val ctorId: CtorId, val args: List<Any?>)

    /**
     * Read of property [propId] on entity with identity [id].
     */
    data class Read(val id: Id, val propId: PropId)

    /**
     * Write of property [propId] on entity with identity [id].
     */
    data class Write(val id: Id, val propId: PropId)

    private val backingCtors = hashSetOf<Ctor>()

    private val backingReads = hashSetOf<Read>()

    private val backingWrites = hashSetOf<Write>()

    /**
     * All tracked constructors since the last [reset].
     */
    val ctors get() = backingCtors.toSet()

    /**
     * All tracked reads since the last [reset].
     */
    val reads get() = backingReads.toSet()

    /**
     * All tracked writes since the last [reset].
     */
    val writes get() = backingWrites.toSet()

    /**
     * Resets the tracker
     */
    fun reset() {
        each(backingCtors, backingReads, backingWrites) { clear() }
    }

    /**
     * Track a constructor call.
     */
    fun ctor(id: Id, ctorId: CtorId, args: List<Any?>) {
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

    override fun toString() = "Tracker(ctors=$backingCtors, reads=$backingReads, writes=$backingWrites)"
}