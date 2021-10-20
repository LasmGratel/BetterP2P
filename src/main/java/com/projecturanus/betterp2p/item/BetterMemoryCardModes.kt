package com.projecturanus.betterp2p.item

enum class BetterMemoryCardModes {
    /**
     * Select an input P2P and bind its output
     */
    OUTPUT,

    /**
     * Select an output P2P and bind its input
     */
    INPUT,

    /**
     * Copy same output frequency
     */
    COPY;

    fun next(): BetterMemoryCardModes {
        return values()[ordinal.plus(1) % values().size]
    }
}
