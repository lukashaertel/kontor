package eu.metatools.wepwawet.tools

typealias SetDiff<E> = Pair<Set<E>, Set<E>>

infix fun <E> Set<E>.diff(other: Set<E>) = (this - other) to (other - this)