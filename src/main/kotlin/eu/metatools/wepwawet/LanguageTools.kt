package eu.metatools.wepwawet

import kotlin.reflect.KMutableProperty0

inline fun <T, U> KMutableProperty0<T>.replaceIn(with: T, block: () -> U) {
    val v = get()
    try {
        set(with)
        block()
    } finally {
        set(v)
    }
}

inline fun <T : Any, U> KMutableProperty0<T?>.replaceNullWith(withOnNull: T?, block: () -> U) {
    val v = get()
    try {
        if (v == null)
            set(withOnNull)
        block()
    } finally {
        if (v == null)
            set(v)
    }
}