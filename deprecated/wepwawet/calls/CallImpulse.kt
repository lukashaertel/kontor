package eu.metatools.wepwawet.calls

import eu.metatools.wepwawet.tracking.Call
import kotlin.reflect.KProperty

data class CallImpulse(val kProperty: KProperty<*>) : Call {
    override fun toString() = "<<impulse ${kProperty.name}>>"
}