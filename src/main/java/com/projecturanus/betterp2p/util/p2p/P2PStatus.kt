package com.projecturanus.betterp2p.util.p2p

import appeng.api.networking.IGrid
import appeng.api.networking.IGridHost
import appeng.parts.p2p.PartP2PTunnel
import appeng.parts.p2p.PartP2PTunnelME
import com.projecturanus.betterp2p.util.listTargetGridP2P

data class P2PStatus(val id: Int, val grid: IGrid, val targetP2P: PartP2PTunnel<*>? = null) {
    val listP2P: List<PartP2PTunnel<*>> = listTargetGridP2P(grid, targetP2P?.javaClass ?: PartP2PTunnelME::class.java)
}
