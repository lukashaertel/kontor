package eu.metatools.kontor.serialization

import io.netty.buffer.ByteBuf
import java.io.DataInput
import java.nio.charset.Charset
import kotlin.reflect.KClass
import kotlin.serialization.ElementValueInput

/**
 * Serialization source reading from a Netty [ByteBuf].
 */
class DataInputInput(val dataInput: DataInput, val charset: Charset = Charsets.UTF_8) : ElementValueInput() {
    override fun readBooleanValue(): Boolean {
        return dataInput.readBoolean()
    }

    override fun readByteValue(): Byte {
        return dataInput.readByte()
    }

    override fun readCharValue(): Char {
        return dataInput.readChar()
    }

    override fun readDoubleValue(): Double {
        return dataInput.readDouble()
    }

    override fun <T : Enum<T>> readEnumValue(enumClass: KClass<T>): T {
        return java.lang.Enum.valueOf(enumClass.java, readStringValue())
    }

    override fun readFloatValue(): Float {
        return dataInput.readFloat()
    }

    override fun readIntValue(): Int {
        return dataInput.readInt()
    }

    override fun readLongValue(): Long {
        return dataInput.readLong()
    }

    override fun readNotNullMark(): Boolean {
        return dataInput.readBoolean()
    }

    override fun readShortValue(): Short {
        return dataInput.readShort()
    }

    override fun readStringValue(): String {
        val size = dataInput.readShort()
        val buffer = ByteArray(size.toInt())
        dataInput.readFully(buffer, 0, size.toInt())
        return String(buffer, charset)
    }
}