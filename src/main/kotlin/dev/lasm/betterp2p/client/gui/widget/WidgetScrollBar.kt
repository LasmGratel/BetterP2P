package dev.lasm.betterp2p.client.gui.widget

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

class WidgetScrollBar(x: Int, y: Int) : AbstractWidget(x, y, 12, 15, Component.empty()) {
    var pageSize = 1

    var maxScroll = 0
    var minScroll = 0

    var onScroll: () -> Unit = {}

    var currentScroll = 0

    val CREATIVE_TAB_GUI =
        ResourceLocation("minecraft", "textures/gui/container/creative_inventory/tabs.png")

    override fun renderWidget(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        if (getRange() == 0) {
            graphics.blit(CREATIVE_TAB_GUI, x, y, 244, 0, 12, 15)
        } else {
            val offset = (currentScroll - minScroll) * (height - 15) / getRange()
            graphics.blit(CREATIVE_TAB_GUI, x, offset + y, 232, 0, 12, 15)
        }
    }

    private fun getRange(): Int {
        return maxScroll - minScroll
    }

    fun setRange(min: Int, max: Int, pageSize: Int) {
        minScroll = min
        maxScroll = max
        this.pageSize = pageSize
        if (minScroll > maxScroll) {
            maxScroll = minScroll
        }
        applyRange()
    }

    private fun applyRange() {
        currentScroll = currentScroll.coerceIn(minScroll, maxScroll)
        onScroll()
    }

    override fun onDrag(mouseX: Double, mouseY: Double, dragX: Double, dragY: Double) {
        if (getRange() == 0) {
            return
        }
        currentScroll = (mouseY - y).toInt()
        currentScroll = minScroll + currentScroll * 2 * getRange() / height
        currentScroll = currentScroll + 1 shr 1
        applyRange()
        super.onDrag(mouseX, mouseY, dragX, dragY)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, delta: Double): Boolean {
        var delta = delta.toInt()
        delta = (-delta).coerceIn(-1, 1)
        currentScroll += delta
        applyRange()
        return true
    }

    override fun updateWidgetNarration(narrationElementOutput: NarrationElementOutput) {}

    fun setHeight(i: Int) {
        height = i
    }
}
