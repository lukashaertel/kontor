package eu.metatools.wepwawet

import eu.metatools.wepwawet.utils.flatAssociate
import eu.metatools.wepwawet.utils.invert
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

class Mapper(val classes: Collection<KClass<*>>) {
    /**
     * Classes to number association
     */
    private val classToNum = classes
            .withIndex()
            .associate { (i, v) -> v to i.toShort() }

    /**
     * Constructors to number association
     */
    private val constructorToNum = classes
            .withIndex()
            .flatAssociate { (i, v) ->
                v.constructors.withIndex().map { (j, c) ->
                    c to ConstructorId(i.toShort(), j.toByte())
                }
            }

    /**
     * Members to number association
     */
    private val memberToNum = classes
            .withIndex()
            .flatAssociate { (i, v) ->
                v.memberProperties.withIndex().map { (j, m) ->
                    @Suppress("unchecked_cast")
                    (m as KProperty1<Any, *>) to MemberId(i.toShort(), j.toByte())
                }
            }

    /**
     * Inversion of [classToNum].
     */
    private val numToClass = classToNum.invert()

    /**
     * Inversion of [constructorToNum].
     */
    private val numToConstructor = constructorToNum.invert()

    /**
     * Inversion of [memberToNum].
     */
    private val numToMember = memberToNum.invert()

    /**
     * Constructor count per class.
     */
    private val constructorsSize = classes
            .withIndex()
            .associate { (i, c) -> i.toShort() to c.constructors.size }

    /**
     * Member count per class.
     */
    private val membersSize = classes
            .withIndex()
            .associate { (i, c) -> i.toShort() to c.memberProperties.size }

    /**
     * Maps the input to the numerical representation.
     */
    fun get(kClass: KClass<*>) =
            classToNum.getValue(kClass)

    /**
     * Maps the input to the numerical representation.
     */
    fun get(kFunction: KFunction<Any>) =
            constructorToNum.getValue(kFunction)

    /**
     * Maps the input to the numerical representation.
     */
    fun get(kProperty: KProperty1<Any, *>) =
            memberToNum.getValue(kProperty)

    /**
     * Maps the numerical representation to the actual value.
     */
    fun get(classNum: Short) =
            numToClass.getValue(classNum)

    /**
     * Maps the numerical representation to the actual value.
     */
    fun get(constructorId: ConstructorId) =
            numToConstructor.getValue(constructorId)

    /**
     * Maps the numerical representation to the actual value.
     */
    fun get(memberId: MemberId) =
            numToMember.getValue(memberId)

    /**
     * Gets the number of constructors for class represented by [classNum].
     */
    fun constructorsSize(classNum: Short) =
            constructorsSize.getValue(classNum)

    /**
     * Gets the number of members for class represented by [classNum].
     */
    fun membersSize(classNum: Short) =
            membersSize.getValue(classNum)

    /**
     * Gets the name of the class represented by [classNum].
     */
    fun name(classNum: Short) =
            get(classNum).simpleName

    /**
     * Gets the name of the constructor represented by [constructorId].
     */
    fun name(constructorId: ConstructorId) = buildString {
        val c = get(constructorId.classNum)
        val f = get(constructorId)

        append(c.simpleName)
        append('(')
        var s = false
        for (p in f.parameters) {
            if (s)
                append(", ")
            append(p.name)
            s = true
        }
        append(')')
    }

    /**
     * Gets the name of the member represented by [memberId].
     */
    fun name(memberId: MemberId) =
            get(memberId).name
}