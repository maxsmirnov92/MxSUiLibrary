package net.maxsmr.android.recyclerview.adapters.base.drag

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.NO_POSITION

private const val DRAG_FLAGS = ItemTouchHelper.UP or ItemTouchHelper.DOWN
private const val SWIPE_FLAGS = ItemTouchHelper.START or ItemTouchHelper.END

open class DragAndDropTouchHelperCallback(
        private val adapter: ITouchHelperAdapter
) : ItemTouchHelper.Callback() {

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        val position = viewHolder.adapterPosition
        val dragFlag: Int
        val swipeFlag: Int
        if (position == NO_POSITION) {
            dragFlag = 0
            swipeFlag = 0
        } else {
            dragFlag = if (adapter.isDraggable(position)) DRAG_FLAGS else 0
            swipeFlag = if (adapter.isDismissible(position)) SWIPE_FLAGS else 0
        }
        return makeMovementFlags(dragFlag, swipeFlag)
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        val position = viewHolder.adapterPosition
        val targetPosition = target.adapterPosition
        return position != NO_POSITION && targetPosition != NO_POSITION && adapter.onItemMove(position, targetPosition)
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        if (position != NO_POSITION) {
            adapter.onItemDismiss(position)
        }
    }

    override fun isLongPressDragEnabled(): Boolean = false

    override fun isItemViewSwipeEnabled(): Boolean = false
}