package eu.metatools.kontor.serialization

import io.netty.buffer.ByteBuf
import rice.p2p.commonapi.rawserialization.InputBuffer
import java.nio.charset.Charset
import kotlin.reflect.KClass
import kotlin.serialization.ElementValueInput

/**
 * Serialization source reading from a Pastry [InputBuffer].
 */
class InputBufferInput(val inputBuffer: InputBuffer, val charset: Charset = Charsets.UTF_8) : ElementValueInput() {
    override fun readBooleanValue(): Boolean {
        return inputBuffer.readBoolean()
    }

    override fun readByteValue(): Byte {
        return inputBuffer.readByte()
    }

    override fun readCharValue(): Char {
        return inputBuffer.readChar()
    }

    override fun readDoubleValue(): Double {
        return inputBuffer.readDouble()
    }

    override fun <T : Enum<T>> readEnumValue(enumClass: KClass<T>): T {
        return java.lang.Enum.valueOf(enumClass.java, readStringValue())
    }

    override fun readFloatValue(): Float {
        return inputBuffer.readFloat()
    }

    override fun readIntValue(): Int {
        return inputBuffer.readInt()
    }

    override fun readLongValue(): Long {
        return inputBuffer.readLong()
    }

    override fun readNotNullMark(): Boolean {
        return inputBuffer.readBoolean()
    }

    override fun readShortValue(): Short {
        return inputBuffer.readShort()
    }

    override fun readStringValue(): String {
        val size = inputBuffer.readShort()
        val buffer = ByteArray(size.toInt())
        inputBuffer.read(buffer, 0, size.toInt())
        return String(buffer, charset)
    }
}