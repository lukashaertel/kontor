package eu.metatools.wepwawet2.util

import eu.metatools.wepwawet2.data.Rev
import java.util.*
import kotlin.collections.MutableMap.*

class RevTable<K, V> {
    /**
     * Internal backing of the assignments.
     */
    private val backing = HashMap<K, TreeMap<Rev, V>>()

    /**
     * Vies this rev table as a map.
     */
    fun asMap(): HashMap<K, TreeMap<Rev, V>> {
        return backing
    }

    /**
     * Gets the cell value.
     */
    operator fun get(rev: Rev, key: K): V? {
        return asMap()[key]?.get(rev)
    }

    /**
     * Sets the cell value.
     */
    operator fun set(rev: Rev, key: K, value: V): V? {
        return asMap().getOrPut(key, ::TreeMap).put(rev, value)
    }

    /**
     * Removes the cell value.
     */
    fun remove(rev: Rev, key: K): V? {
        // Get host map or return null if not present
        val x = asMap()[key] ?: return null

        // Remove item, if host map is then empty, revert host map
        val r = x.remove(rev)
        if (x.isEmpty())
            asMap().remove(key)
        return r

    }

    /**
     * Gets the value of the first earlier cell.
     */
    fun lower(rev: Rev, key: K): MutableEntry<Rev, V>? {
        return asMap()[key]?.lowerEntry(rev)
    }

    /**
     * Gets the value of the first earlier or same cell.
     */
    fun floor(rev: Rev, key: K): MutableEntry<Rev, V>? {
        return asMap()[key]?.floorEntry(rev)
    }

    /**
     * Gets the value of the first later or same cell.
     */
    fun ceiling(rev: Rev, key: K): MutableEntry<Rev, V>? {
        return asMap()[key]?.ceilingEntry(rev)
    }

    /**
     * Gets the value of the first later cell.
     */
    fun higher(rev: Rev, key: K): MutableEntry<Rev, V>? {
        return asMap()[key]?.higherEntry(rev)
    }
}