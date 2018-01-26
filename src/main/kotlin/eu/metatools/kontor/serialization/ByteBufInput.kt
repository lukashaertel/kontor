package eu.metatools.kontor.serialization

import io.netty.buffer.ByteBuf
import java.nio.charset.Charset
import kotlin.reflect.KClass
import kotlinx.serialization.ElementValueInput

/**
 * Serialization source reading from a Netty [ByteBuf].
 */
class ByteBufInput(val byteBuf: ByteBuf, val charset: Charset = Charsets.UTF_8) : ElementValueInput() {
    override fun readBooleanValue(): Boolean {
        return byteBuf.readBoolean()
    }

    override fun readByteValue(): Byte {
        return byteBuf.readByte()
    }

    override fun readCharValue(): Char {
        return byteBuf.readChar()
    }

    override fun readDoubleValue(): Double {
        return byteBuf.readDouble()
    }

    override fun <T : Enum<T>> readEnumValue(enumClass: KClass<T>): T {
        return java.lang.Enum.valueOf(enumClass.java, readStringValue())
    }

    override fun readFloatValue(): Float {
        return byteBuf.readFloat()
    }

    override fun readIntValue(): Int {
        return byteBuf.readInt()
    }

    override fun readLongValue(): Long {
        return byteBuf.readLong()
    }

    override fun readNotNullMark(): Boolean {
        return byteBuf.readBoolean()
    }

    override fun readShortValue(): Short {
        return byteBuf.readShort()
    }

    override fun readStringValue(): String {
        val size = byteBuf.readShort()
        val buffer = ByteArray(size.toInt())
        byteBuf.readBytes(buffer)
        return String(buffer, charset)
    }
}