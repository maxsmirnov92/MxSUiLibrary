package net.maxsmr.android.recyclerview.adapters.base.drag

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

/**
 * Listener for manual initiation of a drag.
 */
open class OnStartDragHelperListener(protected val helper: ItemTouchHelper): OnStartDragListener {

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
        helper.startDrag(viewHolder)
    }

    override fun onStartSwipe(viewHolder: RecyclerView.ViewHolder) {
        helper.startSwipe(viewHolder)
    }
}