package com.projecturanus.betterp2p.network

import com.projecturanus.betterp2p.item.ItemAdvancedMemoryCard
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext

class ServerUpdateInfoHandler : IMessageHandler<C2SUpdateInfo, IMessage?> {
    override fun onMessage(message: C2SUpdateInfo, ctx: MessageContext): IMessage? {
        val player = ctx.serverHandler.player
        val stack = player.getHeldItem(player.activeHand)
        if (stack.item is ItemAdvancedMemoryCard) {
            ItemAdvancedMemoryCard.writeInfo(stack, message.info)
        }
        return null
    }
}
