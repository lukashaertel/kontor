package eu.metatools.kontor.serialization

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandler
import io.netty.channel.ChannelOutboundHandler
import java.nio.charset.Charset
import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass

/**
 * A duplex implementation serialization and deserialization using [KSerializationEncoder] and [KSerializationDecoder].
 */
class KSerializationHandler(
        val charset: Charset = Charsets.UTF_8,
        val classes: List<KClass<*>>,
        private val decoder: KSerializationDecoder = KSerializationDecoder(charset, classes),
        private val encoder: KSerializationEncoder = KSerializationEncoder(charset, classes)) :
        ChannelInboundHandler by decoder,
        ChannelOutboundHandler by encoder {
    @Suppress("deprecation", "overridingDeprecatedMember")
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