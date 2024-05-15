package dev.lasm.betterp2p.client.gui.widget

import appeng.client.gui.widgets.ITooltip
import com.mojang.blaze3d.systems.RenderSystem
import dev.lasm.betterp2p.client.gui.GUI_TEX_HEIGHT
import dev.lasm.betterp2p.client.gui.GUI_WIDTH
import dev.lasm.betterp2p.client.gui.TEXTURE
import dev.lasm.betterp2p.client.gui.drawTexturedQuad
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.renderer.Rect2i
import net.minecraft.network.chat.Component

open class IconButton(var texX: Int, var texY: Int, onPress: OnPress) : Button(0, 0, 32, 32,
    Component.empty(),
    onPress,
    DEFAULT_NARRATION), ITooltip {

    var messages = mutableListOf(message)

    public override fun renderWidget(
        guiGraphics: GuiGraphics,
        mouseX: Int,
        mouseY: Int,
        partial: Float
    ) {
        RenderSystem.enableBlend()
        RenderSystem.enableDepthTest()
        renderBackground(guiGraphics, mouseY, mouseY, partial)

        guiGraphics.drawTexturedQuad(TEXTURE,
            x0 = x + 1.0f,
            y0 = y + 1.0f,
            x1 = x + width - 1.0f,
            y1 = y + height - 1.0f,
            u0 = texX / GUI_WIDTH.toFloat(),
            v0 = texY / GUI_TEX_HEIGHT.toFloat(),
            u1 = (texX + width) / GUI_WIDTH.toFloat(),
            v1 = (texY + height) / GUI_TEX_HEIGHT.toFloat())
    }

    fun getHoverState(): Int {
        var i = 1
        if (!this.active) {
            i = 0
        } else if (this.isHovered) {
            i = 2
        }
        return i
    }

    fun renderBackground(
        graphics: GuiGraphics,
        mouseX: Int,
        mouseY: Int,
        partial: Float) {
        val k = getHoverState()
        graphics.drawTexturedQuad(
            TEXTURE, x.toFloat(), y.toFloat(),
            (x + width).toFloat(), (y + height).toFloat(),
            u0 = (32.0f * k) / GUI_WIDTH, v0 = (232.0f) / GUI_TEX_HEIGHT,
            u1 = (32.0f * (k + 1)) / GUI_WIDTH, v1 = (232.0f + height) / GUI_TEX_HEIGHT
        )
    }

    override fun getTooltipMessage(): List<Component> {
        return messages
    }

    override fun getTooltipArea(): Rect2i {
        return Rect2i(
            x,
            y,
            width,
            height
        )
    }

    override fun isTooltipAreaVisible(): Boolean {
        return this.visible
    }

}
