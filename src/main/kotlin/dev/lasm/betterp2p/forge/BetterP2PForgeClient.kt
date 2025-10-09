package dev.lasm.betterp2p.forge

import com.mojang.blaze3d.vertex.PoseStack
import dev.lasm.betterp2p.BetterP2P
import dev.lasm.betterp2p.client.RenderBlockOutline
import net.minecraft.client.Camera
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.CreativeModeTabs
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.client.event.RenderLevelStageEvent
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent


@Mod(BetterP2P.MOD_ID, dist = [Dist.CLIENT])
object BetterP2PForgeClient {
    @JvmStatic
    fun init() {
        BetterP2P.initClient()
        NeoForge.EVENT_BUS.addListener { context: RenderLevelStageEvent ->
            if (context.stage != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES)
                return@addListener
            val level: ClientLevel? = Minecraft.getInstance().level
            val poseStack: PoseStack = context.poseStack
            val buffers: MultiBufferSource? = Minecraft.getInstance().renderBuffers().bufferSource()
            val camera: Camera = context.camera
            if (level == null || buffers == null) {
                return@addListener
            } else {
                RenderBlockOutline.showPartPlacementPreview(
                    Minecraft.getInstance().player,
                    poseStack,
                    buffers,
                    camera
                )
            }
        }
    }

    @SubscribeEvent
    @JvmStatic
    fun buildContents(event: BuildCreativeModeTabContentsEvent) {
        if (event.tabKey == ResourceKey.create(Registries.CREATIVE_MODE_TAB, ResourceLocation.tryBuild("ae2", "main")!!))
            event.accept(BetterP2P.ADVANCED_MEMORY_CARD_ITEM.get())
    }
}
