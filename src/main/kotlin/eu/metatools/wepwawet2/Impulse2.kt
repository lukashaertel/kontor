package eu.metatools.wepwawet2

import kotlin.reflect.KProperty

/**
 * An impulse on an entity, two arguments.
 */
data class Impulse2<in R : Entity, in T1, in T2>(val propId: PropId, val block: R.(T1, T2) -> Unit, val pre: (R.() -> Boolean)?) {
    operator fun getValue(r: R, p: KProperty<*>): (T1, T2) -> Unit {
        return { t1: T1, t2: T2 ->
            r.node.container.beginImpulse(r.node.id, propId, listOf(t1, t2))
            if (pre?.invoke(r) ?: true)
                r.block(t1, t2)
            r.node.container.endImpulse()
        }
    }
}