package dev.lasm.betterp2p.client.gui.widget

import appeng.parts.p2p.FluidP2PTunnelPart
import appeng.parts.p2p.MEP2PTunnelPart
import appeng.parts.p2p.RedstoneP2PTunnelPart
import com.mojang.blaze3d.systems.RenderSystem
import dev.lasm.betterp2p.BetterP2P
import dev.lasm.betterp2p.client.gui.GuiAdvancedMemoryCard
import dev.lasm.betterp2p.client.gui.drawBlockIcon
import dev.lasm.betterp2p.network.data.TUNNEL_ANY
import dev.lasm.betterp2p.network.packet.C2SRefreshP2PList
import dev.lasm.betterp2p.util.p2p.ClientTunnelInfo
import kotlin.reflect.KProperty0
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.neoforged.neoforge.network.PacketDistributor

private const val s = "gui.advanced_memory_card.types.filtered"

class P2PTypeButton(
    val type: KProperty0<ClientTunnelInfo?>,
    onPress: OnPress,
    private val onSecondaryPress: OnPress
) : IconButton(0, 0, onPress), ITypeReceiver {
    init {
        updateHoverText()
    }

    val types = BetterP2P.proxy.getP2PTypeList()
    var index =
        if (type.get() != null) {
            types.first { it.index == type.get()?.index }.index
        } else {
            types.size
        }
    private val me =
        BetterP2P.proxy.getP2PFromClass(MEP2PTunnelPart::class.java) as ClientTunnelInfo
    private val fluid =
        BetterP2P.proxy.getP2PFromClass(FluidP2PTunnelPart::class.java) as ClientTunnelInfo
    private val redstone =
        BetterP2P.proxy.getP2PFromClass(RedstoneP2PTunnelPart::class.java) as ClientTunnelInfo

    fun nextType(reverse: Boolean): ClientTunnelInfo? {
        return if (reverse) {
            index = (index - 1).rem(types.size + 1)
            types.getOrNull(index) as? ClientTunnelInfo
        } else {
            index = (index + 1).rem(types.size + 1)
            types.getOrNull(index) as? ClientTunnelInfo
        }
    }

    override fun mouseClicked(d: Double, e: Double, i: Int): Boolean {
        if (!this.active || !this.visible) {
            return false
        }
        if ((clicked(d, e))) {
            if (i == 0) {
                this.playDownSound(Minecraft.getInstance().soundManager)
                this.onClick(d, e)
                return true
            } else if (i == 1) {
                this.playDownSound(Minecraft.getInstance().soundManager)
                this.onSecondaryPress.onPress(this)
                return true
            }
        }
        return false
    }

    override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partial: Float) {
        RenderSystem.enableBlend()
        RenderSystem.enableDepthTest()
        renderBackground(guiGraphics, mouseY, mouseY, partial)

        if (type.get() != null) {
            drawBlockIcon(
                guiGraphics,
                type.get()!!.icon(),
                x = this.x + 2,
                y = this.y + 2,
                width = 28,
                height = 28
            )
        } else {
            drawBlockIcon(
                guiGraphics,
                redstone.icon(),
                x = this.x + 12,
                y = this.y + 12,
                width = 18,
                height = 18
            )
            drawBlockIcon(
                guiGraphics,
                fluid.icon(),
                x = this.x + 7,
                y = this.y + 7,
                width = 18,
                height = 18
            )
            drawBlockIcon(
                guiGraphics,
                me.icon(),
                x = this.x + 2,
                y = this.y + 2,
                width = 18,
                height = 18
            )
        }
    }

    private fun updateHoverText() {
        messages[0] =
            if (type.get() == null) {
                Component.translatable(
                    "gui.advanced_memory_card.types.filtered",
                    Component.translatable("gui.advanced_memory_card.types.any")
                )
            } else {
                Component.translatable(
                    "gui.advanced_memory_card.types.filtered",
                    (type.get()!!.stack.displayName as MutableComponent).withStyle(
                        ChatFormatting.GREEN
                    ),
                )
            }
    }

    fun commitType() {
        updateHoverText()
        PacketDistributor.sendToServer(C2SRefreshP2PList(type.get()?.index ?: TUNNEL_ANY))
        playDownSound(Minecraft.getInstance().soundManager)
    }

    override fun accept(type: ClientTunnelInfo?) {
        val screen = Minecraft.getInstance().screen
        if (screen is GuiAdvancedMemoryCard) {
            screen.closeTypeSelector(type)
        }
    }
}
