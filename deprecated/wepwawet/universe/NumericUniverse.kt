package eu.metatools.wepwawet.universe

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.serialization.KInput
import kotlin.serialization.KOutput
import kotlin.serialization.KSerializer

/**
 * Builds a map universe from all the entries given to the constructor. There can be created by calling [universeEntry]
 * on the primary constructor, which should take the container, the ID, and some optional arguments.
 */
class NumericUniverse(
        val classDomain: NumericDomain,
        val propertyDomain: NumericDomain,
        classes: Set<UniverseEntry<*>>) : Universe<Number, Number> {
    constructor(
            classDomain: NumericDomain,
            propertyDomain: NumericDomain,
            vararg classes: UniverseEntry<*>) : this(classDomain, propertyDomain, classes.toSet())

    constructor(classes: Set<UniverseEntry<*>>) : this(
            NumericDomain.select(classes.size),
            NumericDomain.select(classes.map { it.kClass.memberProperties.size }.sum()), classes)

    constructor(vararg classes: UniverseEntry<*>) : this(
            NumericDomain.select(classes.size),
            NumericDomain.select(classes.map { it.kClass.memberProperties.size }.sum()), *classes)

    private val classesMap = classes
            .withIndex()
            .associate { (i, v) -> classDomain(i) to v }

    private val classesMapInv = classes
            .withIndex()
            .associate { (i, v) -> v.kClass to classDomain(i) }

    private val propertiesMap = classes
            .flatMap { it.kClass.memberProperties }
            .withIndex()
            .associate { (i, v) -> propertyDomain(i) to v as KProperty1<*, *> }

    private val propertiesMapInv = classes
            .flatMap { it.kClass.memberProperties }
            .withIndex()
            .associate { (i, v) -> v as KProperty1<*, *> to propertyDomain(i) }

    override fun mapClass(kClass: KClass<*>): Number {
        return classesMapInv.getValue(kClass)
    }

    override fun unmapClass(repClass: Number): UniverseEntry<*> {
        return classesMap.getValue(repClass)
    }

    override fun mapProperty(kProperty: KProperty1<*, *>): Number {
        return propertiesMapInv.getValue(kProperty)
    }

    override fun unmapProperty(repProperty: Number): KProperty1<*, *> {
        return propertiesMap.getValue(repProperty)
    }

    val universeEntrySerializer = object : KSerializer<UniverseEntry<*>> {
        override val serializableClass: KClass<*>
            get() = UniverseEntry::class

        override fun save(output: KOutput, obj: UniverseEntry<*>) {
            classDomain.writeTo(mapClass(obj.kClass), output)
        }

        override fun load(input: KInput): UniverseEntry<*> {
            return unmapClass(classDomain.readIn(input))
        }
    }


    val classSerializer = object : KSerializer<KClass<*>> {
        override val serializableClass: KClass<*>
            get() = KClass::class

        override fun save(output: KOutput, obj: KClass<*>) {
            classDomain.writeTo(mapClass(obj), output)
        }

        override fun load(input: KInput): KClass<*> {
            return unmapClass(classDomain.readIn(input)).kClass
        }
    }

    val propertySerializer = object : KSerializer<KProperty1<*, *>> {
        override val serializableClass: KClass<*>
            get() = KProperty1::class

        override fun save(output: KOutput, obj: KProperty1<*, *>) {
            propertyDomain.writeTo(mapProperty(obj), output)
        }

        override fun load(input: KInput): KProperty1<*, *> {
            return unmapProperty(propertyDomain.readIn(input))
        }
    }
}