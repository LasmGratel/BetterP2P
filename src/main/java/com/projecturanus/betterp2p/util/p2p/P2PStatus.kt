package com.projecturanus.betterp2p.util.p2p

import appeng.api.networking.IGrid
import appeng.parts.p2p.PartP2PTunnel
import appeng.parts.p2p.PartP2PTunnelME
import com.projecturanus.betterp2p.util.listTargetGridP2P
import net.minecraft.entity.player.EntityPlayer

class P2PStatus(player: EntityPlayer, val id: Int, val grid: IGrid, val targetP2P: PartP2PTunnel<*>? = null) {
    val listP2P: MutableList<PartP2PTunnel<*>> = listTargetGridP2P(grid, player, targetP2P?.javaClass ?: PartP2PTunnelME::class.java).toMutableList()
}
