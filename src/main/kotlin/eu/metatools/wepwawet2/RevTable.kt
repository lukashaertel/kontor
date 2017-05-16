package eu.metatools.wepwawet2

/**
 * A revision table.
 */
interface RevTable {
    /**
     * Gets the value at the revision cell [propId] x [rev].
     */
    operator fun get(propId: PropId, rev: Rev): Any?

    /**
     * Sets the value at the revision cell [propId] x [rev].
     */
    operator fun set(propId: PropId, rev: Rev, value: Any?)

    /**
     * Removes the value at the revision cell [propId] x [rev].
     */
    fun remove(propId: PropId, rev: Rev)

    /**
     * Checks if there is a value at revision cell [propId] x [rev].
     */
    fun contains(propId: PropId, rev: Rev): Boolean
}