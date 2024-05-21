package dev.lasm.betterp2p.client.gui.widget

import dev.lasm.betterp2p.client.gui.GuiAdvancedMemoryCard
import dev.lasm.betterp2p.client.gui.InfoList
import dev.lasm.betterp2p.client.gui.InfoWrapper
import dev.lasm.betterp2p.item.BetterMemoryCardModes
import kotlin.reflect.KProperty0
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.ObjectSelectionList
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList
import net.minecraft.network.chat.Component

class WidgetP2PList(
    val gui: GuiAdvancedMemoryCard,
    private val infos: InfoList,
    private var x: Int,
    private var y: Int,
    private val selectedInfo: KProperty0<InfoWrapper?>,
    val mode: () -> BetterMemoryCardModes
) :
    ObjectSelectionList<WidgetP2PList.Companion.Entry>(
        Minecraft.getInstance(),
        254,
        0,
        x,
        y,
        P2PEntryConstants.HEIGHT + 1
    ) {
    companion object {
        class Entry(val widget: WidgetP2PDevice) : ObjectSelectionList.Entry<Entry>() {
            override fun render(
                guiGraphics: GuiGraphics,
                index: Int,
                y: Int,
                x: Int,
                width: Int,
                height: Int,
                mouseX: Int,
                mouseY: Int,
                bl: Boolean,
                partialFloat: Float
            ) {
                widget.setPosition(x, y)
                widget.render(guiGraphics, mouseX, mouseY, partialFloat)
            }

            override fun getNarration(): Component {
                return Component.empty()
            }
        }
    }

    init {
        setRenderBackground(false)
        setRenderTopAndBottom(false)
        setRenderHeader(false, 0)
    }

    fun resize(scale: GuiScale, availableHeight: Int) {
        clearEntries()
        for (i in 0 until scale.size(availableHeight)) {
            val widget =
                Entry(
                    WidgetP2PDevice(
                        i,
                        selectedInfo,
                        mode,
                        { infos.filtered.getOrNull(i) },
                        gui.col,
                        x,
                        y + i * (P2PEntryConstants.HEIGHT + 1)
                    )
                )
            addEntry(widget)
        }
    }

    override fun keyPressed(i: Int, j: Int, k: Int): Boolean {
        val entry = selected as ServerSelectionList.Entry?
        return entry != null && entry.keyPressed(i, j, k) || super.keyPressed(i, j, k)
    }
}
