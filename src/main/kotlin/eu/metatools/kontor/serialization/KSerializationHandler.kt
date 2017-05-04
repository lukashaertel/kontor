package eu.metatools.kontor.serialization

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandler
import io.netty.channel.ChannelOutboundHandler
import java.nio.charset.Charset
import kotlin.serialization.KSerializer

/**
 * A duplex implementation serialization and deserialization using [KSerializationEncoder] and [KSerializationDecoder].
 */
class KSerializationHandler(
        val charset: Charset = Charsets.UTF_8,
        val serializers: List<KSerializer<*>>,
        private val decoder: KSerializationDecoder = KSerializationDecoder(charset, serializers),
        private val encoder: KSerializationEncoder = KSerializationEncoder(charset, serializers)) :
        ChannelInboundHandler by decoder,
        ChannelOutboundHandler by encoder {
    override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable?) {
        decoder.exceptionCaught(ctx, cause)
        encoder.exceptionCaught(ctx, cause)
    }

    override fun handlerAdded(ctx: ChannelHandlerContext?) {
        decoder.handlerAdded(ctx)
        encoder.handlerAdded(ctx)
    }

    override fun handlerRemoved(ctx: ChannelHandlerContext?) {
        decoder.handlerRemoved(ctx)
        encoder.handlerRemoved(ctx)
    }
}