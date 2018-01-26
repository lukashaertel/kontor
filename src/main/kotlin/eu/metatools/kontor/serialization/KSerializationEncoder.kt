package eu.metatools.kontor.serialization

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import java.nio.charset.Charset
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

/**
 * Kotlin Serialization Encoder, uses a list of serializers to encode messages.
 */
class KSerializationEncoder(
        val charset: Charset = Charsets.UTF_8,
        val classes: List<KClass<*>>) : MessageToByteEncoder<Any?>() {

    /**
     * Associate by the serialized class, retain index for writing to streams.
     */
    private val indication = classes.withIndex().associate { it.value to (it.index to it.value.serializer()) }

    private fun writeId(buf: ByteBuf, index: Int) =
            if (classes.size <= Byte.MAX_VALUE)
                buf.writeByte(index)
            else if (classes.size <= Short.MAX_VALUE)
                buf.writeShort(index)
            else
                buf.writeInt(index)


    override fun encode(ctx: ChannelHandlerContext, msg: Any?, buf: ByteBuf) {
        // Write null indicator
        if (msg == null) {
            writeId(buf, 0)
            return
        }

        // Get serializer from indication
        val (index, serializer) = indication.getValue(msg::class)
        writeId(buf, index + 1)

        // Do cast to avoid type problems, we checked this before with serializableClass
        @Suppress("UNCHECKED_CAST")
        serializer as KSerializer<Any>

        // Encode properly
        val encoder = ByteBufOutput(buf, charset)
        encoder.write(serializer, msg)
    }
}
