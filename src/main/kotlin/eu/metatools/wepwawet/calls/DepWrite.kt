package eu.metatools.wepwawet.calls

import eu.metatools.wepwawet.tracking.Dep
import kotlin.reflect.KProperty

data class DepWrite(val kProperty: KProperty<*>) : Dep {
    override fun toString() = "write ${kProperty.name}"
}