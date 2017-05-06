package eu.metatools.wepwawet

import kotlin.reflect.KProperty

class Static<in R : Entity<I>, I, out T>(val config: Config<T>) {
    operator fun getValue(receiver: R, property: KProperty<*>): T {
        return receiver.parent.staticGet(receiver, property, config)
    }
}