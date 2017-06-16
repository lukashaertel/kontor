package eu.metatools.kontor.tools

import rice.p2p.commonapi.Node
import rice.pastry.JoinFailedException
import rice.pastry.PastryNode
import java.util.*
import kotlin.coroutines.experimental.suspendCoroutine
import kotlin.properties.Delegates.notNull

/**
 * Created by pazuzu on 6/13/17.
 */

suspend fun awaitReady(node: PastryNode) = suspendCoroutine<Unit> { c ->
    synchronized(node) {
        if (node.isReady)
            c.resume(Unit)
        else {
            // Make location for the observer
            var observer by notNull<Observer>()

            // Assign observer
            observer = Observer { o, v ->
                if (v is Boolean && v) {
                    o.deleteObserver(observer)
                    c.resume(Unit)
                } else if (v is JoinFailedException) {
                    o.deleteObserver(observer)
                    c.resumeWithException(v)
                }
            }

            // Add to node
            node.addObserver(observer)
        }
    }
}