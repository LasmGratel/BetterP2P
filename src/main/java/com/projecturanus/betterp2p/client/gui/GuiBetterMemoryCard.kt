package com.projecturanus.betterp2p.client.gui

import com.projecturanus.betterp2p.BetterP2P
import com.projecturanus.betterp2p.MODID
import com.projecturanus.betterp2p.capability.MemoryInfo
import com.projecturanus.betterp2p.client.ClientCache
import com.projecturanus.betterp2p.client.TextureBound
import com.projecturanus.betterp2p.client.gui.widget.WidgetP2PDevice
import com.projecturanus.betterp2p.client.gui.widget.WidgetScrollBar
import com.projecturanus.betterp2p.item.BetterMemoryCardModes
import com.projecturanus.betterp2p.network.*
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.resources.I18n
import net.minecraft.util.ResourceLocation
import org.lwjgl.input.Mouse

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

    private var selectedInfo: InfoWrapper?
        get() = infos.getOrNull(selectedIndex)
        set(value) {
            selectedIndex = value?.index ?: -1
        }

    private val widgetDevices: List<WidgetP2PDevice>
    private var infoOnScreen: List<InfoWrapper>

    private val descriptionLines: MutableList<String> = mutableListOf()

    private var mode = msg.memoryInfo.mode
    private var modeString = getModeString()
    private val modeButton by lazy { GuiButton(0, guiLeft + 8, guiTop + 140, 205, 20, modeString) }

    init {
        selectInfo(msg.memoryInfo.selectedIndex)
        sortInfo()
        infoOnScreen = sortedInfo.take(5)

        val list = mutableListOf<WidgetP2PDevice>()
        for (i in 0..4) {
            list += WidgetP2PDevice(::selectedInfo, ::mode, { sortedInfo.getOrNull(i + scrollBar.currentScroll) }, 0, 0)
        }
        widgetDevices = list.toList()
    }

    override fun initGui() {
        super.initGui()
        checkInfo()
        scrollBar = WidgetScrollBar()
        scrollBar.displayX = guiLeft + 218
        scrollBar.displayY = guiTop + 19
        scrollBar.height = 114
        scrollBar.setRange(0, infos.size.coerceIn(0..(infos.size-5).coerceAtLeast(0)), 23)

        for (i in 0..4) {
            widgetDevices[i].x = guiLeft + tableX
            widgetDevices[i].y = guiTop + tableY + 23 * i
        }
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
        infos.forEach { it.error = false }
        infos.groupBy { it.frequency }.filter { it.value.none { x -> !x.output } }.forEach { it.value.forEach { info ->
            info.error = true
        } }
    }

    fun refreshInfo(infos: List<P2PInfo>) {
        for (info in infos) {
            val wrapper = InfoWrapper(info)
            this.infos[info.index] = wrapper
        }
        checkInfo()
        sortInfo()
    }

    fun syncMemoryInfo() {
        ModNetwork.channel.sendToServer(C2SUpdateInfo(MemoryInfo(selectedIndex, mode)))
    }

    fun drawInformation() {
        val x = 8
        var y = 170
        for (line in descriptionLines) {
            fontRenderer.drawString(line, guiLeft + x, guiTop + y, 0)
            y += fontRenderer.FONT_HEIGHT
        }
    }

    override fun onGuiClosed() {
        syncMemoryInfo()
        ModNetwork.channel.sendToServer(C2SCloseGui())
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawDefaultBackground()
        drawBackground()
        scrollBar.draw(this)

        for (widget in widgetDevices) {
            widget.render(this, mouseX, mouseY, partialTicks)
        }
        modeButton.drawButton(mc, mouseX, mouseY, partialTicks)

        if (modeButton.isMouseOver) {
            descriptionLines.clear()
            descriptionLines += I18n.format("gui.better_memory_card.desc.mode", I18n.format("gui.better_memory_card.mode.${mode.next().name.toLowerCase()}"))
        } else {
            descriptionLines.clear()
        }
        drawInformation()

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    private fun switchMode() {
        // Switch mode
        mode = mode.next()
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
                BetterP2P.logger.debug("Bind ${info.index} as input")
                ModNetwork.channel.sendToServer(C2SLinkP2P(info.index, selectedIndex))
            }
            BetterMemoryCardModes.OUTPUT -> {
                BetterP2P.logger.debug("Bind ${info.index} as output")
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
        for (widget in widgetDevices) {
            val info = widget.infoSupplier()
            if (info?.selectButton?.mousePressed(mc, mouseX, mouseY) == true)
                onSelectButtonClicked(widget.infoSupplier()!!)
            else if (info?.bindButton?.mousePressed(mc, mouseX, mouseY) == true)
                onBindButtonClicked(widget.infoSupplier()!!)
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
