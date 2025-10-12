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

    val SCROLLER =
        ResourceLocation.withDefaultNamespace("container/creative_inventory/scroller")
    val SCROLLER_DISABLED =
        ResourceLocation.withDefaultNamespace("container/creative_inventory/scroller_disabled")

    override fun renderWidget(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        if (getRange() == 0) {
            graphics.blitSprite(SCROLLER_DISABLED, x, y, 12, 15)
        } else {
            val offset = (currentScroll - minScroll) * (height - 15) / getRange()
            graphics.blitSprite(SCROLLER, x, offset + y, 12, 15)
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

    override fun mouseScrolled(
        mouseX: Double,
        mouseY: Double,
        scrollX: Double,
        scrollY: Double
    ): Boolean {
        var delta = scrollY.toInt()
        delta = (-delta).coerceIn(-1, 1)
        currentScroll += delta
        applyRange()
        return true
    }

    override fun updateWidgetNarration(narrationElementOutput: NarrationElementOutput) {}
}
