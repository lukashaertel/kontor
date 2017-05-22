package eu.metatools.wepwawet2

import eu.metatools.wepwawet2.tools.cast
import kotlin.reflect.KProperty

/**
 * A property within an entity.
 */
data class EntityListProp<in R : Entity, T : Entity>(val propId: PropId, val mutable: Boolean) {
    companion object {
        fun <R : Entity, T : Entity> doGet(r: R, propId: PropId): List<T> {
            return r.node.getValue<List<Id>>(propId).map {
                cast<T>(r.node.container.entityTable.getValue(it))
            }
        }

        fun <R : Entity, T : Entity> doSet(r: R, propId: PropId, t: List<T>) {
            r.node.setValue(propId, t.map { it.node.id })
        }
    }

    operator fun getValue(r: R, p: KProperty<*>): List<T> {
        // Get from raw entity
        return doGet(r, propId)
    }

    operator fun setValue(r: R, p: KProperty<*>, t: List<T>) {
        // Strict error on immutable mutation
        if (!mutable) error("Trying to mutate an immutable field")

        // Set on raw entity
        doSet(r, propId, t)
    }
}