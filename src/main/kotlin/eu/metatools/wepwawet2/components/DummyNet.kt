package eu.metatools.wepwawet2.components

import eu.metatools.wepwawet2.Id
import eu.metatools.wepwawet2.Net
import kotlinx.coroutines.experimental.channels.Channel
import java.util.*

/**
 * Dummy implementation of [Net], has no functionality.
 */
class DummyNet(
        override val identity: Any = Any(),
         startId: Id,
        override val inbound: Channel<Any> = Channel<Any>(),
        override val outbound: Channel<Any> = Channel<Any>()) : Net {
    private val random = Random()

    private var runningId = startId

    override fun getRootId() = -1

    override fun getAndLeaseId(): Id {
        return runningId++
    }

    override fun releaseId(id: Id) {
    }

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