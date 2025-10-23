package dev.lasm.betterp2p.client.gui.widget

import dev.lasm.betterp2p.BetterP2P
import dev.lasm.betterp2p.client.gui.GuiAdvancedMemoryCard
import dev.lasm.betterp2p.client.gui.InfoList
import dev.lasm.betterp2p.client.gui.InfoWrapper
import dev.lasm.betterp2p.item.BetterMemoryCardModes
import dev.lasm.betterp2p.network.packet.C2SLinkP2P
import dev.lasm.betterp2p.network.packet.C2SRenameP2P
import dev.lasm.betterp2p.network.packet.C2SUnlinkP2P
import java.util.function.Consumer
import kotlin.reflect.KProperty0
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.network.chat.Component
import net.neoforged.neoforge.network.PacketDistributor

/**
 * WidgetP2PColumn
 *
 * A widget that contains a list of P2P entries.
 */
class WidgetP2PColumn(
    val gui: GuiAdvancedMemoryCard,
    private val infos: InfoList,
    x: Int,
    y: Int,
    private val selectedInfo: KProperty0<InfoWrapper?>,
    val mode: () -> BetterMemoryCardModes,
    private var scrollBar: WidgetScrollBar
) : AbstractWidget(x, y, 160, 0, Component.empty()) {

    val entries: MutableList<WidgetP2PDevice> = mutableListOf()

    private val renameBar by lazy {
        object : EditBox(font, 0, 0, 160, 12, Component.empty()) {
            var info: InfoWrapper? = null
        }
    }

    fun getRenameBar() = renameBar

    val font: Font
        get() = gui.getFont()

    init {
        renameBar.setMaxLength(50)
        renameBar.visible = false
        renameBar.setCanLoseFocus(true)
    }

    /** Resize the column */
    fun resize(scale: GuiScale, availableHeight: Int) {
        entries.clear()
        for (i in 0 until scale.size(availableHeight)) {
            val widget =
                WidgetP2PDevice(
                    i,
                    selectedInfo,
                    mode,
                    { infos.filtered.getOrNull(i + scrollBar.currentScroll) },
                    this,
                    x,
                    y + i * (P2PEntryConstants.HEIGHT + 1)
                )
            entries.add(widget)
        }
    }

    override fun setPosition(x: Int, y: Int) {
        super.setPosition(x, y)
        for ((i, entry) in entries.withIndex()) {
            entry.x = x
            entry.y = y + i * (P2PEntryConstants.HEIGHT + 1)
        }
    }

    override fun renderWidget(guiGraphics: GuiGraphics, i: Int, j: Int, f: Float) {}

    override fun updateWidgetNarration(narrationElementOutput: NarrationElementOutput) {}

    fun finishRename() {
        if (!renameBar.visible) return
        for (widget in entries) {
            widget.renderNameTextfield = true
        }
        if (
            renameBar.info != null &&
                renameBar.value.isNotEmpty() &&
                renameBar.info!!.name != renameBar.value
        ) {
            val info: InfoWrapper = renameBar.info!!

            renameBar.value = renameBar.value.trim()
            PacketDistributor.sendToServer(C2SRenameP2P(info.loc, renameBar.value))
        }
        renameBar.visible = false
        renameBar.value = ""
        renameBar.isFocused = false
        renameBar.info = null
        gui.focused = this
    }

    /**
     * Called when rename button "area" is clicked. Rename text bar must be visible after this is
     * called
     */
    fun onRenameButtonClicked(info: InfoWrapper, index: Int) {
        renameBar.visible = true
        renameBar.y = (this.y) + index * (P2PEntryConstants.HEIGHT + 1) + 1
        renameBar.x = this.x + 50
        renameBar.value = info.name
        renameBar.isFocused = true
        renameBar.cursorPosition = 0
        renameBar.info = info
        gui.focused = renameBar
    }

    override fun onClick(mouseX: Double, mouseY: Double, button: Int) {
        val clickRenameButton = false
        if (!clickRenameButton && renameBar.visible) {
            finishRename()
        }
        super.onClick(mouseX, mouseY, button)
    }

    fun onBindButtonClicked(button: Button, info: InfoWrapper) {
        button.active = false
        if (infos.selectedEntry == null) return
        when (mode()) {
            BetterMemoryCardModes.INPUT -> {
                BetterP2P.logger.debug("Bind {} as input", info.loc)
                PacketDistributor.sendToServer(C2SLinkP2P(info.loc, infos.selectedEntry!!))
            }
            BetterMemoryCardModes.OUTPUT -> {
                BetterP2P.logger.debug("Bind {} as output", info.loc)
                PacketDistributor.sendToServer(C2SLinkP2P(infos.selectedEntry!!, info.loc))
            }
            BetterMemoryCardModes.COPY -> {
                val input = findInput(infos.selectedInfo?.frequency)
                if (input != null) PacketDistributor.sendToServer(C2SLinkP2P(input.loc, info.loc))
            }
            else -> {
                BetterP2P.logger.debug("Somehow bind button was pressed while in UNBIND mode.")
            }
        }
        gui.onRefresh(null)
    }

    fun onUnbindButtonClicked(button: Button, info: InfoWrapper) {
        button.active = false
        if (info.frequency != 0.toShort()) {
            PacketDistributor.sendToServer(C2SUnlinkP2P(info.loc, gui.getTypeID()))
            info.frequency = 0.toShort()
        }
        gui.onRefresh(null)
    }

    fun findInput(frequency: Short?) =
        infos.filtered.find { it.frequency == frequency && !it.output }

    fun findOutput(frequency: Short?) =
        infos.filtered.find { it.frequency == frequency && it.output }

    override fun visitWidgets(consumer: Consumer<AbstractWidget>) {
        super.visitWidgets(consumer)
        entries.forEach { it.visitWidgets(consumer) }
        consumer.accept(renameBar)
    }

    fun onGuiClosed() {
        finishRename()
    }
}
