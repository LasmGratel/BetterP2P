package com.projecturanus.betterp2p.network

import com.projecturanus.betterp2p.MODID
import net.minecraftforge.fml.common.network.NetworkRegistry
import net.minecraftforge.fml.relauncher.Side

object ModNetwork {
    val channal = NetworkRegistry.INSTANCE.newSimpleChannel(MODID)

    fun registerNetwork() {
        channal.registerMessage(ClientOpenGuiHandler::class.java, S2COpenGui::class.java, 0, Side.CLIENT)
    }
}
