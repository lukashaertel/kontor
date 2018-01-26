package eu.metatools.kontor.serialization

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ReplayingDecoder
import java.nio.charset.Charset
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

/**
 * Kotlin Serialization Decoder, uses a list of serializers to decode messages.
 */
class KSerializationDecoder(
        val charset: Charset = Charsets.UTF_8,
        val classes: List<KClass<*>>) : ReplayingDecoder<Any?>() {

    private val indication = classes.map { it.serializer() }

    private fun readId(buf: ByteBuf) =
            if (classes.size <= Byte.MAX_VALUE)
                buf.readByte().toInt()
            else if (classes.size <= Short.MAX_VALUE)
                buf.readShort().toInt()
            else
                buf.readInt()


    override fun decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any?>) {
        // Get index of serializer or null indicator
        val index = readId(buf)
        if (index == 0) {
            out += null as Any?
            return
        }

        // Get serializer from list of known serializers
        val serializer = indication[index - 1]

        // Decode appropriately
        val decoder = ByteBufInput(buf, charset)
        val item = decoder.read(serializer)

        // Add to output
        out += item
    }
}