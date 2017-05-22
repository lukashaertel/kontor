package eu.metatools.wepwawet2.components

import eu.metatools.wepwawet2.PropId
import eu.metatools.wepwawet2.Rev
import eu.metatools.wepwawet2.RevTable
import eu.metatools.wepwawet2.tools.forRemoving
import java.util.*

/**
 * Basic reference implementation of [RevTable], not meant to provide high performance..
 */
class HashRevTable : RevTable {
    private val backing = hashMapOf<PropId, TreeMap<Rev, Any?>>()

    override fun keys() = backing.keys

    override fun getLatest(propId: PropId, rev: Rev): Any? {
        try {
            return backing.getValue(propId).floorEntry(rev).value
        } catch(ex: NullPointerException) {
            println("NPE: $backing, $propId, $rev")
            throw ex
        }
    }

    override fun setAt(propId: PropId, rev: Rev, value: Any?) {
        backing.getOrPut(propId, ::TreeMap).put(rev, value)
    }

    override fun removeAt(propId: PropId, rev: Rev) {
        backing.getValue(propId).remove(rev)
    }

    override fun getAll(propId: PropId) = backing.getValue(propId)

    override fun signOff(rev: Rev) {
        for (v in backing.values) {
            val f = v.floorKey(rev)
            if (f != null)
                v.headMap(f).clear()
        }
    }
}