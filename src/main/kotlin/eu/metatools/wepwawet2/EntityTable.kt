package eu.metatools.wepwawet2

/**
 * Table of entity identities to entity.
 */
class EntityTable : MutableMap<Id, Entity> by hashMapOf() {
    fun signOff(rev: Rev) {
        for (e in values)
            e.node.revTable.signOff(rev)
    }

}