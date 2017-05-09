package eu.metatools.wepwawet.calls

import eu.metatools.wepwawet.tracking.Dep
import kotlin.reflect.KProperty

data class DepInit(val kProperty: KProperty<*>) : Dep {
    override fun toString() = "init ${kProperty.name}"
}