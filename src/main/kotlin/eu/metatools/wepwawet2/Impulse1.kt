package eu.metatools.wepwawet2

import kotlin.reflect.KProperty

/**
 * An impulse on an entity, one argument.
 */
data class Impulse1<in R : Entity, in T1>(val propId: PropId, val block: R.(T1) -> Unit, val pre: (R.() -> Boolean)?) {
    operator fun getValue(r: R, p: KProperty<*>): (T1) -> Unit {
        return { t1: T1 ->
            r.node.container.beginImpulse(r.node.id, propId, listOf(t1))
            if (pre?.invoke(r) ?: true)
                r.block(t1)
            r.node.container.endImpulse()
        }
    }
}