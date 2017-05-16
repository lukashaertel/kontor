package eu.metatools.wepwawet.universe

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.serialization.KSerializer

/**
 * Universe maps all properties and classes in the known game universe. This is necessary for serialization and
 * creating entities for state deserialization.
 */
interface Universe<C, P> {
    /**
     * Returns the number representing the class.
     */
    fun mapClass(kClass: KClass<*>): C

    /**
     * Returns the universe entity creating and containing the represented class.
     */
    fun unmapClass(repClass: C): UniverseEntry<*>

    /**
     * Returns the number representing the property.
     */
    fun mapProperty(kProperty: KProperty1<*, *>): P

    /**
     * Returns the property represented by the number.
     */
    fun unmapProperty(repProperty: P): KProperty1<*, *>

}