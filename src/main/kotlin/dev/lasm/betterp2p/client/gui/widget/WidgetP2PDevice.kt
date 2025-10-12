package dev.lasm.betterp2p.client.gui.widget

import appeng.client.gui.widgets.ITooltip
import dev.lasm.betterp2p.client.gui.InfoWrapper
import dev.lasm.betterp2p.client.gui.drawBlockIcon
import dev.lasm.betterp2p.client.gui.drawIcon
import dev.lasm.betterp2p.client.gui.isClicked
import dev.lasm.betterp2p.item.BetterMemoryCardModes
import dev.lasm.betterp2p.network.data.TUNNEL_ANY
import dev.lasm.betterp2p.network.packet.C2SChangeP2PType
import dev.lasm.betterp2p.util.p2p.ClientTunnelInfo
import java.util.function.Consumer
import kotlin.reflect.KProperty0
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.renderer.Rect2i
import net.minecraft.client.resources.language.I18n
import net.minecraft.network.chat.Component
import net.neoforged.neoforge.network.PacketDistributor
import org.lwjgl.glfw.GLFW

object P2PEntryConstants {
    const val HEIGHT = 41
    const val WIDTH = 254
    const val OUTPUT_COLOR = 0x4566ccff
    const val SELECTED_COLOR = 0x4545DA75
    const val ERROR_COLOR = 0x45DA4527
    const val INACTIVE_COLOR = 0x45FFEA05
    const val LEFT_ALIGN = 24
}

