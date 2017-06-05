package eu.metatools.wepwawet2.delegates

import eu.metatools.wepwawet2.Entity
import eu.metatools.wepwawet2.data.PropId
import kotlin.reflect.KProperty

/**
 * A property within an entity.
 */
data class Prop<in R : Entity, T>(val propId: PropId, val mutable: Boolean) {

    operator fun getValue(r: R, p: KProperty<*>): T {
        // Get from raw entity
        return r.node.getValue(propId)
    }

    operator fun setValue(r: R, p: KProperty<*>, t: T) {
        // Strict error on immutable mutation
        if (!mutable) error("Trying to mutate an immutable field")

        // Set on raw entity
        r.node.setValue(propId, t)
    }
}