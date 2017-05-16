package eu.metatools.wepwawet2

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
    fun mapCtor(kFunction: KFunction<Any>): CtorId

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
    fun unmapCtor(ctorId: CtorId): KFunction<Any>

    /**
     * Returns the property for the identity.
     */
    fun unmapProp(propId: PropId): KProperty1<Any, *>
}