package com.projecturanus.betterp2p.client

import appeng.util.Platform
import com.projecturanus.betterp2p.network.P2PInfo
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.resources.I18n
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos

class InfoWrapper(info: P2PInfo) {
    // Basic information
    val index: Int = info.index
    val frequency: Short = info.frequency
    val hasChannel = info.hasChannel
    val pos: BlockPos = info.pos
    val facing: EnumFacing = info.facing
    val description: String
    val output: Boolean = info.output

    // Widgets
    val selectButton = GuiButton(0, 0, 0, 34, 20, I18n.format("gui.better_memory_card.select"))
    val bindButton = GuiButton(0, 0, 0, 34, 20, I18n.format("gui.better_memory_card.bind"))

    init {
        description = buildString {
            append("P2P ")
            if (output)
                append(I18n.format("gui.better_memory_card.desc.mode.output"))
            else
                append(I18n.format("gui.better_memory_card.desc.mode.input"))
            append(" - ")
            if (info.frequency.toInt() == 0)
                append(I18n.format("gui.better_memory_card.desc.not_set"))
            else
                append(Platform.p2p().toHexString(info.frequency))
        }
    }
}
