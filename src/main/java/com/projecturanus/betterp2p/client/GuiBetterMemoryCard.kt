package com.projecturanus.betterp2p.client

import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.resources.I18n
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack

class GuiBetterMemoryCard : GuiScreen() {

    override fun initGui() {

        super.initGui()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawDefaultBackground()
        drawCenteredString(fontRenderer, I18n.format("sign.edit"), width / 2, 40, 16777215)
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
        GlStateManager.pushMatrix()
        GlStateManager.translate((width / 2).toFloat(), 0.0f, 50.0f)
        val f = 93.75f
        GlStateManager.scale(-93.75f, -93.75f, -93.75f)
        GlStateManager.rotate(180.0f, 0.0f, 1.0f, 0.0f)
        GlStateManager.popMatrix()

        drawItemStack(ItemStack(Blocks.STONE), 20, 20)
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    private fun drawItemStack(stack: ItemStack, x: Int, y: Int, altText: String? = null) {
        GlStateManager.translate(0.0f, 0.0f, 32.0f)
        zLevel = 200.0f
        itemRender.zLevel = 200.0f
        var font = stack.item.getFontRenderer(stack)
        if (font == null) font = fontRenderer
        itemRender.renderItemAndEffectIntoGUI(stack, x, y)
        itemRender.renderItemOverlayIntoGUI(font, stack, x, y, altText)
        zLevel = 0.0f
        itemRender.zLevel = 0.0f
    }
}
