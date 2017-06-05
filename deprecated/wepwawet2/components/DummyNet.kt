package eu.metatools.wepwawet2.components

import eu.metatools.wepwawet2.data.Id
import kotlinx.coroutines.experimental.channels.Channel
import java.util.*

/**
 * Dummy implementation of [Net], has no functionality.
 */
class DummyNet(
        override val identity: Any = Any(),
        override val inbound: Channel<Any> = Channel<Any>(16),
        override val outbound: Channel<Any> = Channel<Any>(16)) : Net {
    private val random = Random()


    override fun random(): Double {
        return random.nextDouble()
    }

    override fun random(range: IntRange): Int {
        return range.first + random.nextInt(range.last - range.first)
    }

    override fun random(range: LongRange): Long {
        return range.first + random.nextLong().rem(range.last - range.first)
    }

}