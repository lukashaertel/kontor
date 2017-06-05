package eu.metatools.wepwawet.universe

import eu.metatools.wepwawet.Wepwawet
import kotlin.reflect.KClass

/**
 * Represents an abstract entry to the universe.
 */
interface UniverseEntry<R : Any> {
    val kClass: KClass<R>

    @Suppress("UNCHECKED_CAST")
    operator fun invoke(wepwawet: Wepwawet, id: Int, vararg args: Any?) =
            when (args.size) {
                0 -> (this as UniverseEntryZero<R>).create(wepwawet, id)
                1 -> (this as UniverseEntryOne<R, Any?>).create(wepwawet, id, args[0])
                2 -> (this as UniverseEntryTwo<R, Any?, Any?>).create(wepwawet, id, args[0], args[1])
                3 -> (this as UniverseEntryThree<R, Any?, Any?, Any?>).create(wepwawet, id, args[0], args[1], args[2])
                else -> error("Unsupported argument count ${args.size}")
            }
}

/**
 * Represents a zero argument constructor entry to the universe.
 */
data class UniverseEntryZero<R : Any>(
        override val kClass: KClass<R>,
        val create: (Wepwawet, Int) -> R) : UniverseEntry<R> {
}

/**
 * Represents a onr argument constructor entry to the universe.
 */
data class UniverseEntryOne<R : Any, in T1>(
        override val kClass: KClass<R>,
        val create: (Wepwawet, Int, T1) -> R) : UniverseEntry<R> {
}

/**
 * Represents a two argument constructor entry to the universe.
 */
data class UniverseEntryTwo<R : Any, in T1, in T2>(
        override val kClass: KClass<R>,
        val create: (Wepwawet, Int, T1, T2) -> R) : UniverseEntry<R> {
}

/**
 * Represents a three argument constructor entry to the universe.
 */
data class UniverseEntryThree<R : Any, in T1, in T2, in T3>(
        override val kClass: KClass<R>,
        val create: (Wepwawet, Int, T1, T2, T3) -> R) : UniverseEntry<R> {
}

/**
 * Creates a zero argument universal entry.
 */
@JvmName("universeEntry0")
inline fun <reified R : Any> universeEntry(noinline create: (Wepwawet, Int) -> R) =
        UniverseEntryZero(R::class, create)

/**
 * Creates a one argument universal entry.
 */
@JvmName("universeEntry1")
inline fun <reified R : Any, T1> universeEntry(noinline create: (Wepwawet, Int, T1) -> R) =
        UniverseEntryOne(R::class, create)

/**
 * Creates a two argument universal entry.
 */
@JvmName("universeEntry2")
inline fun <reified R : Any, T1, T2> universeEntry(noinline create: (Wepwawet, Int, T1, T2) -> R) =
        UniverseEntryTwo(R::class, create)

/**
 * Creates a three argument universal entry.
 */
@JvmName("universeEntry3")
inline fun <reified R : Any, T1, T2, T3> universeEntry(noinline create: (Wepwawet, Int, T1, T2, T3) -> R) =
        UniverseEntryThree(R::class, create)