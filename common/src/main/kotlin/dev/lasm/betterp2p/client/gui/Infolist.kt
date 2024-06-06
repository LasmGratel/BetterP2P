package dev.lasm.betterp2p.client.gui

import dev.lasm.betterp2p.client.gui.widget.WidgetScrollBar
import dev.lasm.betterp2p.network.data.P2PLocation
import java.util.*
import kotlin.reflect.KProperty0

/**
 * InfoList Dedicated list to hold "InfoWrappers". Internally, stores them in a HashMap. This is an
 * opaque type, and access to it is restricted. External access is instead directed to a sorted view
 * and a filtered view of the internal map.
 */
class InfoList(initList: Collection<InfoWrapper>, private val search: KProperty0<String>) {

    /** The master map, acts as the source of truth for all items in this list. */
    private val masterMap: HashMap<P2PLocation, InfoWrapper> = hashMapOf()

    /** Sorted view of the master map. This is resorted whenever the map is updated. */
    val sorted: MutableList<InfoWrapper> = mutableListOf()

    /** Filtered view of the sorted view (not the master map) */
    var filtered: List<InfoWrapper> = listOf()

    private val filter: InfoFilter = InfoFilter()

    /** Binding to the search string in the text box */
    private val searchStr: String
        get() = search.get()

    val selectedInfo: InfoWrapper?
        get() {
            return if (selectedEntry == null) {
                null
            } else {
                masterMap[selectedEntry!!]
            }
        }

    var selectedEntry: P2PLocation? = null

    val size: Int
        get() = masterMap.size

    init {
        initList.forEach { masterMap[it.loc] = it }
    }

    fun resort() {
        sorted.sortBy {
            when {
                it.loc == selectedEntry -> {
                    -2 // Put the selected p2p in the front
                    // Non-Zero frequencies
                }
                it.frequency != 0.toShort() &&
                    it.frequency == selectedInfo?.frequency &&
                    !it.output -> {
                    -3 // Put input in the beginning
                }
                it.frequency != 0.toShort() && it.frequency == selectedInfo?.frequency -> {
                    -1 // Put same frequency in the front
                }
                else -> {
                    // Frequencies from lowest to highest
                    it.frequency + Short.MAX_VALUE
                }
            }
        }
    }

    /** Updates the filtered list. */
    fun refilter() {
        filter.updateFilter(searchStr.lowercase(Locale.getDefault()))
        filtered =
            sorted
                .filter {
                    if (it.loc == selectedEntry) {
                        return@filter true
                    }
                    for ((f, strs) in filter.activeFilters) {
                        if (!f.filter(it, strs?.toList())) {
                            return@filter false
                        }
                    }
                    true
                }
                .sortedBy {
                    when {
                        it.loc == selectedEntry -> Long.MIN_VALUE + 1
                        it.frequency != 0.toShort() &&
                            it.frequency == selectedInfo?.frequency &&
                            !it.output -> Long.MIN_VALUE
                        it.frequency != 0.toShort() && it.frequency == selectedInfo?.frequency ->
                            Long.MIN_VALUE + 2
                        filter.activeFilters.containsKey(Filter.NAME) -> {
                            var hits = 0L
                            var name = it.name
                            for (f in filter.activeFilters[Filter.NAME]!!) {
                                if (name.contains(f, true)) {
                                    hits += 1
                                    name = name.replaceFirst(f, "", true)
                                }
                            }
                            -(hits * hits) + name.length
                        }
                        else -> it.frequency + Long.MAX_VALUE - (if (it.output) 0 else 1)
                    }
                }
    }

    /** Updates the sorted list and applies the filter again. */
    fun refresh() {
        sorted.clear()
        sorted.addAll(masterMap.values)
        resort()
        refilter()
    }

    /** Completely refresh the master list. */
    fun rebuild(updateList: Collection<InfoWrapper>, scrollbar: WidgetScrollBar, numEntries: Int) {
        masterMap.clear()
        updateList.forEach { masterMap[it.loc] = it }
        sorted.clear()
        sorted.addAll(masterMap.values)
        resort()
        // TODO: Extend the filtering mechanism.
        refilter()
        scrollbar.setRange(
            0,
            masterMap.size.coerceIn(0, (masterMap.size - numEntries).coerceAtLeast(0)),
            23
        )
    }

    /** Update the master list, and send the changes downstream to sorted/filtered */
    fun update(updateList: Collection<InfoWrapper>, scrollbar: WidgetScrollBar, numEntries: Int) {
        updateList.forEach { masterMap[it.loc] = it }
        sorted.clear()
        sorted.addAll(masterMap.values)
        resort()
        // TODO: Extend the filtering mechanism.
        refilter()
        scrollbar.setRange(
            0,
            masterMap.size.coerceIn(0, (masterMap.size - numEntries).coerceAtLeast(0)),
            23
        )
    }

    fun select(which: P2PLocation?) {
        selectedEntry = masterMap.getOrDefault(which, null)?.loc
    }
}
