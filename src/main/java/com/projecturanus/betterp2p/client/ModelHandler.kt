package com.projecturanus.betterp2p.client

import com.projecturanus.betterp2p.MODID
import com.projecturanus.betterp2p.item.ItemAdvancedMemoryCard
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.ModelRegistryEvent
import net.minecraftforge.client.model.ModelLoader
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.relauncher.Side

@Mod.EventBusSubscriber(modid = MODID, value = [Side.CLIENT])
object ModelHandler {
    @JvmStatic
    @SubscribeEvent
    fun registerModels(event: ModelRegistryEvent) {
        ModelLoader.setCustomModelResourceLocation(ItemAdvancedMemoryCard, 0, ModelResourceLocation(ResourceLocation(MODID, "advanced_memory_card"), "inventory"))
    }
}
