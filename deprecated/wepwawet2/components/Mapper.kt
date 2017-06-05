package eu.metatools.wepwawet2.components

import eu.metatools.wepwawet2.data.*
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty1

/**
 * Mapper for identification and transfer of networked entities.
 */
interface Mapper {
    /**
     * Returns the identity of the class.
     */
    fun mapClass(kClass: KClass<*>): ClassId

    /**
     * Returns the identity of the constructor.
     */
    fun mapCreate(kFunction: KFunction<Any>): CreateId

    /**
     * Returns the identity of the property.
     */
    fun mapProp(kProperty: KProperty1<Any, *>): PropId

    /**
     * Returns the class for the identity.
     */
    fun unmapClass(classId: ClassId): KClass<*>

    /**
     * Returns the constructor for the identity.
     */
    fun unmapCreate(ctorId: CreateId): KFunction<Any>

    /**
     * Returns the property for the identity.
     */
    fun unmapProp(propId: PropId): KProperty1<Any, *>

    operator fun get(kClass: KClass<*>) =
            mapClass(kClass)

    operator fun get(kFunction: KFunction<Any>) =
            mapCreate(kFunction)

    operator fun get(kProperty: KProperty1<Any, *>) =
            mapProp(kProperty)
}