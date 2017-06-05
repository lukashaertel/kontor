package eu.metatools.wepwawet2.delegates

import eu.metatools.wepwawet2.Entity
import eu.metatools.wepwawet2.data.PropId
import eu.metatools.wepwawet2.dsls.debugOut
import kotlin.reflect.KProperty

/**
 * An impulse on an entity, one argument.
 */
data class Impulse1<in R : Entity, in T1>(val propId: PropId, val block: R.(T1) -> Unit) {
    operator fun getValue(r: R, p: KProperty<*>): (T1) -> Unit = { t1 ->
        // Run bound block and send with disambiguating identity
        r.node.container.runImpulse(r, propId, listOf(t1), { r.block(t1) })
    }
}