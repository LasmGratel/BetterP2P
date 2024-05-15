package dev.lasm.betterp2p.client.gui.widget

import dev.lasm.betterp2p.client.gui.GuiAdvancedMemoryCard
import dev.lasm.betterp2p.client.gui.drawBlockIcon
import dev.lasm.betterp2p.util.p2p.ClientTunnelInfo
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.resources.language.I18n
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import kotlin.math.min

const val ICONS_PER_ROW = 5

/**
 * Select a type.
 */
class WidgetTypeSelector(x: Int, y: Int,
                         val gui: GuiAdvancedMemoryCard,
                         val p2pTypes: List<ClientTunnelInfo>):
    AbstractWidget(
        x, y,
        min(ICONS_PER_ROW, p2pTypes.size) * 18 + 8,
        (p2pTypes.size + ICONS_PER_ROW - 1) * 18 / ICONS_PER_ROW + 8,
        Component.empty()) {
    var hoveredIdx: Int = 0
    var useAny = false
    /**
     * Feeds the input into this parent.
     */
    var parent: ITypeReceiver? = null
    private val translated: List<List<String>>

    override fun setFocused(focused: Boolean) {
        super.setFocused(focused)
        if (!focused) visible = false
    }

    init {
        val list = p2pTypes.map { listOf(it.dispName) }.toMutableList()
        list.add(listOf(I18n.get("gui.advanced_memory_card.types.any")))
        translated = list
    }

    override fun onClick(mouseX: Double, mouseY: Double) {
        if (hoveredIdx != -1)
            parent?.accept(p2pTypes.getOrNull(hoveredIdx))
        super.onClick(mouseX, mouseY)
    }

    override fun renderWidget(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialFloat: Float) {
        // Background
        graphics.fill(x, y, x + width, y + height, 0xAA000000.toInt())

        hoveredIdx = -1
        for ((i, type) in p2pTypes.withIndex()) {
            val iconPosX = x + 4 + (i % ICONS_PER_ROW) * 18
            val iconPosY = y + 4 + (i / ICONS_PER_ROW) * 18
            val iconHover = mouseX > iconPosX && mouseX < iconPosX + 18 && mouseY > iconPosY && mouseY < iconPosY + 18
            if (iconHover) {
                hoveredIdx = i
                graphics.fill(iconPosX, iconPosY, iconPosX + 18, iconPosY + 18, 0xFF00FF00.toInt())
            }
            drawBlockIcon(graphics, type.icon(),
                x = iconPosX + 1,
                y = iconPosY + 1)
        }
        if (useAny) {
            val iconPosX = x + 4 + (p2pTypes.size % ICONS_PER_ROW) * 18
            val iconPosY = y + 4 + (p2pTypes.size / ICONS_PER_ROW) * 18
            val iconHover = mouseX > iconPosX && mouseX < iconPosX + 18 && mouseY > iconPosY && mouseY < iconPosY + 18
            if (iconHover) {
                hoveredIdx = p2pTypes.size
                graphics.fill(iconPosX, iconPosY, iconPosX + 18, iconPosY + 18, 0xFF00FF00.toInt())
            }
            drawBlockIcon(graphics, ResourceLocation("minecraft", "textures/block/coal_block.png"),
                x = iconPosX + 1,
                y = iconPosY + 1)
            graphics.drawString(Minecraft.getInstance().font, "?", iconPosX + 6, iconPosY + 6, 0xFFFF0000.toInt(), false)
        }
        if (hoveredIdx != -1) {
            gui.drawTooltip(graphics, mouseX, mouseY, translated[hoveredIdx].map { Component.literal(it) })
        }
    }

    override fun updateWidgetNarration(narrationElementOutput: NarrationElementOutput) {

    }
}

interface ITypeReceiver: LayoutElement {

    fun accept(type: ClientTunnelInfo?)
}
