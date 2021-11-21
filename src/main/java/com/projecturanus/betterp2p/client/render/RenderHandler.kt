package com.projecturanus.betterp2p.client.render

import com.projecturanus.betterp2p.MODID
import com.projecturanus.betterp2p.client.ClientCache
import com.projecturanus.betterp2p.item.ItemAdvancedMemoryCard
import net.minecraft.client.Minecraft
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.relauncher.Side

@Mod.EventBusSubscriber(modid = MODID, value = [Side.CLIENT])
object RenderHandler {
    @JvmStatic
    @SubscribeEvent
    fun renderOverlays(event: RenderWorldLastEvent) {
        val player = Minecraft.getMinecraft().player
        if (player.heldItemMainhand.item == ItemAdvancedMemoryCard) {
            if (ClientCache.positions.isNotEmpty() || ClientCache.selectedPosition != null) {
                if (ClientCache.selectedPosition != null) {
                    OutlineRenderer.renderOutlinesWithFacing(
                        event,
                        player,
                        listOf(ClientCache.selectedPosition to ClientCache.selectedFacing),
                        0x45,
                        0xDA,
                        0x75
                    )
                }
                OutlineRenderer.renderOutlinesWithFacing(event, player, ClientCache.positions, 0x66, 0xCC, 0xFF)
            }
        }
    }
}
