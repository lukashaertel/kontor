package eu.metatools.kontor.serialization

import io.netty.buffer.ByteBuf
import java.nio.charset.Charset
import kotlin.reflect.KClass
import kotlinx.serialization.ElementValueOutput

/**
 * Serialization target writing into a Netty [ByteBuf].
 */
class ByteBufOutput(val byteBuf: ByteBuf, val charset: Charset = Charsets.UTF_8) : ElementValueOutput() {
    override fun writeBooleanValue(value: Boolean) {
        byteBuf.writeBoolean(value)
    }

    override fun writeByteValue(value: Byte) {
        byteBuf.writeByte(value.toInt())
    }

    override fun writeCharValue(value: Char) {
        byteBuf.writeChar(value.toInt())
    }

    override fun writeDoubleValue(value: Double) {
        byteBuf.writeDouble(value)
    }

    override fun <T : Enum<T>> writeEnumValue(enumClass: KClass<T>, value: T) {
        writeStringValue(value.name)
    }

    override fun writeFloatValue(value: Float) {
        byteBuf.writeFloat(value)
    }

    override fun writeIntValue(value: Int) {
        byteBuf.writeInt(value)
    }

    override fun writeLongValue(value: Long) {
        byteBuf.writeLong(value)
    }

    override fun writeNotNullMark() {
        byteBuf.writeBoolean(true)
    }

    override fun writeNullValue() {
        byteBuf.writeBoolean(false)
    }

    override fun writeShortValue(value: Short) {
        byteBuf.writeShort(value.toInt())
    }

    override fun writeStringValue(value: String) {
        val bytes = value.toByteArray(charset)
        byteBuf.writeShort(bytes.size)
        byteBuf.writeBytes(bytes)
    }
}