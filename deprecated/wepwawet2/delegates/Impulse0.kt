package eu.metatools.wepwawet2.delegates

import eu.metatools.wepwawet2.Entity
import eu.metatools.wepwawet2.data.PropId
import eu.metatools.wepwawet2.dsls.debugOut
import kotlin.reflect.KProperty

/**
 * An impulse on an entity, no arguments.
 */
data class Impulse0<in R : Entity>(val propId: PropId, val block: R.() -> Unit) {
    operator fun getValue(r: R, p: KProperty<*>): () -> Unit = {
        // Run bound block and send with disambiguating identity
        r.node.container.runImpulse(r, propId, listOf(), { r.block() })
    }
}