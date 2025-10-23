package dev.lasm.betterp2p.client.gui

import appeng.client.gui.Tooltip
import appeng.client.gui.style.Blitter
import appeng.client.gui.widgets.ITooltip
import appeng.parts.p2p.FluidP2PTunnelPart
import appeng.parts.p2p.MEP2PTunnelPart
import com.mojang.blaze3d.systems.RenderSystem
import dev.lasm.betterp2p.BetterP2P
import dev.lasm.betterp2p.BetterP2P.MOD_ID
import dev.lasm.betterp2p.client.AdvancedMemoryCardMenu
import dev.lasm.betterp2p.client.ClientCache
import dev.lasm.betterp2p.client.gui.widget.*
import dev.lasm.betterp2p.item.BetterMemoryCardModes
import dev.lasm.betterp2p.item.MAX_TOOLTIP_LENGTH
import dev.lasm.betterp2p.network.data.MemoryInfo
import dev.lasm.betterp2p.network.data.P2PInfo
import dev.lasm.betterp2p.network.data.P2PLocation
import dev.lasm.betterp2p.network.data.TUNNEL_ANY
import dev.lasm.betterp2p.network.packet.C2SCloseGui
import dev.lasm.betterp2p.network.packet.C2SRefreshP2PList
import dev.lasm.betterp2p.network.packet.C2SUpdateMemoryInfo
import dev.lasm.betterp2p.util.p2p.ClientTunnelInfo
import java.util.Optional
import kotlin.jvm.optionals.getOrNull
import net.minecraft.ChatFormatting
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.ComponentRenderUtils
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.components.events.ContainerEventHandler
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.MenuAccess
import net.minecraft.client.renderer.Rect2i
import net.minecraft.client.resources.language.I18n
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.FormattedCharSequence
import net.neoforged.neoforge.network.PacketDistributor

val TEXTURE = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/advanced_memory_card.png")
const val GUI_WIDTH = 288
const val GUI_TEX_HEIGHT = 264

val TEXTURE_BLITTER = Blitter.texture(TEXTURE, GUI_WIDTH, GUI_TEX_HEIGHT)

