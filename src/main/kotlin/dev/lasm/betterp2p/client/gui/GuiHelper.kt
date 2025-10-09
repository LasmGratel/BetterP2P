package dev.lasm.betterp2p.client.gui

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.BufferUploader
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.resources.ResourceLocation
import org.joml.Matrix4f

fun GuiGraphics.drawTexturedQuad(
    texture: ResourceLocation,
    x0: Float,
    y0: Float,
    x1: Float,
    y1: Float,
    u0: Float,
    v0: Float,
    u1: Float,
    v1: Float
) {
    RenderSystem.setShader { GameRenderer.getPositionTexShader() }
    RenderSystem.setShaderTexture(0, texture)
    val matrix4f: Matrix4f = this.pose().last().pose()
    val bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX)
    bufferBuilder.addVertex(matrix4f, x0, y1, 0.0f).setUv(u0, v1)
    bufferBuilder.addVertex(matrix4f, x1, y1, 0.0f).setUv(u1, v1)
    bufferBuilder.addVertex(matrix4f, x1, y0, 0.0f).setUv(u1, v0)
    bufferBuilder.addVertex(matrix4f, x0, y0, 0.0f).setUv(u0, v0)
    BufferUploader.drawWithShader(bufferBuilder.build()!!)
}

fun GuiGraphics.drawIcon(srcX: Int, srcY: Int, x: Int, y: Int, width: Int = 16, height: Int = 16) {
    blit(TEXTURE, x, y, 0, srcX.toFloat(), srcY.toFloat(), width, height, 288, 264)
}

fun AbstractWidget.isClicked(mouseX: Double, mouseY: Double) =
    this.isActive &&
        this.visible &&
        (mouseX >= x.toDouble()) &&
        (mouseY >= y.toDouble()) &&
        (mouseX < (this.x + this.width).toDouble()) &&
        (mouseY < (this.y + this.height).toDouble())

fun drawBlockIcon(
    graphics: GuiGraphics,
    icon: ResourceLocation,
    overlay: ResourceLocation = ResourceLocation.tryBuild("ae2", "textures/part/p2p_tunnel_front.png")!!,
    x: Int,
    y: Int,
    width: Int = 16,
    height: Int = 16
) {
    graphics.drawTexturedQuad(
        icon,
        x0 = x.toFloat() + 2,
        y0 = y.toFloat() + 2,
        x1 = x.toFloat() + width - 2,
        y1 = y.toFloat() + height - 2,
        u0 = 0.0f,
        v0 = 0.0f,
        u1 = 1.0f,
        v1 = 1.0f
    )

    graphics.drawTexturedQuad(
        overlay,
        x0 = x.toFloat(),
        y0 = y.toFloat(),
        x1 = x.toFloat() + width,
        y1 = y.toFloat() + height,
        u0 = 0.0f,
        v0 = 0.0f,
        u1 = 1.0f,
        v1 = 1.0f
    )
}
