package eu.metatools.wepwawet2.components

import eu.metatools.wepwawet2.Id
import eu.metatools.wepwawet2.Net
import kotlinx.coroutines.experimental.channels.Channel
import java.util.*

/**
 * Dummy implementation of [Net], has no functionality.
 */
class DummyNet : Net {
    override val identity = Any()

    override val inbound = Channel<Any>()

    override val outbound = Channel<Any>()

    private val random = Random()

    private var runningId = 0

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