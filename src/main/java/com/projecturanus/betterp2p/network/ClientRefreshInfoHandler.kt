package com.projecturanus.betterp2p.network

import com.projecturanus.betterp2p.client.gui.GuiAdvancedMemoryCard
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

class ClientRefreshInfoHandler : IMessageHandler<S2CRefreshInfo, IMessage?> {
    @SideOnly(Side.CLIENT)
    override fun onMessage(message: S2CRefreshInfo, ctx: MessageContext): IMessage? {
        Minecraft.getMinecraft().addScheduledTask {
            val gui = Minecraft.getMinecraft().currentScreen
            if (gui is GuiAdvancedMemoryCard) {
                gui.refreshInfo(message.infos)
            }
        }
        return null
    }
}
