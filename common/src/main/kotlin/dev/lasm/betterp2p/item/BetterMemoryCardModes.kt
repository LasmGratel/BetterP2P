package dev.lasm.betterp2p.item

const val MAX_TOOLTIP_LENGTH = 40

enum class BetterMemoryCardModes(val unlocalizedName: String, vararg val unlocalizedDesc: String) {
    /**
     * Select an input P2P and bind its output
     */
    OUTPUT("gui.advanced_memory_card.mode.output",
        "gui.advanced_memory_card.mode.output.desc.1",
        "gui.advanced_memory_card.mode.output.desc.2",
        "gui.advanced_memory_card.mode.output.desc.3"
    ),

    /**
     * Select an output P2P and bind its input
     */
    INPUT("gui.advanced_memory_card.mode.input",
        "gui.advanced_memory_card.mode.input.desc.1",
        "gui.advanced_memory_card.mode.input.desc.2",
        "gui.advanced_memory_card.mode.input.desc.3"
    ),

    /**
     * Copy same output frequency
     */
    COPY("gui.advanced_memory_card.mode.copy",
    "gui.advanced_memory_card.mode.copy.desc.1",
    "gui.advanced_memory_card.mode.copy.desc.2",
    "gui.advanced_memory_card.mode.copy.desc.3",
    "gui.advanced_memory_card.mode.copy.desc.4"
    ),

    /**
     * Unbind/reset frequencies
     */
    UNBIND("gui.advanced_memory_card.mode.unbind",
    "gui.advanced_memory_card.mode.unbind.desc.1",
    "gui.advanced_memory_card.mode.unbind.desc.2");

    fun next(reverse: Boolean = false): BetterMemoryCardModes {
        if (reverse) {
            return if (ordinal - 1 < 0) {
                values()[values().size - 1]
            } else {
                values()[(ordinal - 1).rem(values().size)]
            }
        }
        return values()[(ordinal + 1).rem(values().size)]
    }
}
