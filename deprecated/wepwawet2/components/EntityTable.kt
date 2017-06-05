package eu.metatools.wepwawet2.components

import eu.metatools.wepwawet2.Entity
import eu.metatools.wepwawet2.SignOff
import eu.metatools.wepwawet2.data.Id
import eu.metatools.wepwawet2.data.Rev
import eu.metatools.wepwawet2.util.*

/**
 * Table of entity identities to entity.
 */
class EntityTable : SignOff {
    val backing = RevTable<Id, Mutation<Entity>>()

    fun idsAt(rev: Rev) =
            backing.asMap().keys.filter { contains(rev, it) }

    fun contains(rev: Rev, id: Id): Boolean {
        // Get value before
        val m = backing.floor(rev, id)?.value

        // Check validity as result
        return m != null && m is Add<*>
    }

    fun getAt(rev: Rev, id: Id): Entity? {
        // Get value before
        val m = backing.floor(rev, id)?.value

        // Check validity and return boxed item
        if (m != null && m is Add<*>)
            return m.element

        // Otherwise null
        return null
    }

    fun constructAt(rev: Rev, id: Id, entity: Entity) {
        if (contains(rev, id))
            throw  IllegalArgumentException("Overwriting existing entity $id at $rev.")
        backing[rev, id] = Add(entity)
    }

    fun destructAt(rev: Rev, id: Id, entity: Entity) {
        if (!contains(rev, id))
            throw  IllegalArgumentException("Removing non-existing entity $id at $rev.")
        backing[rev, id] = Remove(entity)
    }

    fun revert(rev: Rev, id: Id) {
        val next = backing.higher(rev, id)
        val current = backing.remove(rev, id)
        if (current is Add && next?.value is Remove)
            backing.remove(next.key, id)
    }

    override fun signOff(rev: Rev) {
        backing.asMap().values.forRemoving {
            // Get cells up to revision, also the greatest entry in there
            val head = it.headMap(rev, false)
            val top = head.lastEntry()

            // Check if there is an entry and it is an ongoing entity
            if (top != null && top.value is Add)
            // If yes, move to revision, sign off it's rev table
                it[rev] = top.value.apply {
                    element.node.signOff(rev)
                }

            // Remove all dropped cells
            head.clear()

            // Remove the entity tree map if no entries left
            it.isEmpty()
        }
    }
}