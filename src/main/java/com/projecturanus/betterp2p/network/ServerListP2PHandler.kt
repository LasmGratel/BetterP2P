package com.projecturanus.betterp2p.network

import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext

class ServerListP2PHandler : IMessageHandler<AEPartLocationMessage, S2CListP2P?> {
    override fun onMessage(message: AEPartLocationMessage, ctx: MessageContext): S2CListP2P? {
        
        return null
    }
}
