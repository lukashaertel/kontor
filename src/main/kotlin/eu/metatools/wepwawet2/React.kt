package eu.metatools.wepwawet2

import kotlin.reflect.KProperty

/**
 * A change reactor.
 */
data class React<in R : Entity>(val depends: List<PropId>, val block: R.() -> Unit) {
    operator fun getValue(r: R, p: KProperty<*>) = Unit
}