class WidgetP2PDevice(
    private val index: Int,
    private val selectedInfoProperty: KProperty0<InfoWrapper?>,
    val modeSupplier: () -> BetterMemoryCardModes,
    val infoSupplier: () -> InfoWrapper?,
    val col: WidgetP2PColumn,
    x: Int,
    y: Int
) :
    AbstractWidget(x, y, P2PEntryConstants.WIDTH, P2PEntryConstants.HEIGHT, Component.empty()),
    ITypeReceiver,
    ITooltip {

    val font
        get() = Minecraft.getInstance().font

    var renderNameTextfield = true

    private val selectedInfo: InfoWrapper?
        get() = selectedInfoProperty.get()

    val bindButton: Button =
        Button.builder(Component.translatable("gui.advanced_memory_card.bind")) {
                col.onBindButtonClicked(infoSupplier()!!)
            }
            .size(56, 20)
            .build()
    val unbindButton: Button =
        Button.builder(Component.translatable("gui.advanced_memory_card.unbind")) {
                col.onUnbindButtonClicked(infoSupplier()!!)
            }
            .size(56, 20)
            .build()

    /** Update the button visibility */
    fun updateButtonVisibility() {
        val info = infoSupplier()
        val mode = modeSupplier()

        if (info == null) {
            bindButton.visible = false
            unbindButton.visible = false
            return
        }

        when {
            selectedInfo == null ||
                mode == BetterMemoryCardModes.COPY &&
                    ((!info.output && info.frequency != 0.toShort()) || selectedInfo!!.output) -> {
                // Copy mode
                // If this info is (input && set freq) || selected info is an output
                // Disable all buttons
                bindButton.visible = false
                unbindButton.visible = false
            }
            mode == BetterMemoryCardModes.UNBIND -> {
                // Only unbinds allowed in unbind mode
                bindButton.visible = false
                unbindButton.visible = info.frequency != 0.toShort()
            }
            else -> {
                // Other modes:
                // Bind allowed only if currently not selected && selected is unbound; OR not bound
                // to
                // selected
                bindButton.visible =
                    info.loc != selectedInfo!!.loc &&
                        (selectedInfo!!.frequency == 0.toShort() ||
                            info.frequency != selectedInfo!!.frequency)
                unbindButton.visible = false
            }
        }
    }

    override fun updateWidgetNarration(narrationElementOutput: NarrationElementOutput) {}

    override fun renderWidget(
        graphics: GuiGraphics,
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float
    ) {
        val info = infoSupplier() ?: return
        // draw the background first
        when {
            selectedInfo?.loc == info.loc -> {
                graphics.fill(
                    x,
                    y,
                    x + P2PEntryConstants.WIDTH,
                    y + P2PEntryConstants.HEIGHT,
                    P2PEntryConstants.SELECTED_COLOR
                )
            }
            info.error -> {
                // P2P output without an input, or unbound
                graphics.fill(
                    x,
                    y,
                    x + P2PEntryConstants.WIDTH,
                    y + P2PEntryConstants.HEIGHT,
                    P2PEntryConstants.ERROR_COLOR
                )
            }
            !info.hasChannel && info.frequency != 0.toShort() -> {
                // No channel
                graphics.fill(
                    x,
                    y,
                    x + P2PEntryConstants.WIDTH,
                    y + P2PEntryConstants.HEIGHT,
                    P2PEntryConstants.INACTIVE_COLOR
                )
            }
            selectedInfo?.frequency == info.frequency && info.frequency != 0.toShort() -> {
                // Show same frequency
                graphics.fill(
                    x,
                    y,
                    x + P2PEntryConstants.WIDTH,
                    y + P2PEntryConstants.HEIGHT,
                    P2PEntryConstants.OUTPUT_COLOR
                )
            }
        }

        if (
            isHovered &&
                mouseX > x.toDouble() + 50 &&
                mouseX < x.toDouble() + 50 + 160 &&
                mouseY > y.toDouble() + 1 &&
                mouseY < y.toDouble() + 1 + 13
        ) {
            graphics.fill(
                x + 50,
                y + 1,
                x + 50 + 160,
                y + 1 + 12,
                0x6E000000 // ARGB xd
            )
        }

        graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f)
        // Draw our icons...
        drawBlockIcon(graphics, info.icon, info.overlay, x + 3, y + 3)

        if (info.output) {
            graphics.drawIcon(144, 200, x, y + 4)
        } else {
            graphics.drawIcon(128, 200, x, y + 4)
        }
        if (info.error || info.frequency == 0.toShort() || !info.hasChannel) {
            graphics.drawIcon(144, 216, x + 3, y + 20)
        } else {
            graphics.drawIcon(128, 216, x + 3, y + 20)
        }
        // Now draw the stuff that messes up our GL state (aka text)
        val leftAlign = x + P2PEntryConstants.LEFT_ALIGN
        if (renderNameTextfield) {
            graphics.drawString(
                font,
                I18n.get("gui.advanced_memory_card.name", info.name),
                leftAlign,
                y + 2,
                0x404040,
                false
            )
        } else {
            graphics.drawString(
                font,
                I18n.get("gui.advanced_memory_card.name", ""),
                leftAlign,
                y + 2,
                0x404040,
                false
            )
        }
        graphics.drawString(font, info.description, leftAlign, y + 12, 0x404040, false)
        graphics.drawString(font, info.freqDisplay, leftAlign, y + 22, 0x404040, false)
        if (info.channels != null) {
            graphics.drawString(font, info.channels!!, leftAlign, y + 32, 0x404040, false)
        }

        updateButtonVisibility()
    }

    override fun clicked(mouseX: Double, mouseY: Double): Boolean {
        return bindButton.isClicked(mouseX, mouseY) ||
            unbindButton.isClicked(mouseX, mouseY) ||
            super.clicked(mouseX, mouseY)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (clicked(mouseX, mouseY) && button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            this.playDownSound(Minecraft.getInstance().soundManager)
            col.gui.openTypeSelector(this, false)
            return true
        }
        return bindButton.mouseClicked(mouseX, mouseY, button) ||
            unbindButton.mouseClicked(mouseX, mouseY, button) ||
            super.mouseClicked(mouseX, mouseY, button)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (col.getRenameBar().visible) {
            col.getRenameBar().isFocused = true
            if (keyCode == GLFW.GLFW_KEY_ENTER) {
                col.finishRename()
            } else {
                return col.getRenameBar().keyPressed(keyCode, scanCode, modifiers)
            }
            return true
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun charTyped(codePoint: Char, modifiers: Int): Boolean {
        if (col.getRenameBar().visible) {
            col.getRenameBar().isFocused = true
            return col.getRenameBar().charTyped(codePoint, modifiers)
        }
        return super.charTyped(codePoint, modifiers)
    }

    override fun onClick(mouseX: Double, mouseY: Double,  button: Int) {
        val info = infoSupplier() ?: return
        if (
            isHovered &&
                mouseX > x.toDouble() + 50 &&
                mouseX < x.toDouble() + 50 + 160 &&
                mouseY > y.toDouble() + 1 &&
                mouseY < y.toDouble() + 1 + 13
        ) {
            col.onRenameButtonClicked(info, index)
        } else {
            col.gui.selectInfo(info.loc)
            col.finishRename()
        }
    }

    override fun visitWidgets(consumer: Consumer<AbstractWidget>) {
        super.visitWidgets(consumer)
        bindButton.x = x + 190
        bindButton.width = 56
        bindButton.y = y + 14

        unbindButton.x = x + 190
        unbindButton.width = 56
        unbindButton.y = y + 14
        consumer.accept(bindButton)
        consumer.accept(unbindButton)
    }

    override fun accept(type: ClientTunnelInfo?) {
        PacketDistributor.sendToServer(
            C2SChangeP2PType(type?.index ?: TUNNEL_ANY, infoSupplier()!!.loc)
        )
        col.gui.closeTypeSelector(type)
    }

    override fun getTooltipMessage(): MutableList<Component> {
        return infoSupplier()!!.hoverInfo.asSequence().map { Component.literal(it) }.toMutableList()
    }

    override fun getTooltipArea(): Rect2i {
        return Rect2i(x, y, 20, height)
    }

    override fun isTooltipAreaVisible(): Boolean {
        return visible && infoSupplier() != null
    }
}
