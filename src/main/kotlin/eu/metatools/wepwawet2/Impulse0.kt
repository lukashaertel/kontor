package eu.metatools.wepwawet2

import kotlin.reflect.KProperty

/**
 * An impulse on an entity, no arguments.
 */
data class Impulse0<in R : Entity>(val propId: PropId, val block: R.() -> Unit) {
    operator fun getValue(r: R, p: KProperty<*>): () -> Unit = {
        // Run bound block and send with disambiguating identity
        r.node.container.runImpulse(r.node.id, propId, listOf(), { r.block() })
    }
}