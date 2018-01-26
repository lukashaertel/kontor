package eu.metatools.kontor.serialization

import io.netty.buffer.ByteBuf
import java.io.DataOutput
import java.nio.charset.Charset
import kotlin.reflect.KClass
import kotlinx.serialization.ElementValueOutput

/**
 * Serialization target writing into a Netty [ByteBuf].
 */
class DataOutputOutput(val dataOutput: DataOutput, val charset: Charset = Charsets.UTF_8) : ElementValueOutput() {
    override fun writeBooleanValue(value: Boolean) {
        dataOutput.writeBoolean(value)
    }

    override fun writeByteValue(value: Byte) {
        dataOutput.writeByte(value.toInt())
    }

    override fun writeCharValue(value: Char) {
        dataOutput.writeChar(value.toInt())
    }

    override fun writeDoubleValue(value: Double) {
        dataOutput.writeDouble(value)
    }

    override fun <T : Enum<T>> writeEnumValue(enumClass: KClass<T>, value: T) {
        writeStringValue(value.name)
    }

    override fun writeFloatValue(value: Float) {
        dataOutput.writeFloat(value)
    }

    override fun writeIntValue(value: Int) {
        dataOutput.writeInt(value)
    }

    override fun writeLongValue(value: Long) {
        dataOutput.writeLong(value)
    }

    override fun writeNotNullMark() {
        dataOutput.writeBoolean(true)
    }

    override fun writeNullValue() {
        dataOutput.writeBoolean(false)
    }

    override fun writeShortValue(value: Short) {
        dataOutput.writeShort(value.toInt())
    }

    override fun writeStringValue(value: String) {
        val bytes = value.toByteArray(charset)
        dataOutput.writeShort(bytes.size)
        dataOutput.write(bytes)
    }
}