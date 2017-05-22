package eu.metatools.wepwawet2

import kotlin.reflect.KProperty

/**
 * An impulse on an entity, three arguments.
 */
data class Impulse3<in R : Entity, in T1, in T2, in T3>(val propId: PropId, val block: R.(T1, T2, T3) -> Unit) {
    operator fun getValue(r: R, p: KProperty<*>): (T1, T2, T3) -> Unit = { t1, t2, t3 ->
        // Run bound block and send with disambiguating identity
        r.node.container.runImpulse(r, propId, listOf(t1, t2, t3), { r.block(t1, t2, t3) })
    }
}