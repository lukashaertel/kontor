package eu.metatools.wepwawet2

import eu.metatools.wepwawet2.data.*
import eu.metatools.wepwawet2.util.applyNotNull
import eu.metatools.wepwawet2.util.cast
import eu.metatools.wepwawet2.util.forRemoving
import java.util.*

/**
 * A node, i.e., the raw entity.
 */
data class Node(val container: Wepwawet, val id: Id, val createId: CreateId, val createArgs: List<Any?>) : SignOff {
    /**
     * Internal backing map.
     */
    internal val backing = hashMapOf<PropId, TreeMap<Rev, Any?>>()

    /**
     * Sets the value for [propId] at the current revision.
     */
    fun setValue(propId: PropId, value: Any?) {
        // Track write
        container.tracker.write(id, propId)

        // Set in revision table
        backing.getOrPut(propId, ::TreeMap).put(container.now, value)
    }

    /**
     * Gets and casts the value of [propId] at the current revision.
     */
    fun <T> getValue(propId: PropId): T {
        // Track read
        container.tracker.read(id, propId)

        // Get in revision table
        return cast(backing.getValue(propId).floorEntry(container.now).value)
    }

    /**
     * Removes the value at the revision cell [propId] x [rev].
     */
    fun revert(rev: Rev, propId: PropId) {
        backing[propId].applyNotNull {
            remove(rev)
            if (backing.isEmpty())
                backing.remove(propId)
        }
    }


    override fun signOff(rev: Rev) {
        backing.values.forRemoving {
            val f = it.floorKey(rev)
            if (f != null)
                it.headMap(f, false).clear()

            it.isEmpty()
        }
    }

    override fun toString() = backing.entries.joinToString { (p, r) ->
        "${container.mapper.unmapProp(p).name}=${r.lastEntry()!!.value}"
    }
}