package eu.metatools.wepwawet

import kotlin.reflect.KProperty

class Impulse<in R : Entity<I>, I, in T>(val impulse: R.(T) -> Unit) {
    operator fun getValue(receiver: R, property: KProperty<*>): (T) -> Unit {
        return { receiver.parent.impulseExecute(receiver, property, it, impulse) }
    }
}