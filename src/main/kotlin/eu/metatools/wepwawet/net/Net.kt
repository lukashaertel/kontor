package eu.metatools.wepwawet.net

import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlin.coroutines.experimental.buildSequence

/**
 * Network handler for [eu.metatools.wepwawet.Wepwawet], needs a client [identity] and an [inbound] and [outbound]
 * channel. This class provides network synchronized non-colliding identities for [eu.metatools.wepwawet.Entity].
 */
class Net(
        val identity: Any,
        val inbound: ReceiveChannel<WepwawetMsg>,
        val outbound: SendChannel<WepwawetMsg>) {

    // TODO Network synchronized ranges of IDs
    private var runningId = 0

    fun getAndLeaseId() =
            runningId.also { runningId++ }

    fun releaseId(id: Int) {

    }
}
