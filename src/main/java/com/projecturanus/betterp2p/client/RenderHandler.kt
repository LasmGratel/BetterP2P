package com.projecturanus.betterp2p.client

import com.projecturanus.betterp2p.MODID
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.relauncher.Side

@Mod.EventBusSubscriber(modid = MODID, value = [Side.CLIENT])
object RenderHandler {
    @JvmStatic
    @SubscribeEvent
    fun renderOverlays(event: RenderWorldLastEvent) {

    }
}
