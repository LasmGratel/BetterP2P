package com.projecturanus.betterp2p.client

import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager

class WidgetScrollBar {
    var displayX = 0
    var displayY = 0
    var width = 12
    var height = 16
    var pageSize = 1

    var maxScroll = 0
    var minScroll = 0

    var onScroll: () -> Unit = {}

    var currentScroll = 0

    fun <T> draw(g: T) where T: TextureBound, T: Gui {
        g.bindTexture("minecraft", "textures/gui/container/creative_inventory/tabs.png")
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
        if (getRange() == 0) {
            g.drawTexturedModalRect(displayX, displayY, 232 + width, 0, width, 15)
        } else {
            val offset = (currentScroll - minScroll) * (height - 15) / getRange()
            g.drawTexturedModalRect(displayX, offset + displayY, 232, 0, width, 15)
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
        currentScroll = currentScroll.coerceAtMost(maxScroll).coerceAtLeast(minScroll)
        onScroll()
    }

    fun click(x: Int, y: Int) {
        if (getRange() == 0) {
            return
        }
        if (x > displayX && x <= displayX + width) {
            if (y > displayY && y <= displayY + height) {
                currentScroll = y - displayY
                currentScroll = minScroll + currentScroll * 2 * getRange() / height
                currentScroll = currentScroll + 1 shr 1
                applyRange()
            }
        }
    }

    fun wheel(delta: Int) {
        var delta = delta
        delta = (-delta).coerceAtMost(1).coerceAtLeast(-1)
        currentScroll += delta
        applyRange()
    }
}
