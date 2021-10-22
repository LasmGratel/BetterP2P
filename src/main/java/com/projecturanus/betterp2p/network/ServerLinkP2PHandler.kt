package com.projecturanus.betterp2p.network

import com.projecturanus.betterp2p.util.p2p.P2PCache
import com.projecturanus.betterp2p.util.p2p.getInfo
import com.projecturanus.betterp2p.util.p2p.linkP2P
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext

class ServerLinkP2PHandler : IMessageHandler<C2SLinkP2P, IMessage?> {
    override fun onMessage(message: C2SLinkP2P, ctx: MessageContext): IMessage? {
        if (!P2PCache.statusMap.containsKey(ctx.serverHandler.player.uniqueID)) return null
        val status = P2PCache.statusMap[ctx.serverHandler.player.uniqueID]!!

        val result = linkP2P(ctx.serverHandler.player, message.input, message.output, status)
        if (result != null) {
            status.listP2P[message.input] = result.first
            status.listP2P[message.output] = result.second
            GlobalScope.launch {
                ModNetwork.channel.sendTo(S2CRefreshInfo(listOf(result.first.getInfo(message.input), result.second.getInfo(message.output))), ctx.serverHandler.player)
                // It takes time before a channel is assigned to the new tunnel
                delay(2000)
                ModNetwork.channel.sendTo(S2CRefreshInfo(listOf(result.first.getInfo(message.input), result.second.getInfo(message.output))), ctx.serverHandler.player)
            }
        }
        return null
    }
}
