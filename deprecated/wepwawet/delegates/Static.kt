package eu.metatools.wepwawet.delegates

import eu.metatools.wepwawet.Entity
import kotlin.reflect.KProperty

class Static<in R : Entity, out T>(val config: Config<T>) {
    operator fun getValue(receiver: R, property: KProperty<*>): T {
        return receiver.parent.staticGet(receiver, property, this)
    }
}