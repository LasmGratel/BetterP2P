package com.projecturanus.betterp2p.client

import com.projecturanus.betterp2p.MODID
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import org.lwjgl.input.Mouse

class GuiBetterMemoryCard : GuiScreen(), TextureBound {
    private val xSize = 238
    private val ySize = 206
    private val guiLeft: Int by lazy { (width - this.xSize) / 2 }
    private val guiTop: Int by lazy { (height - this.ySize) / 2 }

    private val tableX = 9
    private val tableY = 19

    private lateinit var scrollBar: WidgetScrollBar

    override fun initGui() {
        super.initGui()
        scrollBar = WidgetScrollBar()
        scrollBar.displayX = guiLeft + 218
        scrollBar.displayY = guiTop + 19
        scrollBar.height = 114
        scrollBar.setRange(0, 10, 23)
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawDefaultBackground()
        drawBackground()
        scrollBar.draw(this)
        drawItemStack(ItemStack(Blocks.STONE), guiLeft + tableX + 4, guiTop + tableY + 3)
        fontRenderer.drawString("Test", guiLeft + 33, guiTop + 22, 16777215)

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        scrollBar.click(mouseX, mouseY)
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun mouseClickMove(mouseX: Int, mouseY: Int, clickedMouseButton: Int, timeSinceLastClick: Long) {
        scrollBar.click(mouseX, mouseY)
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick)
    }

    override fun handleMouseInput() {
        super.handleMouseInput()
        val i = Mouse.getEventDWheel()
        if (i != 0 && isShiftKeyDown()) {
            val x = Mouse.getEventX() * width / mc.displayWidth
            val y = height - Mouse.getEventY() * height / mc.displayHeight - 1
//            this.mouseWheelEvent(x, y, i / Math.abs(i))
        } else if (i != 0) {
            scrollBar.wheel(i)
        }
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

    override fun bindTexture(modid: String, location: String) {
        val loc = ResourceLocation(modid, location)
        mc.textureManager.bindTexture(loc)
    }

    private fun drawBackground() {
        bindTexture(MODID, "textures/gui/better_memory_card.png")
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, this.xSize, this.ySize)
    }

    override fun doesGuiPauseGame(): Boolean {
        return false
    }
}
