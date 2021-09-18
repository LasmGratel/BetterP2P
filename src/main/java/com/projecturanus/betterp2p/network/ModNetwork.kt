package com.projecturanus.betterp2p.network

import com.projecturanus.betterp2p.MODID
import net.minecraftforge.fml.common.network.NetworkRegistry
import net.minecraftforge.fml.relauncher.Side

object ModNetwork {
    val channal = NetworkRegistry.INSTANCE.newSimpleChannel(MODID)

    fun registerNetwork() {
        channal.registerMessage(ClientOpenGuiHandler::class.java, AEPartLocationMessage::class.java, 0, Side.CLIENT)
        channal.registerMessage(ServerListP2PHandler::class.java, AEPartLocationMessage::class.java, 1, Side.SERVER)
    }
}
