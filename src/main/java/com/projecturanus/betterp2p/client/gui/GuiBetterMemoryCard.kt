package com.projecturanus.betterp2p.client.gui

import appeng.util.Platform
import com.projecturanus.betterp2p.MODID
import com.projecturanus.betterp2p.capability.MemoryInfo
import com.projecturanus.betterp2p.client.ClientCache
import com.projecturanus.betterp2p.client.TextureBound
import com.projecturanus.betterp2p.client.gui.widget.WidgetScrollBar
import com.projecturanus.betterp2p.item.BetterMemoryCardModes
import com.projecturanus.betterp2p.network.*
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.resources.I18n
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import org.lwjgl.input.Mouse
import java.awt.Color

class GuiBetterMemoryCard(msg: S2CListP2P) : GuiScreen(), TextureBound {
    private val outputColor = 0x4566ccff
    private val selectedColor = 0x4545DA75
    private val errorColor = 0x45DA4527
    private val inactiveColor = 0x45FFEA05

    private val xSize = 238
    private val ySize = 206
    private val guiLeft: Int by lazy { (width - this.xSize) / 2 }
    private val guiTop: Int by lazy { (height - this.ySize) / 2 }

    private val tableX = 9
    private val tableY = 19
    private val rowWidth = 203
    private val rowHeight = 22

    private var selectedIndex = -1

    private lateinit var scrollBar: WidgetScrollBar

    private val infos = msg.infos.map(::InfoWrapper).toMutableList()

    private var sortedInfo = infos.toList()

    private var errorInfos = emptyList<Short>()

    private val selectedInfo: InfoWrapper?
        get() = infos.getOrNull(selectedIndex)

    private var infoOnScreen: List<InfoWrapper>

    private var mode = msg.memoryInfo.mode
    private var modeString = getModeString()
    private val modeButton by lazy { GuiButton(0, guiLeft + 8, guiTop + 140, 205, 20, modeString) }

    init {
        selectInfo(msg.memoryInfo.selectedIndex)
        sortInfo()
        infoOnScreen = sortedInfo.take(5)
    }

    override fun initGui() {
        super.initGui()
        checkInfo()
        scrollBar = WidgetScrollBar()
        scrollBar.displayX = guiLeft + 218
        scrollBar.displayY = guiTop + 19
        scrollBar.height = 114
        scrollBar.onScroll = this::onScrollChanged
        scrollBar.setRange(0, infos.size.coerceIn(0..(infos.size-5).coerceAtLeast(0)), 23)
    }

    fun sortInfo() {
        sortedInfo = infos.sortedBy {
            if (it.index == selectedIndex) {
                -2 // Put the selected p2p in the front
            } else if (it.frequency != 0.toShort() && it.frequency == selectedInfo?.frequency && !it.output) {
                -3 // Put input in the beginning
            } else if (it.frequency != 0.toShort() && it.frequency == selectedInfo?.frequency) {
                -1 // Put same frequency in the front
            } else {
                it.frequency + Short.MAX_VALUE
            }
        }
    }

    fun checkInfo() {
        errorInfos = infos.groupBy { it.frequency }.filter { it.value.none { x -> !x.output } }.map { it.key }
    }

    fun refreshInfo(infos: List<P2PInfo>) {
        for (info in infos) {
            val wrapper = InfoWrapper(info)
            this.infos[info.index] = wrapper
        }
        checkInfo()
        sortInfo()
        takeInfo()
    }

    fun onScrollChanged() {
        takeInfo()
    }

    fun takeInfo() {
        infoOnScreen = sortedInfo.drop(scrollBar.currentScroll)
            .take(5)
    }

    fun syncMemoryInfo() {
        ModNetwork.channel.sendToServer(C2SUpdateInfo(MemoryInfo(selectedIndex, mode)))
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
        else if (errorInfos.contains(info.frequency)) {
            // P2P output without an input
            drawRect(x, y, x + rowWidth, y + rowHeight, errorColor)
        }
        else if (!info.hasChannel && info.frequency != 0.toShort()) {
            // No channel
            drawRect(x, y, x + rowWidth, y + rowHeight, inactiveColor)
        }
        else if (selectedInfo?.frequency == info.frequency && info.frequency != 0.toShort()) {
            // Show same frequency
            drawRect(x, y, x + rowWidth, y + rowHeight, outputColor)
        }

        fontRenderer.drawString(info.description, x + 24, y + 3, 0)
        fontRenderer.drawString(I18n.format("gui.better_memory_card.pos", info.pos.x, info.pos.y, info.pos.z), x + 24, y + 12, 0)

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
        if (mode == BetterMemoryCardModes.COPY && !info.output) {
            info.bindButton.enabled = false
        }
        drawButtons(info, x, y, mouseX, mouseY, partialTicks)
        renderP2PColors(info.frequency, x, y)
    }

    override fun onGuiClosed() {
        syncMemoryInfo()
        ModNetwork.channel.sendToServer(C2SCloseGui())
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

    private fun switchMode() {
        // Switch mode
        mode = BetterMemoryCardModes.values()[mode.ordinal.plus(1) % BetterMemoryCardModes.values().size]
        modeString = getModeString()
        modeButton.displayString = modeString

        syncMemoryInfo()
    }

    private fun getModeString(): String {
        return I18n.format("gui.better_memory_card.mode.${mode.name.toLowerCase()}")
    }

    fun findInput(frequency: Short) =
        infos.find { it.frequency == frequency && !it.output }

    fun selectInfo(index: Int) {
        selectedIndex = index
        syncMemoryInfo()

        ClientCache.selectedPosition = selectedInfo?.pos
        ClientCache.selectedFacing = selectedInfo?.facing
        ClientCache.positions.clear()
        ClientCache.positions.addAll(infos.filter { it.frequency == selectedInfo?.frequency && it != selectedInfo }.map { it.pos to it.facing })
    }

    private fun onSelectButtonClicked(info: InfoWrapper) {
        selectInfo(info.index)
    }

    private fun onBindButtonClicked(info: InfoWrapper) {
        if (selectedIndex == -1) return
        when (mode) {
            BetterMemoryCardModes.INPUT -> {
                println("Bind ${info.index} as input")
                ModNetwork.channel.sendToServer(C2SLinkP2P(info.index, selectedIndex))
            }
            BetterMemoryCardModes.OUTPUT -> {
                println("Bind ${info.index} as output")
                ModNetwork.channel.sendToServer(C2SLinkP2P(selectedIndex, info.index))
            }
            BetterMemoryCardModes.COPY -> {
                val input = selectedInfo?.frequency?.let { findInput(it) }
                if (input != null)
                    ModNetwork.channel.sendToServer(C2SLinkP2P(input.index, info.index))
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
            switchMode()
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
