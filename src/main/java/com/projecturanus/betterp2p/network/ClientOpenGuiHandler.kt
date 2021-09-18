package com.projecturanus.betterp2p.network

import com.projecturanus.betterp2p.client.GuiBetterMemoryCard
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

class ClientOpenGuiHandler : IMessageHandler<AEPartLocationMessage, IMessage?> {
    @SideOnly(Side.CLIENT)
    override fun onMessage(message: AEPartLocationMessage, ctx: MessageContext): IMessage? {
        Minecraft.getMinecraft().addScheduledTask {
            Minecraft.getMinecraft().displayGuiScreen(GuiBetterMemoryCard(message))
        }
        return null
    }
}