class GuiAdvancedMemoryCard(val theMenu: AdvancedMemoryCardMenu) :
    Screen(Component.empty()), MenuAccess<AdvancedMemoryCardMenu>, ContainerEventHandler {
    private var ySize: Int = 0
    private var leftPos: Int = 0
    private var topPos: Int = 0

    private val tableX = 9
    private val tableY = 19

    private var type: ClientTunnelInfo? =
        BetterP2P.proxy.getP2PFromIndex(menu.memoryInfo.type) as? ClientTunnelInfo

    var memoryInfo = theMenu.memoryInfo
    var scale = memoryInfo.guiScale

    val resizeButton = IconButton(scale.ordinal * 32, 200, this::onResize)

    val typeButton = P2PTypeButton(::type, this::onChangeType, this::openTypeSelector)

    val refreshButton = IconButton(160, 200, this::onRefresh)

    fun onRefresh(_button: Button?) {
        PacketDistributor.sendToServer(C2SRefreshP2PList(type?.index ?: TUNNEL_ANY))
    }

    private fun onChangeType(button: Button) {
        val button = button as P2PTypeButton
        type = button.nextType(false)
        button.commitType()
    }

    private val searchText: String
        get() = searchBar.value

    private val infos = InfoList(menu.infos.map(::InfoWrapper), ::searchText, ::mode)

    private val typeSelector: WidgetTypeSelector

    private var mode = menu.memoryInfo.mode

    private val sortRules: List<Component> by lazy {
        listOf(
            Component.translatable("gui.advanced_memory_card.sortinfo1")
                .withStyle(Style.EMPTY.withColor(ChatFormatting.AQUA).withUnderlined(true)),
            Component.literal("@in")
                .withStyle(ChatFormatting.BLUE)
                .append(
                    Component.literal(" - ")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.translatable("gui.advanced_memory_card.sortinfo2"))
                ),
            Component.literal("@out")
                .withStyle(ChatFormatting.GOLD)
                .append(
                    Component.literal(" - ")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.translatable("gui.advanced_memory_card.sortinfo3"))
                ),
            Component.literal("@b")
                .withStyle(ChatFormatting.GREEN)
                .append(
                    Component.literal(" - ")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.translatable("gui.advanced_memory_card.sortinfo4"))
                ),
            Component.literal("@u")
                .withStyle(ChatFormatting.RED)
                .append(
                    Component.literal(" - ")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.translatable("gui.advanced_memory_card.sortinfo5"))
                ),
            Component.literal("@type=<name1>[;<name2>;]...")
                .withStyle(ChatFormatting.YELLOW)
                .append(
                    Component.literal(" - ")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.translatable("gui.advanced_memory_card.sortinfo6"))
                ),
            Component.translatable("gui.advanced_memory_card.sortinfo7")
                .withStyle(ChatFormatting.GRAY)
        )
    }

    val scrollBar = WidgetScrollBar(tableX, tableY)

    val searchBar: EditBox by lazy {
        object : EditBox(font, 0, 0, 100, 10, Component.empty()), ITooltip {
            override fun getTooltipMessage(): MutableList<Component> {
                return sortRules.toMutableList()
            }

            override fun getTooltipArea(): Rect2i {
                return Rect2i(x, y, width, height)
            }

            override fun isTooltipAreaVisible(): Boolean {
                return isVisible
            }
        }
    }

    fun getFont(): Font = font

    val modeButton by lazy { IconButton((mode.ordinal + 3) * 32, 232, ::onChangeMode) }

    val col by lazy { WidgetP2PColumn(this, infos, 0, 0, ::selectedInfo, ::mode, scrollBar) }

    private val selectedInfo: InfoWrapper?
        get() = infos.selectedInfo

    init {
        val typeSelectorList = mutableListOf<ClientTunnelInfo>()
        var toAdd =
            BetterP2P.proxy.getP2PFromClass(MEP2PTunnelPart::class.java) as? ClientTunnelInfo
        if (toAdd != null) {
            typeSelectorList.add(toAdd)
        }
        toAdd = BetterP2P.proxy.getP2PFromClass(FluidP2PTunnelPart::class.java) as? ClientTunnelInfo
        if (toAdd != null) {
            typeSelectorList.add(toAdd)
        }
        BetterP2P.proxy.getP2PTypeList().forEach {
            if (!typeSelectorList.contains(it)) {
                typeSelectorList.add(it as ClientTunnelInfo)
            }
        }
        typeSelector = WidgetTypeSelector(0, 0, this, typeSelectorList)
    }

    private val modeDescriptions: List<List<Component>> =
        listOf(
            fmtTooltips(
                title = BetterMemoryCardModes.OUTPUT.unlocalizedName,
                maxChars = MAX_TOOLTIP_LENGTH,
                keys = BetterMemoryCardModes.OUTPUT.unlocalizedDesc
            ),
            fmtTooltips(
                title = BetterMemoryCardModes.INPUT.unlocalizedName,
                maxChars = MAX_TOOLTIP_LENGTH,
                keys = BetterMemoryCardModes.INPUT.unlocalizedDesc
            ),
            fmtTooltips(
                title = BetterMemoryCardModes.COPY.unlocalizedName,
                maxChars = MAX_TOOLTIP_LENGTH,
                keys = BetterMemoryCardModes.COPY.unlocalizedDesc
            ),
            fmtTooltips(
                title = BetterMemoryCardModes.UNBIND.unlocalizedName,
                maxChars = MAX_TOOLTIP_LENGTH,
                keys = BetterMemoryCardModes.UNBIND.unlocalizedDesc
            )
        )

    fun onChangeMode(button: Button) {
        val button = button as IconButton
        mode = mode.next()
        button.texX = (mode.ordinal + 3) * 32
        button.texY = 232
        button.messages = modeDescriptions[mode.ordinal].toMutableList()
        syncMemoryInfo()
    }

    fun onResize(button: Button) {
        scale =
            when (scale) {
                GuiScale.DYNAMIC -> GuiScale.LARGE
                GuiScale.LARGE -> GuiScale.NORMAL
                GuiScale.NORMAL -> GuiScale.SMALL
                GuiScale.SMALL -> GuiScale.DYNAMIC
            }
        repositionElements()
    }

    override fun init() {
        checkInfo()

        val h = height.coerceAtLeast(256)
        if (scale.minHeight > h) {
            scale = GuiScale.DYNAMIC
        }

        val numEntries = scale.size(height - 75)
        this.ySize = (numEntries * P2PEntryConstants.HEIGHT) + 75 + (numEntries - 1)

        this.leftPos = (this.width - GUI_WIDTH) / 2
        this.topPos = (this.height - ySize) / 2

        searchBar.x = leftPos + 163
        searchBar.y = topPos + 5
        searchBar.setResponder {
            infos.refresh()
            col.entries.forEach { it.updateButtonVisibility() }
            scrollBar.height = numEntries * P2PEntryConstants.HEIGHT + (numEntries - 1) - 7
            scrollBar.setRange(
                0,
                infos.filtered.size.coerceIn(
                    0,
                    (infos.filtered.size - numEntries).coerceAtLeast(0)
                ),
                23
            )
        }

        scrollBar.x = leftPos + 268
        scrollBar.y = topPos + 19

        resizeButton.x = leftPos - 32
        resizeButton.y = topPos + 2
        resizeButton.texX = scale.ordinal * 32
        resizeButton.messages[0] = Component.literal("Resize")

        modeButton.x = leftPos - 32
        modeButton.y = topPos + 34
        modeButton.texX = (mode.ordinal + 3) * 32
        modeButton.messages = modeDescriptions[mode.ordinal].toMutableList()

        typeButton.setPosition(leftPos - 32, topPos + 66)

        refreshButton.setPosition(leftPos - 32, topPos + 98)
        refreshButton.messages[0] = Component.literal("Refresh")

        col.resize(scale, h - 75)
        col.setPosition(leftPos + tableX, topPos + tableY)

        infos.select(memoryInfo.selectedEntry.getOrNull())
        infos.refresh()

        scrollBar.height = numEntries * P2PEntryConstants.HEIGHT + (numEntries - 1) - 7
        scrollBar.setRange(
            0,
            infos.filtered.size.coerceIn(0, (infos.filtered.size - numEntries).coerceAtLeast(0)),
            23
        )

        col.entries.forEach { it.updateButtonVisibility() }

        typeSelector.parent = typeButton
        typeSelector.visible = false

        checkInfo()
        refreshOverlay()

        selectInfo(memoryInfo.selectedEntry.getOrNull())

        col.visitWidgets(::addRenderableWidget)
        addRenderableWidget(refreshButton)
        addRenderableWidget(scrollBar)
        addRenderableWidget(typeButton)
        addRenderableWidget(resizeButton)
        addRenderableWidget(modeButton)
        addRenderableWidget(searchBar)
        addRenderableWidget(typeSelector)
    }

    override fun isPauseScreen(): Boolean {
        return false
    }

    override fun afterMouseAction() {
        super.afterMouseAction()
    }

    override fun renderMenuBackground(graphics: GuiGraphics) {
        super.renderMenuBackground(graphics)
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0.0f, 0.0f, GUI_WIDTH, 60, 288, 264)

        val p2pHeight = P2PEntryConstants.HEIGHT + 1
        for (i in 0 until scale.size(ySize - 75) - 2) {
            graphics.blit(
                TEXTURE,
                leftPos,
                topPos + 60 + p2pHeight * i,
                0,
                0.0f,
                60.0f,
                GUI_WIDTH,
                102 - 60,
                288,
                264
            )
        }
        graphics.blit(
            TEXTURE,
            leftPos,
            topPos + ySize - 98,
            0,
            0.0f,
            102.0f,
            GUI_WIDTH,
            98,
            288,
            264
        )
    }

    override fun mouseScrolled(
        mouseX: Double,
        mouseY: Double,
        scrollX: Double,
        scrollY: Double
    ): Boolean {
        return scrollBar.mouseScrolled(mouseX, mouseY, scrollX, scrollY) ||
            super<Screen>.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
    }

    override fun render(graphics: GuiGraphics, i: Int, j: Int, f: Float) {
        this.renderMenuBackground(graphics)

        graphics.drawString(
            font,
            Component.translatable("item.betterp2p.advanced_memory_card"),
            leftPos + tableX,
            topPos + 6,
            0x404040,
            false
        )

        RenderSystem.disableDepthTest()
        RenderSystem.depthMask(false)

        super.render(graphics, i, j, f)

        renderTooltips(graphics, i, j)

        RenderSystem.depthMask(true)
        RenderSystem.enableDepthTest()
    }

    private fun syncMemoryInfo() {
        PacketDistributor.sendToServer(
            C2SUpdateMemoryInfo(
                MemoryInfo(
                    Optional.ofNullable(infos.selectedEntry),
                    selectedInfo?.frequency ?: 0,
                    mode,
                    scale,
                    type?.index ?: TUNNEL_ANY
                )
            )
        )
    }

    private fun checkInfo() {

        infos.filtered.forEach {
            it.error =
                it.frequency != 0.toShort() &&
                    if (it.output) {
                        col.findInput(it.frequency) == null
                    } else {
                        col.findOutput(it.frequency) == null
                    }
        }
    }

    fun refreshInfo(infos: List<P2PInfo>) {
        this.infos.rebuild(infos.map(::InfoWrapper), scrollBar, scale.size(height - 75))
        checkInfo()
        refreshOverlay()
    }

    fun updateInfo(infos: List<P2PInfo>) {
        this.infos.update(infos.map(::InfoWrapper), scrollBar, scale.size(height - 75))
        checkInfo()
        refreshOverlay()
    }

    private fun renderTooltips(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        for (c in children()) {
            if (c is ITooltip) {
                if (!c.isTooltipAreaVisible) {
                    continue
                }

                val area: Rect2i = c.tooltipArea
                if (
                    mouseX >= area.x &&
                        mouseY >= area.y &&
                        mouseX < area.x + area.width &&
                        mouseY < area.y + area.height
                ) {
                    val tooltip = Tooltip(c.tooltipMessage)
                    if (tooltip.content.isNotEmpty()) {
                        drawTooltipWithHeader(guiGraphics, tooltip, mouseX, mouseY)
                    }
                }
            }
        }

        //        // Widget-container uses screen-relative coordinates while the rest uses
        // window-relative
        //        val tooltip: Tooltip = this.widgets.getTooltip(mouseX - leftPos, mouseY - topPos)
        //        if (tooltip != null) {
        //            drawTooltipWithHeader(guiGraphics, tooltip, mouseX, mouseY)
        //        }
    }

    private fun drawTooltipWithHeader(
        guiGraphics: GuiGraphics,
        tooltip: Tooltip,
        mouseX: Int,
        mouseY: Int
    ) {
        drawTooltipWithHeader(guiGraphics, mouseX, mouseY, tooltip.content)
    }

    override fun onClose() {
        ClientCache.searchText = searchBar.value
        col.onGuiClosed()
        syncMemoryInfo()
        PacketDistributor.sendToServer(C2SCloseGui())
        super.onClose()
    }

    fun drawTooltip(guiGraphics: GuiGraphics, x: Int, y: Int, lines: List<Component>) {
        if (lines.isEmpty()) {
            return
        }

        // Max width should be half screen with some padding.
        // Vanilla will place the tooltip on the right or left of the cursor
        // automatically, but uses a 12px offset (we use 40px for some extra space)
        val maxWidth = width / 2 - 40

        // Make the first line white
        // All lines after the first are colored gray
        val styledLines: MutableList<FormattedCharSequence> = java.util.ArrayList(lines.size)
        for (line in lines) {
            styledLines.addAll(ComponentRenderUtils.wrapComponents(line, maxWidth, font))
        }
        guiGraphics.renderTooltip(font, styledLines, x, y)
    }

    fun drawTooltipWithHeader(guiGraphics: GuiGraphics, x: Int, y: Int, lines: List<Component>) {
        if (lines.isEmpty()) {
            return
        }

        val formattedLines = ArrayList<Component>(lines.size)
        for (i in lines.indices) {
            if (i == 0) {
                formattedLines.add(
                    lines[i].copy().withStyle { s: Style -> s.withColor(ChatFormatting.WHITE) }
                )
            } else {
                formattedLines.add(
                    lines[i].copy().withStyle { s: Style ->
                        if (s.color != null) {
                            return@withStyle s
                        } else {
                            return@withStyle s.withColor(ChatFormatting.GRAY)
                        }
                    }
                )
            }
        }
        drawTooltip(guiGraphics, x, y, formattedLines)
    }

    private fun refreshOverlay() {
        if (selectedInfo == null) {
            ClientCache.selectedPosition = null
            ClientCache.selectedFacing = null
        } else {
            ClientCache.selectedPosition = selectedInfo?.loc?.pos
            ClientCache.selectedFacing = selectedInfo?.loc?.facing
        }
        ClientCache.positions.clear()
        ClientCache.positions.addAll(
            infos.sorted
                .filter {
                    it.frequency == selectedInfo?.frequency &&
                        it != selectedInfo &&
                        it.loc.dim == minecraft?.player?.level()?.dimension()
                }
                .filter {
                    val d =
                        minecraft?.player?.blockPosition()?.let { pos -> it.loc.pos.distSqr(pos) }
                    (d?.compareTo(50.0) ?: 1) < 0 // Distance < 50
                }
                .map { it.loc.pos to it.loc.facing }
                .take(200)
        )
    }

    fun openTypeSelector(parent: ITypeReceiver, useAny: Boolean) {
        typeSelector.parent = parent
        if (parent is WidgetP2PDevice) typeSelector.setPosition(parent.x + 20, parent.y)
        else typeSelector.setPosition(parent.x + parent.width, parent.y)
        typeSelector.useAny = useAny
        typeSelector.visible = true
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (typeSelector.mouseClicked(mouseX, mouseY, button)) return true
        else closeTypeSelector(type)
        return super<Screen>.mouseClicked(mouseX, mouseY, button)
    }

    fun closeTypeSelector(type: ClientTunnelInfo?) {
        if (this.type != type) {
            this.type = type
            PacketDistributor.sendToServer(C2SRefreshP2PList(type?.index ?: TUNNEL_ANY))
        }

        typeSelector.visible = false
    }

    fun getTypeID(): Int {
        return 0
    }

    override fun getMenu(): AdvancedMemoryCardMenu = theMenu

    fun openTypeSelector(button: Button) {
        openTypeSelector(typeButton, true)
    }

    fun selectInfo(loc: P2PLocation?) {
        infos.select(loc)
        syncMemoryInfo()
        refreshOverlay()
    }
}

/** Format multiple lines of tooltips by the given max chars. */
fun fmtTooltips(title: String, vararg keys: String, maxChars: Int): List<Component> {
    val result: MutableList<Component> = mutableListOf()
    result.add(Component.translatable(title))
    for (key in keys) {
        val words = I18n.get(key).split(' ')
        var i = 0
        if (key.length < maxChars) {
            result.add(Component.literal(key))
        }
        while (i < words.size) {
            val s = StringBuilder()
            perWord@ while (s.length < maxChars) {
                s.append(words[i])
                i += 1
                if (i >= words.size) break@perWord
                s.append(" ")
            }
            val c = Component.literal(s.toString())
            result.add(if (!s.startsWith('§')) c.withStyle(ChatFormatting.GRAY) else c)
        }
    }
    return result
}
