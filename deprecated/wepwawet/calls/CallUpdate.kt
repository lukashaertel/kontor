package eu.metatools.wepwawet.calls

import eu.metatools.wepwawet.tracking.Call
import kotlin.reflect.KProperty

data class CallUpdate(val kProperty: KProperty<*>) : Call {
    override fun toString() = "<<update ${kProperty.name}>>"
}