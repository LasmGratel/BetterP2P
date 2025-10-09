package dev.lasm.betterp2p.client.gui.widget

import dev.lasm.betterp2p.client.gui.drawBlockIcon
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

class WidgetTypeIcon(
    x: Int,
    y: Int,
    tooltipLiteral: String,
    val iconSupplier: () -> ResourceLocation
) : AbstractWidget(x, y, 18, 18, Component.empty()) {
    init {
        tooltip = Tooltip.create(Component.literal(tooltipLiteral))
    }

    override fun renderWidget(graphics: GuiGraphics, i: Int, j: Int, f: Float) {
        if (isHovered) {
            graphics.fill(x, y, x + width, y + height, 0xFF00FF00.toInt())
        }
        drawBlockIcon(graphics, iconSupplier(), x = x + 1, y = y + 1)
    }

    override fun updateWidgetNarration(arg: NarrationElementOutput) {}
}
