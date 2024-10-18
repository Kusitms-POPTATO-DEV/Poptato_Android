package com.poptato.ui.util

import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.abs

class DragDropListState(
    val lazyListState: LazyListState,
    val scope: CoroutineScope,
    private val onMove: (Int, Int) -> Unit
) {
    private var draggedDistance by mutableFloatStateOf(0f)
    private var initiallyDraggedElement by mutableStateOf<LazyListItemInfo?>(null)
    private val initialOffsets: Pair<Int, Int>?
        get() = initiallyDraggedElement?.let { Pair(it.offset, it.offset + it.size) }
    var currentIndexOfDraggedItem by mutableStateOf<Int?>(null)
    val elementDisplacement
        get() = currentIndexOfDraggedItem?.let {
            lazyListState.layoutInfo.visibleItemsInfo.getOrNull(it - lazyListState.layoutInfo.visibleItemsInfo.first().index)
        }?.let { item ->
            (initiallyDraggedElement?.offset ?: 0f).toFloat() + draggedDistance - item.offset
        }
    private val currentElement: LazyListItemInfo?
        get() = currentIndexOfDraggedItem?.let {
            lazyListState.layoutInfo.visibleItemsInfo.getOrNull(it - lazyListState.layoutInfo.visibleItemsInfo.first().index)
        }
    var overscrollJob by mutableStateOf<Job?>(null)

    fun onDragStart(offset: Offset) {
        lazyListState.layoutInfo.visibleItemsInfo.firstOrNull { item ->
            offset.y.toInt() in item.offset..(item.offset + item.size)
        }?.also {
            currentIndexOfDraggedItem = it.index
            initiallyDraggedElement = it
        }
    }

    fun onDragInterrupted() {
        draggedDistance = 0f
        currentIndexOfDraggedItem = null
        initiallyDraggedElement = null
        overscrollJob?.cancel()
    }

    fun onDrag(offset: Offset) {
        draggedDistance += offset.y

        initialOffsets?.let { (topOffset, bottomOffset) ->
            val startOffset = topOffset + draggedDistance
            val endOffset = bottomOffset + draggedDistance

            currentElement?.let { hovered ->
                lazyListState.layoutInfo.visibleItemsInfo.filterNot { item ->
                    item.offset + item.size <= startOffset || item.offset >= endOffset || hovered.index == item.index
                }.firstOrNull { item ->
                    when {
                        startOffset > hovered.offset -> (endOffset > item.offset + item.size / 2)
                        else -> (startOffset < item.offset + item.size / 2)
                    }
                }?.also { item ->
                    currentIndexOfDraggedItem?.let { current ->
                        onMove.invoke(current, item.index)
                    }
                    currentIndexOfDraggedItem = item.index
                }
            }
        }

        val overscroll = checkForOverScroll()
        if (overscroll != 0f) {
            scope.launch {
                lazyListState.scrollBy(overscroll * 0.05f)
            }
        }
    }

    fun checkForOverScroll(): Float {
        return initiallyDraggedElement?.let {
            val startOffset = it.offset + draggedDistance
            val endOffset = it.offset + it.size + draggedDistance

            return@let when {
                draggedDistance > 0 -> (endOffset - lazyListState.layoutInfo.viewportEndOffset).takeIf { diff -> diff > 0 }
                draggedDistance < 0 -> (startOffset - lazyListState.layoutInfo.viewportStartOffset).takeIf { diff -> diff < 0 }
                else -> null
            }
        } ?: 0f
    }
}

@Composable
fun rememberDragDropListState(
    lazyListState: LazyListState = rememberLazyListState(),
    onMove: (Int, Int) -> Unit,
): DragDropListState {
    val scope = rememberCoroutineScope()

    return remember {
        DragDropListState(
            lazyListState = lazyListState,
            onMove = onMove,
            scope = scope
        )
    }
}