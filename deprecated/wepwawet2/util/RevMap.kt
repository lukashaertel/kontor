package eu.metatools.wepwawet2.util

import eu.metatools.wepwawet2.data.Rev
import java.util.*
import kotlin.collections.MutableMap.MutableEntry

class RevMap<V> {
    /**
     * Internal backing of the assignments.
     */
    private val backing = TreeMap<Rev, V>()

    /**
     * Vies this rev map as a map.
     */
    fun asMap(): TreeMap<Rev, V> {
        return backing
    }

    /**
     * Gets the cell value.
     */
    operator fun get(rev: Rev): V? {
        return asMap()[rev]
    }

    /**
     * Sets the cell value.
     */
    operator fun set(rev: Rev, value: V): V? {
        return asMap().put(rev, value)
    }

    /**
     * Removes the cell value.
     */
    fun remove(rev: Rev): V? {
        return asMap().remove(rev)
    }

    /**
     * Gets the value of the first earlier cell.
     */
    fun lower(rev: Rev): MutableEntry<Rev, V>? {
        return asMap().lowerEntry(rev)
    }

    /**
     * Gets the value of the first earlier cell.
     */
    fun floor(rev: Rev): MutableEntry<Rev, V>? {
        return asMap().floorEntry(rev)
    }

    /**
     * Gets the value of the first later cell.
     */
    fun ceiling(rev: Rev): MutableEntry<Rev, V>? {
        return asMap().ceilingEntry(rev)
    }

    /**
     * Gets the value of the first later cell.
     */
    fun higher(rev: Rev): MutableEntry<Rev, V>? {
        return asMap().higherEntry(rev)
    }
}