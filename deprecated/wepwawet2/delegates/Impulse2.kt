package eu.metatools.wepwawet2.delegates

import eu.metatools.wepwawet2.Entity
import eu.metatools.wepwawet2.data.PropId
import kotlin.reflect.KProperty

/**
 * An impulse on an entity, two arguments.
 */
data class Impulse2<in R : Entity, in T1, in T2>(val propId: PropId, val block: R.(T1, T2) -> Unit) {
    operator fun getValue(r: R, p: KProperty<*>): (T1, T2) -> Unit = { t1, t2 ->
        // Run bound block and send with disambiguating identity
        r.node.container.runImpulse(r, propId, listOf(t1, t2), { r.block(t1, t2) })
    }
}