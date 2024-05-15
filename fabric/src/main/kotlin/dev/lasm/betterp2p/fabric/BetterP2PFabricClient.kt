package dev.lasm.betterp2p.fabric

import com.mojang.blaze3d.vertex.PoseStack
import dev.lasm.betterp2p.BetterP2P
import dev.lasm.betterp2p.client.RenderBlockOutline
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.minecraft.client.Camera
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.renderer.MultiBufferSource

object BetterP2PFabricClient : ClientModInitializer {
    override fun onInitializeClient() {
        BetterP2P.initClient()
        WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register { context, hitResult ->
            val level: ClientLevel? = context.world()
            val poseStack: PoseStack = context.matrixStack()
            val buffers: MultiBufferSource ?= context.consumers()
            val camera: Camera = context.camera()
            if (level == null || buffers == null) {
                true
            } else {
                RenderBlockOutline.showPartPlacementPreview(Minecraft.getInstance().player, poseStack, buffers, camera)
                false
            }
        }
    }
}
