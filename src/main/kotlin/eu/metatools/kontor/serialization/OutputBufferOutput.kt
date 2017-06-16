package eu.metatools.kontor.serialization

import io.netty.buffer.ByteBuf
import rice.p2p.commonapi.rawserialization.OutputBuffer
import java.nio.charset.Charset
import kotlin.reflect.KClass
import kotlin.serialization.ElementValueOutput

/**
 * Serialization target writing into a Pastry [OutputBuffer].
 */
class OutputBufferOutput(val outputBuffer: OutputBuffer, val charset: Charset = Charsets.UTF_8) : ElementValueOutput() {
    override fun writeBooleanValue(value: Boolean) {
        outputBuffer.writeBoolean(value)
    }

    override fun writeByteValue(value: Byte) {
        outputBuffer.writeByte(value)
    }

    override fun writeCharValue(value: Char) {
        outputBuffer.writeChar(value)
    }

    override fun writeDoubleValue(value: Double) {
        outputBuffer.writeDouble(value)
    }

    override fun <T : Enum<T>> writeEnumValue(enumClass: KClass<T>, value: T) {
        writeStringValue(value.name)
    }

    override fun writeFloatValue(value: Float) {
        outputBuffer.writeFloat(value)
    }

    override fun writeIntValue(value: Int) {
        outputBuffer.writeInt(value)
    }

    override fun writeLongValue(value: Long) {
        outputBuffer.writeLong(value)
    }

    override fun writeNotNullMark() {
        outputBuffer.writeBoolean(true)
    }

    override fun writeNullValue() {
        outputBuffer.writeBoolean(false)
    }

    override fun writeShortValue(value: Short) {
        outputBuffer.writeShort(value)
    }

    override fun writeStringValue(value: String) {
        val bytes = value.toByteArray(charset)
        outputBuffer.writeShort(bytes.size.toShort())
        outputBuffer.write(bytes, 0, bytes.size)
    }
}