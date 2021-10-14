package com.projecturanus.betterp2p.client

import appeng.util.Platform
import com.projecturanus.betterp2p.MODID
import com.projecturanus.betterp2p.network.C2SUpdateInfo
import com.projecturanus.betterp2p.network.ModNetwork
import com.projecturanus.betterp2p.network.P2PInfo
import com.projecturanus.betterp2p.network.S2CListP2P
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import org.lwjgl.input.Mouse
import java.awt.Color

class GuiBetterMemoryCard(msg: S2CListP2P) : GuiScreen(), TextureBound {
    private class InfoWrapper(info: P2PInfo) {
        // Basic information
        val index: Int = info.index
        val frequency: Short = info.frequency
        val pos: BlockPos = info.pos
        val facing: EnumFacing = info.facing
        val description: String
        val output: Boolean = info.output

        // Widgets
        val selectButton = GuiButton(0, 0, 0, 34, 20,"Select")
        val bindButton = GuiButton(0, 0, 0, 34, 20, "Bind")

        init {
            description = buildString {
                append("P2P ")
                if (output)
                    append("Output")
                else
                    append("Input")
                append(" - ")
                if (info.frequency.toInt() == 0)
                    append("Not set")
                else
                    append(Platform.p2p().toHexString(info.frequency))
            }
        }
    }

    private val outputColor = 0x7166ccff
    private val selectedColor = 0x2745DA75

    private val xSize = 238
    private val ySize = 206
    private val guiLeft: Int by lazy { (width - this.xSize) / 2 }
    private val guiTop: Int by lazy { (height - this.ySize) / 2 }

    private val tableX = 9
    private val tableY = 19
    private val rowWidth = 203
    private val rowHeight = 22

    private var scrollIndex = 0
    private var selectedIndex = msg.targetIndex

    private lateinit var scrollBar: WidgetScrollBar

    private val infos = msg.infos.map(::InfoWrapper).toMutableList()

    private val selectedInfo: InfoWrapper?
        get() = infos.getOrNull(selectedIndex)

    private var infoOnScreen: List<InfoWrapper> = infos.take(5)

    private var mode = BetterMemoryCardModes.OUTPUT
    private var modeString = "Mode: Bind Output"
    private val modeButton by lazy { GuiButton(0, guiLeft + 8, guiTop + 140, 205, 20, modeString) }

    override fun initGui() {
        super.initGui()
        scrollBar = WidgetScrollBar()
        scrollBar.displayX = guiLeft + 218
        scrollBar.displayY = guiTop + 19
        scrollBar.height = 114
        scrollBar.setRange(0, 10, 23)
    }

    fun refreshInfo(input: P2PInfo, output: P2PInfo) {
        infos[input.index] = InfoWrapper(input)
        infos[output.index] = InfoWrapper(output)
        infoOnScreen = infos.take(5)
    }

    private fun drawButtons(info: InfoWrapper, x: Int, y: Int, mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (!info.bindButton.enabled && info.selectButton.enabled) {
            info.bindButton.enabled = false
            info.selectButton.enabled = true

            info.selectButton.x = x + 166
            info.selectButton.y = y + 1
            info.selectButton.drawButton(mc, mouseX, mouseY, partialTicks)
        } else if (info.bindButton.enabled && info.selectButton.enabled) {
            info.bindButton.enabled = true
            info.selectButton.enabled = true

            // Select button on the left
            info.selectButton.x = x + 130
            info.selectButton.y = y + 1
            info.selectButton.drawButton(mc, mouseX, mouseY, partialTicks)

            // Bind button on the right
            info.bindButton.x = x + 166
            info.bindButton.y = y + 1
            info.bindButton.drawButton(mc, mouseX, mouseY, partialTicks)
        } else if (!info.selectButton.enabled && !info.bindButton.enabled) {
            // TODO Unbind
            info.bindButton.enabled = false
            info.selectButton.enabled = false
        }
    }

    private fun drawRectBorder(left: Int, top: Int, width: Int, height: Int, stroke: Int) {
        drawHorizontalLine(left, left + width, top, stroke)
        drawHorizontalLine(left, left + width, top + height, stroke)
        drawVerticalLine(left, top, top + height, stroke)
        drawVerticalLine(left + width, top, top + height, stroke)
    }

