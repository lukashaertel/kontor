package eu.metatools.wepwawet.universe

import kotlin.serialization.KInput
import kotlin.serialization.KOutput

/**
 * Crosscast with serialization helpers.
 */
enum class NumericDomain {
    BYTE {
        override fun readIn(input: KInput) = input.readByteValue()

        override fun writeTo(number: Number, output: KOutput) = output.writeByteValue(number.toByte())

        override fun invoke(int: Int) = int.toByte()
    },
    SHORT {
        override fun readIn(input: KInput) = input.readShortValue()

        override fun writeTo(number: Number, output: KOutput) = output.writeShortValue(number.toShort())

        override fun invoke(int: Int) = int.toShort()
    },
    INT {
        override fun readIn(input: KInput) = input.readIntValue()

        override fun writeTo(number: Number, output: KOutput) = output.writeIntValue(number.toInt())

        override fun invoke(int: Int) = int
    };

    companion object {
        /**
         * Returns the appropriate cross cast.
         */
        fun select(size: Int): NumericDomain =
                when {
                    size <= Byte.MAX_VALUE -> BYTE
                    size <= Short.MAX_VALUE -> SHORT
                    size <= Int.MAX_VALUE -> INT
                    else -> error(Unit)
                }
    }

    abstract operator fun invoke(int: Int): Number

    abstract fun readIn(input: KInput): Number

    abstract fun writeTo(number: Number, output: KOutput)
}