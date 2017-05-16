package eu.metatools.wepwawet2

import kotlin.reflect.KProperty

/**
 * An impulse on an entity, no arguments.
 */
data class Impulse0<in R : Entity>(val propId: PropId, val block: R.() -> Unit, val pre: (R.() -> Boolean)?) {
    operator fun getValue(r: R, p: KProperty<*>): () -> Unit {
        return {
            r.node.container.beginImpulse(r.node.id, propId, listOf())
            if (pre?.invoke(r) ?: true)
                r.block()
            r.node.container.endImpulse()
        }
    }
}