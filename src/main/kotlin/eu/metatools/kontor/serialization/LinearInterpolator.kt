package eu.metatools.kontor.serialization

import java.math.BigDecimal
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance
import kotlin.serialization.*
import kotlin.serialization.internal.*

/**
 * Safely casts [t] to [T], make sure the cast is valid!!!
 */
inline fun <reified T> safeCast(t: Any): T {
    val x = t as T
    return x
}

/**
 * Interpolates values that are [Serializable], including numeric primitives.
 */
class LinearInterpolator {
    private val aDoubleQueue = LinkedList<Double>()

    private val aBigDecimalQueue = LinkedList<BigDecimal>()

    private val bDoubleQueue = LinkedList<Double>()

    private val bBigDecimalQueue = LinkedList<BigDecimal>()

    private var switch = false

    private var weight = 0.0

    private val doubleQueue get() = if (switch)
        aDoubleQueue
    else
        bDoubleQueue

    private val bigDecimalQueue get() = if (switch)
        aBigDecimalQueue
    else
        bBigDecimalQueue


    private val input = object : ElementValueInput() {
        override fun readBooleanValue(): Boolean {
            return readIntValue() == 0
        }

        private fun lerpDouble() =
                (1.0 - weight) * aDoubleQueue.poll() +
                        weight * bDoubleQueue.poll()

        private fun lerpDecimal() =
                BigDecimal(1.0 - weight) * aBigDecimalQueue.poll() +
                        BigDecimal(weight) * bBigDecimalQueue.poll()

        override fun readByteValue() = lerpDouble().toByte()

        override fun readCharValue() = lerpDouble().toChar()

        override fun readDoubleValue() = lerpDouble()

        override fun readFloatValue() = lerpDouble().toFloat()

        override fun readIntValue() = lerpDouble().toInt()

        override fun readLongValue() = lerpDecimal().toLong()

        override fun readNotNullMark() = true

        override fun readShortValue() = lerpDouble().toShort()

    }

    private val output = object : ElementValueOutput() {
        override fun writeBooleanValue(value: Boolean) {
            writeIntValue(if (value) 1 else 0)
        }

        override fun writeByteValue(value: Byte) {
            doubleQueue.offer(value.toDouble())
        }

        override fun writeCharValue(value: Char) {
            doubleQueue.offer(value.toDouble())
        }

        override fun writeDoubleValue(value: Double) {
            doubleQueue.offer(value)
        }

        override fun writeFloatValue(value: Float) {
            doubleQueue.offer(value.toDouble())
        }

        override fun writeIntValue(value: Int) {
            doubleQueue.offer(value.toDouble())
        }

        override fun writeLongValue(value: Long) {
            bigDecimalQueue.offer(BigDecimal(value))
        }

        override fun writeNotNullMark() {
        }

        override fun writeNullValue() {
            TODO("Nullable values are not supported yet :/")
        }

        override fun writeShortValue(value: Short) {
            doubleQueue.offer(value.toDouble())
        }
    }

    /**
     * Interpolates between [a] and [b] linearly with argument [t].
     */
    fun <T : Any> interpolate(a: T, t: Double, b: T): T {
        reset()

        switch = true
        weight = 1.0 - t
        val akc = put(a)

        switch = false
        weight = t
        val bkc = put(b)

        if (akc != bkc)
            throw IllegalArgumentException("Mismatching classes for $a and $b")

        return get(akc)
    }

    private fun reset() {
        aDoubleQueue.clear()
        bDoubleQueue.clear()
        aBigDecimalQueue.clear()
        bBigDecimalQueue.clear()
    }

    private fun <T : Any> detect(value: T): KSerializer<T> {
        return when (value) {
            is Long -> safeCast(LongSerializer)
            is Boolean -> safeCast(BooleanSerializer)
            is Unit -> safeCast(UnitSerializer)
            is Short -> safeCast(ShortSerializer)
            is Char -> safeCast(CharSerializer)
            is String -> safeCast(StringSerializer)
            is Byte -> safeCast(ByteSerializer)
            is Float -> safeCast(FloatSerializer)
            is Int -> safeCast(IntSerializer)

        // Missing collections

            else -> {
                // Get kotlin class, it contains the companion object
                val kClass = value.javaClass.kotlin

                // Get the companion object as a serializer, that's where serialization info is stored
                val kSerializer = kClass.companionObjectInstance as? KSerializer<*>
                        ?: throw IllegalArgumentException("Not a serializable class")

                // Match types, this should already be a safe cast
                if (kSerializer.serializableClass != kClass)
                    throw IllegalArgumentException("Serializer mismatching serialized class")

                return safeCast(kSerializer)
            }
        }
    }

    private fun <T : Any> put(value: T): KSerializer<T> {
        val ser = detect(value)
        output.write(ser, value)
        return ser
    }

    private fun <T : Any> get(ser: KSerializer<T>): T {
        return input.read(ser)
    }
}

@Serializable
data class Vector(val a: Double, val b: Double, val c: Int)

@Serializable
data class WhackyType(val x: Char, val y: Short, val z: Vector)

fun main(args: Array<String>) {
    val le = LinearInterpolator()

    val va = Vector(100.0, 2.0, 40)
    val vb = Vector(30.0, 8.0, 10)
    val vc = le.interpolate(va, 0.5, vb)
    println(va)
    println(vb)
    println(vc)

    val aa = 10
    val ab = 20
    val ac = le.interpolate(aa, 0.3, ab)
    println(aa)
    println(ab)
    println(ac)

    val wa = WhackyType('a', 200, va)
    val wb = WhackyType('f', 300, vb)
    val wc = le.interpolate(wa, 0.6, wb)

    println(wa)
    println(wb)
    println(wc)


}