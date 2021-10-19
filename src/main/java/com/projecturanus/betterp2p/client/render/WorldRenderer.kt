package com.projecturanus.betterp2p.client.render

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.math.BlockPos
import org.lwjgl.opengl.GL11

/**
 * Render things in world
 * Author: Cyclic
 */
object WorldRenderer {
    fun renderBlockList(blockPosList: List<BlockPos>, center: BlockPos, relX: Double, relY: Double, relZ: Double, red: Float, green: Float, blue: Float) {
        GlStateManager.pushAttrib()
        GlStateManager.pushMatrix()
        // translate to center or te
        GlStateManager.translate(relX + 0.5F, relY + 0.5F, relZ + 0.5F)
        GlStateManager.disableLighting() // so colors are correct
        GlStateManager.disableTexture2D() // no texturing needed
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GlStateManager.enableBlend()
        val alpha = 0.5F
        GlStateManager.color(red, green, blue, alpha)
        if (Minecraft.isAmbientOcclusionEnabled())
            GlStateManager.shadeModel(GL11.GL_SMOOTH)
        else
            GlStateManager.shadeModel(GL11.GL_FLAT)
        for (p in blockPosList) {
            GlStateManager.pushMatrix()
            GlStateManager.translate(
                (center.x - p.x) * -1.0F,
                (center.y - p.y) * -1.0F,
                (center.z - p.z) * -1.0F)
            shadedCube(0.4F)
            GlStateManager.popMatrix()
        }
        GlStateManager.disableBlend()
        GlStateManager.enableTexture2D()
        GlStateManager.enableLighting()
        GlStateManager.popMatrix()
        GlStateManager.popAttrib()
    }

    fun shadedCube(scale: Float) {
        val size = 1.0 * scale
        val tessellator = Tessellator.getInstance()
        val worldRenderer = tessellator.buffer
        // Front - anticlockwise vertices
        // Back - clockwise vertices
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION)
        // xy anti-clockwise - front
        worldRenderer.pos(-size, -size, size).endVertex()
        worldRenderer.pos(size, -size, size).endVertex()
        worldRenderer.pos(size, size, size).endVertex()
        worldRenderer.pos(-size, size, size).endVertex()
        // xy clockwise - back
        worldRenderer.pos(-size, -size, -size).endVertex()
        worldRenderer.pos(-size, size, -size).endVertex()
        worldRenderer.pos(size, size, -size).endVertex()
        worldRenderer.pos(size, -size, -size).endVertex()
        // anti-clockwise - left
        worldRenderer.pos(-size, -size, -size).endVertex()
        worldRenderer.pos(-size, -size, size).endVertex()
        worldRenderer.pos(-size, size, size).endVertex()
        worldRenderer.pos(-size, size, -size).endVertex()
        // clockwise - right
        worldRenderer.pos(size, -size, -size).endVertex()
        worldRenderer.pos(size, size, -size).endVertex()
        worldRenderer.pos(size, size, size).endVertex()
        worldRenderer.pos(size, -size, size).endVertex()
        // anticlockwise - top
        worldRenderer.pos(-size, size, -size).endVertex()
        worldRenderer.pos(-size, size, size).endVertex()
        worldRenderer.pos(size, size, size).endVertex()
        worldRenderer.pos(size, size, -size).endVertex()
        // clockwise - bottom
        worldRenderer.pos(-size, -size, -size).endVertex()
        worldRenderer.pos(size, -size, -size).endVertex()
        worldRenderer.pos(size, -size, size).endVertex()
        worldRenderer.pos(-size, -size, size).endVertex()
        tessellator.draw()
    }

}
