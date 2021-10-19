package com.projecturanus.betterp2p.network

import com.projecturanus.betterp2p.util.p2p.P2PCache
import com.projecturanus.betterp2p.util.p2p.getInfo
import com.projecturanus.betterp2p.util.p2p.linkP2P
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext

class ServerLinkP2PHandler : IMessageHandler<C2SLinkP2P, S2CRefreshInfo?> {
    override fun onMessage(message: C2SLinkP2P, ctx: MessageContext): S2CRefreshInfo? {
        if (!P2PCache.statusMap.containsKey(ctx.serverHandler.player.uniqueID)) return null
        val status = P2PCache.statusMap[ctx.serverHandler.player.uniqueID]!!
        val input = status.listP2P[message.input]
        val output = status.listP2P[message.output]

        if (linkP2P(ctx.serverHandler.player, input, output)) {
            return S2CRefreshInfo(listOf(input.getInfo(message.input), output.getInfo(message.output)))
        }
        return null
    }
}
