package eu.metatools.kontor.serialization

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ReplayingDecoder
import java.nio.charset.Charset
import kotlin.serialization.KSerializer

/**
 * Kotlin Serialization Decoder, uses a list of serializers to decode messages.
 */
class KSerializationDecoder(
        val charset: Charset = Charsets.UTF_8,
        val serializers: List<KSerializer<*>>) : ReplayingDecoder<Any?>() {

    private fun readId(buf: ByteBuf) =
            if (serializers.size <= Byte.MAX_VALUE)
                buf.readByte().toInt()
            else if (serializers.size <= Short.MAX_VALUE)
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
        val serializer = serializers[index - 1]

        // Decode appropriately
        val decoder = ByteBufInput(buf, charset)
        val item = decoder.read(serializer)

        // Add to output
        out += item
    }
}