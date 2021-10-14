package com.projecturanus.betterp2p.network

import com.projecturanus.betterp2p.MODID
import net.minecraftforge.fml.common.network.NetworkRegistry
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper
import net.minecraftforge.fml.relauncher.Side

object ModNetwork {
    val channel: SimpleNetworkWrapper = NetworkRegistry.INSTANCE.newSimpleChannel(MODID)

    fun registerNetwork() {
        channel.registerMessage(ClientOpenGuiHandler::class.java, S2CListP2P::class.java, 0, Side.CLIENT)
        channel.registerMessage(ClientRefreshInfoHandler::class.java, S2CRefreshInfo::class.java, 1, Side.CLIENT)

        channel.registerMessage(ServerUpdateInfoHandler::class.java, C2SUpdateInfo::class.java, 2, Side.SERVER)
    }
}
