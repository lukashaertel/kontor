package eu.metatools.wepwawet2.components

import eu.metatools.wepwawet2.SignOff
import eu.metatools.wepwawet2.data.Call
import eu.metatools.wepwawet2.data.Rev
import eu.metatools.wepwawet2.util.filterKeysView
import eu.metatools.wepwawet2.util.notIn
import eu.metatools.wepwawet2.util.poll
import java.util.*

/**
 * Timeline of impulses for invalidation tracking.
 */
class CallTable : SignOff {

    /**
     * Internal backing of values.
     */
    private val timeline = TreeMap<Rev, Call>()

    /**
     * Views this tracking table as a map.
     */
    fun asMap() = timeline


    /**
     * Inserts the calls and returns all existing calls that were invalidated by the insertees.
     */
    fun insert(calls: SortedMap<Rev, Call>): TreeMap<Rev, Call> {
        // Result set is prepared, it also holds call nets that may be skipped as they have already been invalidated
        val insert = TreeMap<Rev, Call>(calls)
        val result = TreeMap<Rev, Call>()

        // Window will hold all subsequent calls by mapping to the tail after the insertion key
        var window: NavigableMap<Rev, Call> = timeline

        // Handle all the invalidations
        while (insert.isNotEmpty()) {
            // Poll invalidation check entry
            val (ik, iv) = insert.poll()

            // Slide window, live view without already seen call nets.
            window = window.tailMap(ik, false).filterKeysView(notIn(result.keys))

            // Filter all values that are invalidated, offer them to the insert buffer and the result vector
            for ((ok, ov) in window)
                if (iv invalidates ov) {
                    result.put(ok, ov)
                    insert.put(ok, ov)
                }
        }

        // Finally add all new calls
        timeline.putAll(calls)

        return result
    }

    fun substitute(rev: Rev, call: Call) {
        timeline[rev] = call
    }

    override fun signOff(rev: Rev) {
        timeline.headMap(rev, false).clear()
    }
}