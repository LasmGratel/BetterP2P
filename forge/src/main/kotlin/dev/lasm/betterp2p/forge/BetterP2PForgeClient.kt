package dev.lasm.betterp2p.forge

import com.mojang.blaze3d.vertex.PoseStack
import dev.lasm.betterp2p.BetterP2P
import dev.lasm.betterp2p.client.RenderBlockOutline
import net.minecraft.client.Camera
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import net.minecraftforge.client.event.RenderHighlightEvent
import net.minecraftforge.common.MinecraftForge

@OnlyIn(Dist.CLIENT)
object BetterP2PForgeClient {
    @JvmStatic
    fun init() {
        BetterP2P.initClient()
        MinecraftForge.EVENT_BUS.addListener { context: RenderHighlightEvent.Block ->
            val level: ClientLevel? = Minecraft.getInstance().level
            val poseStack: PoseStack = context.poseStack
            val buffers: MultiBufferSource? = context.multiBufferSource
            val camera: Camera = context.camera
            if (level == null || buffers == null) {
                return@addListener
            } else {
                RenderBlockOutline.showPartPlacementPreview(Minecraft.getInstance().player, poseStack, buffers, camera)
                context.isCanceled = true
            }
        }
    }
}
