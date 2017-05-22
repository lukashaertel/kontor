package eu.metatools.wepwawet2

import java.util.*

/**
 * Created by pazuzu on 5/22/17.
 */
class TrackingTable : NavigableMap<Rev, MutableList<CallNet>> by TreeMap<Rev, MutableList<CallNet>>() {
    fun signOff(rev: Rev) {
        headMap(rev).clear()
    }
}
