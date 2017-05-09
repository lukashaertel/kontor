package eu.metatools.wepwawet.delegates

import eu.metatools.wepwawet.Entity
import kotlin.reflect.KProperty

class Impulse<in R : Entity, in T>(val block: R.(T) -> Unit) {
    operator fun getValue(receiver: R, property: KProperty<*>): (T) -> Unit {
        return { receiver.parent.impulseExecute(receiver, property, it, this) }
    }
}