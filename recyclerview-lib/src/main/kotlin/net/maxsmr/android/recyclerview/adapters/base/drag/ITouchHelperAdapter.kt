package net.maxsmr.android.recyclerview.adapters.base.drag

interface ITouchHelperAdapter {

    fun onItemMove(from: Int, to: Int): Boolean

    fun onItemDismiss(position: Int)

    fun isDraggable(position: Int): Boolean

    fun isDismissible(position: Int): Boolean
}