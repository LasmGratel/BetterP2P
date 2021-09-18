package com.projecturanus.betterp2p.network

import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext

class ClientListP2PHandler : IMessageHandler<S2CListP2P, IMessage?> {
    override fun onMessage(message: S2CListP2P, ctx: MessageContext): IMessage? {

        return null
    }
}
