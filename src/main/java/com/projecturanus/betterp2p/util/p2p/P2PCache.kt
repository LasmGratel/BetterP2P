package com.projecturanus.betterp2p.util.p2p

import java.util.*

/**
 * Cache P2P status for client to use
 */
object P2PCache {
    val statusMap: MutableMap<UUID, P2PStatus> = Collections.synchronizedMap(WeakHashMap())
}
