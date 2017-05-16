package eu.metatools.wepwawet2.components

import eu.metatools.wepwawet2.PropId
import eu.metatools.wepwawet2.Rev
import eu.metatools.wepwawet2.RevTable

/**
 * Basic reference implementation [RevTable], not meant to provide high performance..
 */
class HashRevTable : RevTable {
    private val backing = hashMapOf<Pair<PropId, Rev>, Any?>()

    override fun get(propId: PropId, rev: Rev): Any? {
        return backing[propId to rev]
    }

    override fun set(propId: PropId, rev: Rev, value: Any?) {
        backing[propId to rev] = value
    }

    override fun remove(propId: PropId, rev: Rev) {
        backing.remove(propId to rev)
    }

    override fun contains(propId: PropId, rev: Rev): Boolean {
        return propId to rev in backing
    }
}