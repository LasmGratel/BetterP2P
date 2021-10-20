package com.projecturanus.betterp2p.client.gui.widget

import net.minecraft.client.gui.Gui

open class Widget {
    fun drawHorizontalLine(startX: Int, endX: Int, y: Int, color: Int) {
        var startX = startX
        var endX = endX
        if (endX < startX) {
            val i = startX
            startX = endX
            endX = i
        }
        Gui.drawRect(startX, y, endX + 1, y + 1, color)
    }

    /**
     * Draw a 1 pixel wide vertical line. Args : x, y1, y2, color
     */
    fun drawVerticalLine(x: Int, startY: Int, endY: Int, color: Int) {
        var startY = startY
        var endY = endY
        if (endY < startY) {
            val i = startY
            startY = endY
            endY = i
        }
        Gui.drawRect(x, startY + 1, x + 1, endY, color)
    }

    fun drawRectBorder(left: Int, top: Int, width: Int, height: Int, stroke: Int) {
        drawHorizontalLine(left, left + width, top, stroke)
        drawHorizontalLine(left, left + width, top + height, stroke)
        drawVerticalLine(left, top, top + height, stroke)
        drawVerticalLine(left + width, top, top + height, stroke)
    }
}
