package eu.metatools.wepwawet2.components

import eu.metatools.wepwawet2.*
import eu.metatools.wepwawet2.tools.cast
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

/**
 * Maps by associating all the given classes, their properties and their constructors.
 */
class AssociateMapper(classes: Set<KClass<*>>) : Mapper {
    constructor(vararg classes: KClass<*>) : this(classes.toSet())

    private val mapClass = classes
            .withIndex()
            .associate { (i, v) -> v to i.toClassId() }

    private val mapCtor = classes
            .flatMap { it.constructors }
            .withIndex()
            .associate { (i, v) -> v to i.toCtorId() }

    private val mapProp = classes
            .flatMap { it.memberProperties }
            .withIndex()
            .associate { (i, v) -> cast<KProperty1<Any, *>>(v) to i.toPropId() }

    private val unmapClass = classes
            .withIndex()
            .associate { (i, v) -> i.toClassId() to v }

    private val unmapCtor = classes
            .flatMap { it.constructors }
            .withIndex()
            .associate { (i, v) -> i.toCtorId() to v }

    private val unmapProp = classes
            .flatMap { it.memberProperties }
            .withIndex()
            .associate { (i, v) -> i.toPropId() to cast<KProperty1<Any, *>>(v) }

    override fun mapClass(kClass: KClass<*>) =
            mapClass.getValue(kClass)

    override fun mapCtor(kFunction: KFunction<Any>) =
            mapCtor.getValue(kFunction)

    override fun mapProp(kProperty: KProperty1<Any, *>) =
            mapProp.getValue(kProperty)

    override fun unmapClass(classId: ClassId) =
            unmapClass.getValue(classId)

    override fun unmapCtor(ctorId: CtorId) =
            unmapCtor.getValue(ctorId)

    override fun unmapProp(propId: PropId) =
            unmapProp.getValue(propId)
}