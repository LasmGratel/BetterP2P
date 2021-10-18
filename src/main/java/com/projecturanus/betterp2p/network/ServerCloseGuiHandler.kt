package com.projecturanus.betterp2p.network

import com.projecturanus.betterp2p.util.p2p.P2PCache
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext

class ServerCloseGuiHandler : IMessageHandler<C2SCloseGui, IMessage?> {
    override fun onMessage(message: C2SCloseGui, ctx: MessageContext): IMessage? {
        P2PCache.statusMap.remove(ctx.serverHandler.player.uniqueID)

        return null
    }
}
