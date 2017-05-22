package eu.metatools.wepwawet2

import java.util.*

/**
 * A revision table.
 */
interface RevTable {
    fun keys(): Set<PropId>

    /**
     * Gets the value at the revision cell [propId] x [rev] or the first before.
     */
    fun getLatest(propId: PropId, rev: Rev): Any?

    /**
     * Sets the value at the revision cell [propId] x [rev].
     */
    fun setAt(propId: PropId, rev: Rev, value: Any?)

    /**
     * Removes the value at the revision cell [propId] x [rev].
     */
    fun removeAt(propId: PropId, rev: Rev)

    fun getAll(propId: PropId): SortedMap<Rev, Any?>

    fun signOff(rev: Rev)
}