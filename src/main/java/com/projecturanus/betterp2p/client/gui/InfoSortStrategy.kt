package com.projecturanus.betterp2p.client.gui

enum class InfoSortStrategy(val comparator: (InfoWrapper, InfoWrapper) -> Int) : Comparator<InfoWrapper> {
    DEFAULT({ o1, o2 ->
        o1.frequency.compareTo(o2.frequency)
    }),
    BY_FREQUENCY({ o1, o2 ->
        o1.frequency.compareTo(o2.frequency)
    }),
    ;

    override fun compare(o1: InfoWrapper, o2: InfoWrapper): Int = comparator(o1, o2)
}
