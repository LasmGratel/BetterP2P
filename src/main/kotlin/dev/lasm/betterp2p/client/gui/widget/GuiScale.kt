package dev.lasm.betterp2p.client.gui.widget

enum class GuiScale(
    val size: (Int) -> Int,
    val minHeight: Int,
    val unlocalizedName: String
) {
    SMALL({ 4 }, 0, "gui.advanced_memory_card.gui_scale.small"),
    NORMAL({ 6 }, 326, "gui.advanced_memory_card.gui_scale.normal"),
    LARGE({ 8 }, 409, "gui.advanced_memory_card.gui_scale.large"),
    DYNAMIC(
        { availableHeight -> availableHeight / P2PEntryConstants.HEIGHT },
        0,
        "gui.advanced_memory_card.gui_scale.dynamic"
    )
}
