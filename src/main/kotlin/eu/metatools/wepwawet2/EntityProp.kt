package eu.metatools.wepwawet2

import eu.metatools.wepwawet2.tools.cast
import kotlin.reflect.KProperty

/**
 * A property within an entity.
 */
data class EntityProp<in R : Entity, T : Entity>(val propId: PropId, val mutable: Boolean) {
    companion object {
        fun <R : Entity, T : Entity> doGet(r: R, propId: PropId): T {
            return cast(r.node.container.entityTable.getValue(r.node.getValue<Id>(propId)))
        }

        fun <R : Entity, T : Entity> doSet(r: R, propId: PropId, t: T) {
            r.node.setValue(propId, t.node.id)
        }
    }

    operator fun getValue(r: R, p: KProperty<*>): T {
        // Get from raw entity
        return doGet(r, propId)
    }

    operator fun setValue(r: R, p: KProperty<*>, t: T) {
        // Strict error on immutable mutation
        if (!mutable) error("Trying to mutate an immutable field")

        // Set on raw entity
        doSet(r, propId, t)
    }
}