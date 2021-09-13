package com.projecturanus.betterp2p.item

import appeng.api.implementations.items.IMemoryCard
import appeng.api.implementations.items.MemoryCardMessages
import appeng.api.parts.IPart
import appeng.api.util.AEColor
import appeng.core.localization.GuiText
import appeng.core.localization.PlayerMessages
import appeng.items.AEBaseItem
import appeng.parts.p2p.PartP2PTunnel
import appeng.util.Platform
import com.projecturanus.betterp2p.BetterP2P
import com.projecturanus.betterp2p.util.colorCode
import com.projecturanus.betterp2p.util.facingTile
import com.projecturanus.betterp2p.util.getPart
import net.minecraft.client.resources.I18n
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.ActionResult
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.TextComponentString
import net.minecraft.util.text.TextFormatting
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

private val DEFAULT_COLOR_CODE = arrayOf(
    AEColor.TRANSPARENT, AEColor.TRANSPARENT, AEColor.TRANSPARENT, AEColor.TRANSPARENT,
    AEColor.TRANSPARENT, AEColor.TRANSPARENT, AEColor.TRANSPARENT, AEColor.TRANSPARENT)

object ItemBetterMemoryCard : AEBaseItem(), IMemoryCard {
    init {
        setMaxStackSize(1)
    }

    @SideOnly(Side.CLIENT)
    override fun addCheckedInformation(stack: ItemStack, world: World, lines: MutableList<String>, advancedTooltips: ITooltipFlag) {
        lines.add(getLocalizedName(getSettingsName(stack) + ".name", getSettingsName(stack)))
        val data = getData(stack)
        if (data.hasKey("tooltip")) {
            lines.add(I18n.format(getLocalizedName(data.getString("tooltip") + ".name", data.getString("tooltip"))))
        }
        if (data.hasKey("freq")) {
            val freq = data.getShort("freq")
            val freqTooltip = TextFormatting.BOLD.toString() + Platform.p2p().toHexString(freq)
            lines.add(I18n.format("gui.tooltips.appliedenergistics2.P2PFrequency", freqTooltip))
        }
    }

    /**
     * Find the localized string...
     *
     * @param name possible names for the localized string
     *
     * @return localized name
     */
    @SideOnly(Side.CLIENT)
    private fun getLocalizedName(vararg name: String): String {
        for (n in name) {
            val l = I18n.format(n)
            if (l != n) {
                return l
            }
        }
        for (n in name) {
            return n
        }
        return ""
    }

    override fun setMemoryCardContents(itemStack: ItemStack, settingsName: String, data: NBTTagCompound) {
        val c = Platform.openNbtData(itemStack)
        c.setString("Config", settingsName)
        c.setTag("Data", data)
    }

    override fun getSettingsName(itemStack: ItemStack): String {
        val c = Platform.openNbtData(itemStack)
        val name = c?.getString("Config")
        return if (name == null || name.isEmpty()) GuiText.Blank.unlocalized else name
    }

    override fun getData(itemStack: ItemStack): NBTTagCompound {
        val c = Platform.openNbtData(itemStack)
        val o = c?.getCompoundTag("Data") ?: NBTTagCompound()
        return o.copy()
    }

    override fun getColorCode(itemStack: ItemStack): Array<AEColor> {
        val tag = getData(itemStack)
        if (tag.hasKey("colorCode")) {
            val frequency = tag.getIntArray("colorCode")
            val colorArray = AEColor.values()
            return arrayOf(
                colorArray[frequency[0]], colorArray[frequency[1]], colorArray[frequency[2]], colorArray[frequency[3]],
                colorArray[frequency[4]], colorArray[frequency[5]], colorArray[frequency[6]], colorArray[frequency[7]])
        }
        return DEFAULT_COLOR_CODE
    }

    override fun notifyUser(player: EntityPlayer, msg: MemoryCardMessages?) {
        if (Platform.isClient()) {
            return
        }
        when (msg) {
            MemoryCardMessages.SETTINGS_CLEARED -> player.sendMessage(PlayerMessages.SettingCleared.get())
            MemoryCardMessages.INVALID_MACHINE -> player.sendMessage(PlayerMessages.InvalidMachine.get())
            MemoryCardMessages.SETTINGS_LOADED -> player.sendMessage(PlayerMessages.LoadedSettings.get())
            MemoryCardMessages.SETTINGS_SAVED -> player.sendMessage(PlayerMessages.SavedSettings.get())
            MemoryCardMessages.SETTINGS_RESET -> player.sendMessage(PlayerMessages.ResetSettings.get())
            else -> {
            }
        }
    }

    /**
     * Return a list of p2p in the part's target grid
     * @param part Part to be detected
     * @param clazz P2P class type
     * @return a list of p2p tunnel in the target grid, or an empty list
     */
    fun listTargetGridP2P(part: IPart?, clazz: Class<PartP2PTunnel<*>>): List<PartP2PTunnel<*>> {
        val grid = part?.gridNode?.grid
        return grid?.getMachines(clazz)?.map { it.machine as PartP2PTunnel<*> }?.toList() ?: emptyList()
    }

    override fun onItemUse(player: EntityPlayer, w: World, pos: BlockPos, hand: EnumHand, side: EnumFacing, hx: Float, hy: Float, hz: Float): EnumActionResult {
        val part = getPart(w, pos, hx, hy, hz)
        if (part is PartP2PTunnel<*>) {
            player.sendMessage(TextComponentString("P2P Type: ${part.javaClass.simpleName}"))
            listTargetGridP2P(part, part.javaClass).forEach {
                player.sendMessage(TextComponentString("P2P: ${it.location.pos}, Frequency: ${it.frequency}, Output: ${it.isOutput}, Color: ${it.colorCode}, Facing: ${it.facingTile}"))
            }
            return EnumActionResult.SUCCESS
        }
        return EnumActionResult.PASS
    }

    override fun onItemRightClick(w: World, player: EntityPlayer, hand: EnumHand): ActionResult<ItemStack> {
        if (player.isSneaking) {
            if (!w.isRemote) {
                clearCard(player, w, hand)
            }
        }
        player.openGui(BetterP2P, 0, w, 0, 0, 0)
        return super.onItemRightClick(w, player, hand)
    }

    override fun doesSneakBypassUse(itemstack: ItemStack, world: IBlockAccess?, pos: BlockPos?, player: EntityPlayer?): Boolean {
        return true
    }

    private fun clearCard(player: EntityPlayer, w: World, hand: EnumHand) {
        val mem = player.getHeldItem(hand).item as IMemoryCard
        mem.notifyUser(player, MemoryCardMessages.SETTINGS_CLEARED)
        player.getHeldItem(hand).tagCompound = null
    }

}
