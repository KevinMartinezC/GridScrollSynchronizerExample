package com.example.gridscrollsynchronizerexample.utils

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalCoroutinesApi::class)
class GridScrollSynchronizer {

    /**
     * A small data holder representing the "anchor" scroll position of a grid:
     * - [index]: first visible item index
     * - [offset]: pixel offset within that item
     */
    private data class Pos(val index: Int, val offset: Int)

    /** Last known leader position (non-null persists for followers). */
    private val leaderPos = MutableStateFlow<Pos?>(null)

    /**
     * Bind a [LazyGridState] to the synchronizer. The given [scope] is used to:
     * 1) Detect when *this* grid becomes the leader and publish positions.
     * 2) Follow positions published by others.
     * 3) Re-apply leader position when item count changes (e.g., data loads).
     */
    fun bind(state: LazyGridState, scope: CoroutineScope) {
        // 1) Publish positions while THIS grid is scrolling -> candidate leader.
        scope.launch {
            snapshotFlow { state.isScrollInProgress }
                .filter { it } // only while dragging/flinging
                .flatMapLatest {
                    // While scrolling, stream the first-visible item and its pixel offset.
                    snapshotFlow {
                        Pos(
                            index = state.firstVisibleItemIndex,
                            offset = state.firstVisibleItemScrollOffset
                        )
                    }
                }
                .distinctUntilChanged() // drop duplicates (same index+offset)
                .collect { leaderPos.value = it }
        }

        // 2) Follow the leader, with proper clamping
        scope.launch {
            val leaderFlow = leaderPos.filterNotNull()
            val countFlow = snapshotFlow { state.layoutInfo.totalItemsCount }
                .filter { it >= 0 }
                .distinctUntilChanged()
                .onStart { emit(state.layoutInfo.totalItemsCount) }

            combine(leaderFlow, countFlow) { pos, _ -> pos }
                .collect { pos -> state.scrollToClamped(pos) }
        }
    }

    private suspend fun LazyGridState.scrollToClamped(pos: Pos) {
        val maxIndex = max(0, layoutInfo.totalItemsCount - 1)
        val safeIndex = min(pos.index, maxIndex)
        if (safeIndex < 0) return

        val alreadyThere = firstVisibleItemIndex == safeIndex &&
                firstVisibleItemScrollOffset == pos.offset

        if (!alreadyThere) {
            scrollToItem(safeIndex, pos.offset)
        }
    }
}
