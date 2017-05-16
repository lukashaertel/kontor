package eu.metatools.wepwawet2

import kotlin.reflect.KProperty

/**
 * An impulse on an entity, three arguments.
 */
data class Impulse3<in R : Entity, in T1, in T2, in T3>(val propId: PropId, val block: R.(T1, T2, T3) -> Unit, val pre: (R.() -> Boolean)?) {
    operator fun getValue(r: R, p: KProperty<*>): (T1, T2, T3) -> Unit {
        return { t1: T1, t2: T2, t3: T3 ->
            r.node.container.beginImpulse(r.node.id, propId, listOf(t1, t2, t3))
            if (pre?.invoke(r) ?: true)
                r.block(t1, t2, t3)
            r.node.container.endImpulse()
        }
    }
}