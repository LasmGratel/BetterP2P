package dev.lasm.betterp2p.client.gui

import dev.lasm.betterp2p.BetterP2P
import dev.lasm.betterp2p.network.data.P2PInfo
import dev.lasm.betterp2p.network.data.P2PLocation
import dev.lasm.betterp2p.util.p2p.ClientTunnelInfo
import net.minecraft.client.resources.language.I18n
import net.minecraft.resources.ResourceLocation

class InfoWrapper(info: P2PInfo) {
    var frequency: Short = info.frequency
        set(value) {
            if (error || value == 0.toShort()) {
                hoverInfo[4] = "§c" + I18n.get("gui.advanced_memory_card.p2p_status.unbound")
            } else {
                hoverInfo[4] = "§a" + I18n.get("gui.advanced_memory_card.p2p_status.bound")
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

    val description: String

    val freqDisplay: String by lazy {
        buildString {
            append(I18n.get("item.betterp2p.advanced_memory_card.selected"))
            append(" ")
            if (frequency != 0.toShort()) {
                val hex: String =
                    buildString {
                            append((frequency.toUInt() shr 32).toString(16).uppercase())
                            append(frequency.toUInt().toString(16).uppercase())
                        }
                        .format4()
                append(hex)
            } else {
                append(I18n.get("gui.advanced_memory_card.desc.not_set"))
            }
        }
    }

    val hoverInfo: MutableList<String>

    val channels: String? by lazy {
        if (info.channels >= 0) {
            I18n.get("gui.advanced_memory_card.extra.channel", info.channels)
        } else {
            null
        }
    }

    init {
        val p2pType: ClientTunnelInfo =
            BetterP2P.proxy.getP2PFromIndex(info.type) as ClientTunnelInfo
        icon = p2pType.icon()
        description = buildString {
            append("Type: ")
            append(p2pType.dispName)
            append(" - ")
            if (output) {
                append(I18n.get("gui.advanced_memory_card.p2p_status.output"))
            } else {
                append(I18n.get("gui.advanced_memory_card.p2p_status.input"))
            }
        }
        val online = info.hasChannel
        hoverInfo =
            mutableListOf(
                "§bP2P - ${p2pType.dispName}",
                "§e" + I18n.get("gui.advanced_memory_card.pos", info.pos.x, info.pos.y, info.pos.z),
                "§e" + I18n.get("gui.advanced_memory_card.side", info.facing.name),
                "§e" + I18n.get("gui.advanced_memory_card.dim", info.dim.location())
            )
        if (error || frequency == 0.toShort()) {
            hoverInfo.add("§c" + I18n.get("gui.advanced_memory_card.p2p_status.unbound"))
        } else {
            hoverInfo.add("§a" + I18n.get("gui.advanced_memory_card.p2p_status.bound"))
        }

        if (!online) {
            hoverInfo.add("§c" + I18n.get("gui.advanced_memory_card.p2p_status.offline"))
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
