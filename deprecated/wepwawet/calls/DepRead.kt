package eu.metatools.wepwawet.calls

import eu.metatools.wepwawet.tracking.Dep
import kotlin.reflect.KProperty

data class DepRead(val kProperty: KProperty<*>) : Dep {
    override fun toString() = "read ${kProperty.name}"
}