    private fun renderP2PColors(frequency: Short, x: Int, y: Int) {
        val colors = Platform.p2p().toColors(frequency)
        drawRectBorder(x + 9, y + 9, 3, 3, Color.BLACK.rgb)
        for (row in 0..1) {
            for (col in 0..1) {
                for (colorIndex in 0..3) {
                    val offsetX: Int = colorIndex % 2
                    val offsetY: Int = colorIndex / 2
                    drawHorizontalLine(x + 7 + col * 6 + offsetX, x + 8 + col * 6 + offsetX, y + 7 + row * 6 + offsetY, Color(colors[colorIndex].dye.colorValue, false).rgb)
                }
                drawRectBorder(x + 6 + col * 6, y + 6 + row * 6, 3, 3, Color.BLACK.rgb)
            }
        }
    }

    private fun renderInfo(info: InfoWrapper, x: Int, y: Int, mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (selectedIndex == info.index)
            drawRect(x, y, x + rowWidth, y + rowHeight, selectedColor)
        else if (selectedInfo?.frequency == info.frequency) {
            // Show same frequency
            drawRect(x, y, x + rowWidth, y + rowHeight, outputColor)
        }

        fontRenderer.drawString(info.description, x + 32, y + 3, 0)

        if (selectedIndex == -1) {
            info.bindButton.enabled = false
            info.selectButton.enabled = true
        } else if (info.index != selectedIndex) {
            info.bindButton.enabled = true
            info.selectButton.enabled = true

        } else {
            // TODO Unbind
            info.bindButton.enabled = false
            info.selectButton.enabled = false
        }
        drawButtons(info, x, y, mouseX, mouseY, partialTicks)
        renderP2PColors(info.frequency, x, y)
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawDefaultBackground()
        drawBackground()
        scrollBar.draw(this)

        infoOnScreen.forEachIndexed { i, info ->
            renderInfo(info, guiLeft + tableX, guiTop + tableY + 23 * i, mouseX, mouseY, partialTicks)
        }
        modeButton.drawButton(mc, mouseX, mouseY, partialTicks)
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    private fun onModeButtonClicked() {
        // Switch mode
        mode = if (mode == BetterMemoryCardModes.INPUT)
            BetterMemoryCardModes.OUTPUT
        else
            BetterMemoryCardModes.INPUT
        modeString = getModeString()
        modeButton.displayString = modeString
    }

    private fun getModeString() = when (mode) {
        BetterMemoryCardModes.INPUT -> "Mode: Bind Input"
        BetterMemoryCardModes.OUTPUT -> "Mode: Bind Output"
    }

    private fun onSelectButtonClicked(info: InfoWrapper) {
        selectedIndex = info.index
    }

    private fun onBindButtonClicked(info: InfoWrapper) {
        if (selectedIndex == -1) return
        when (mode) {
            BetterMemoryCardModes.INPUT -> {
                println("Bind ${info.index} as input")
                ModNetwork.channel.sendToServer(C2SUpdateInfo(info.index, selectedIndex))
            }
            BetterMemoryCardModes.OUTPUT -> {
                println("Bind ${info.index} as output")
                ModNetwork.channel.sendToServer(C2SUpdateInfo(selectedIndex, info.index))
            }
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        for (info in infoOnScreen) {
            if (info.selectButton.mousePressed(mc, mouseX, mouseY))
                onSelectButtonClicked(info)
            else if (info.bindButton.mousePressed(mc, mouseX, mouseY))
                onBindButtonClicked(info)
        }
        if (modeButton.mousePressed(mc, mouseX, mouseY)) {
            onModeButtonClicked()
        }
        scrollBar.click(mouseX, mouseY)
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun mouseClickMove(mouseX: Int, mouseY: Int, clickedMouseButton: Int, timeSinceLastClick: Long) {
        scrollBar.click(mouseX, mouseY)
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick)
    }

    override fun handleMouseInput() {
        super.handleMouseInput()
        val i = Mouse.getEventDWheel()
        if (i != 0 && isShiftKeyDown()) {
            val x = Mouse.getEventX() * width / mc.displayWidth
            val y = height - Mouse.getEventY() * height / mc.displayHeight - 1
//            this.mouseWheelEvent(x, y, i / Math.abs(i))
        } else if (i != 0) {
            scrollBar.wheel(i)
        }
    }

    private fun drawItemStack(stack: ItemStack, x: Int, y: Int) {
        RenderHelper.enableGUIStandardItemLighting()
        itemRender.zLevel = 100.0f
        itemRender.renderItemAndEffectIntoGUI(stack, x, y)
        itemRender.zLevel = 0.0f
        RenderHelper.disableStandardItemLighting()
    }

    override fun bindTexture(modid: String, location: String) {
        val loc = ResourceLocation(modid, location)
        mc.textureManager.bindTexture(loc)
    }

    private fun drawBackground() {
        bindTexture(MODID, "textures/gui/better_memory_card.png")
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, this.xSize, this.ySize)
    }

    override fun doesGuiPauseGame(): Boolean {
        return false
    }
}
