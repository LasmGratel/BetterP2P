package dev.lasm.betterp2p.client.gui

import dev.lasm.betterp2p.BetterP2P
import dev.lasm.betterp2p.network.data.P2PInfo
import dev.lasm.betterp2p.network.data.P2PLocation
import dev.lasm.betterp2p.util.p2p.ClientTunnelInfo
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

class InfoWrapper(info: P2PInfo) {
    var frequency: Short = info.frequency
        set(value) {
            if (error || value == 0.toShort()) {
                hoverInfo[4] =
                    Component.translatable("gui.advanced_memory_card.p2p_status.unbound")
                        .withStyle(ChatFormatting.RED)
            } else {
                hoverInfo[4] =
                    Component.translatable("gui.advanced_memory_card.p2p_status.bound")
                        .withStyle(ChatFormatting.GREEN)
            }
            field = value
        }

    val hasChannel = info.hasChannel
    val loc: P2PLocation = P2PLocation(info.pos, info.facing, info.dim)
    val output: Boolean = info.output
    val type: Int = info.type
    var name: String = info.name
    var error: Boolean = false

    /** The backing p2p icon/feature */
    var icon: ResourceLocation

    /** p2p frame */
    var overlay: ResourceLocation =
        ResourceLocation.fromNamespaceAndPath("ae2", "textures/part/p2p_tunnel_front.png")

    val description: Component

    val freqDisplay: Component =
        Component.translatable("item.betterp2p.advanced_memory_card.selected")
            .append(" ")
            .append(
                if (frequency != 0.toShort()) {
                    val hex: String =
                        buildString {
                                append((frequency.toUInt() shr 32).toString(16).uppercase())
                                append(frequency.toUInt().toString(16).uppercase())
                            }
                            .format4()
                    Component.literal(hex)
                } else {
                    Component.translatable("gui.advanced_memory_card.desc.not_set")
                }
            )

    val hoverInfo: MutableList<Component>

    val channels: Component? by lazy {
        if (info.channels >= 0) {
            Component.translatable("gui.advanced_memory_card.extra.channel", info.channels)
        } else {
            null
        }
    }

    init {
        val p2pType: ClientTunnelInfo =
            BetterP2P.proxy.getP2PFromIndex(info.type) as ClientTunnelInfo
        icon = p2pType.icon()
        description =
            Component.literal("Type: ")
                .append(p2pType.dispName)
                .append(" - ")
                .append(
                    if (output) {
                        Component.translatable("gui.advanced_memory_card.p2p_status.output")
                    } else {
                        Component.translatable("gui.advanced_memory_card.p2p_status.input")
                    }
                )

        val online = info.hasChannel
        hoverInfo =
            mutableListOf(
                Component.literal("P2P - ").withStyle(ChatFormatting.AQUA).append(p2pType.dispName),
                Component.translatable(
                        "gui.advanced_memory_card.pos",
                        info.pos.x,
                        info.pos.y,
                        info.pos.z
                    )
                    .withStyle(ChatFormatting.YELLOW),
                Component.translatable("gui.advanced_memory_card.side", info.facing.name)
                    .withStyle(ChatFormatting.YELLOW),
                Component.translatable(
                        "gui.advanced_memory_card.dim",
                        info.dim.location().toString()
                    )
                    .withStyle(ChatFormatting.YELLOW)
            )
        if (error || frequency == 0.toShort()) {
            hoverInfo.add(
                Component.translatable("gui.advanced_memory_card.p2p_status.unbound")
                    .withStyle(ChatFormatting.RED)
            )
        } else {
            hoverInfo.add(
                Component.translatable("gui.advanced_memory_card.p2p_status.bound")
                    .withStyle(ChatFormatting.GREEN)
            )
        }

        if (!online) {
            hoverInfo.add(
                Component.translatable("gui.advanced_memory_card.p2p_status.offline")
                    .withStyle(ChatFormatting.RED)
            )
        }
    }

    override fun hashCode(): Int {
        return loc.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (this.javaClass != other?.javaClass) return false
        other as InfoWrapper

        return this.loc == other.loc
    }
}

fun String.format4(): String {
    val format = StringBuilder()
    for (index in this.indices) {
        if (index % 4 == 0 && index != 0) {
            format.append(" ")
        }
        format.append(this[index])
    }
    return format.toString()
}
