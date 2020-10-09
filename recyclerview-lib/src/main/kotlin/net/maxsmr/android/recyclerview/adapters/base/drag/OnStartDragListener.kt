package net.maxsmr.android.recyclerview.adapters.base.drag

import androidx.recyclerview.widget.RecyclerView

/**
 * Listener for manual initiation of a drag.
 */
interface OnStartDragListener {
    /**
     * Called when a view is requesting a start of a drag.
     *
     * @param viewHolder The holder of the view to drag.
     */
    fun onStartDrag(viewHolder: RecyclerView.ViewHolder)

    fun onStartSwipe(viewHolder: RecyclerView.ViewHolder)
}