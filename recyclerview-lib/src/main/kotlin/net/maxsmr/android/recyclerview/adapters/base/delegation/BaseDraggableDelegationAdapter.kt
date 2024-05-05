package net.maxsmr.android.recyclerview.adapters.base.delegation

import android.database.Observable
import android.view.View
import androidx.annotation.CallSuper
import androidx.annotation.IdRes
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.adapterdelegates4.AbsListItemAdapterDelegate
import com.hannesdorfmann.adapterdelegates4.AsyncListDifferDelegationAdapter
import com.hannesdorfmann.adapterdelegates4.dsl.v2.AdapterDelegateViewHolder
import net.maxsmr.android.recyclerview.adapters.base.drag.ITouchHelperAdapter
import net.maxsmr.android.recyclerview.adapters.base.drag.OnMotionTouchListener
import net.maxsmr.android.recyclerview.adapters.base.drag.OnStartDragListener

open class BaseDraggableDelegationAdapter<Data : BaseAdapterData>(
    vararg adapters: AbsListItemAdapterDelegate<Data, Data, DragAndDropViewHolder<Data>>,
) : AsyncListDifferDelegationAdapter<Data>(itemCallback(), *adapters), ITouchHelperAdapter {

    val isEmpty get() = itemCount == 0

    protected val itemsEventsObservable = ItemsEventsObservable<Data>()

    var startDragListener: OnStartDragListener? = null
        set(value) {
            if (field != value) {
                field = value
                if (!isEmpty) {
                    notifyDataSetChanged()
                }
            }
        }

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: List<*>) {
        super.onBindViewHolder(holder, position)

        holder as? DragAndDropViewHolder<Data>
            ?: throw IllegalArgumentException("Incorrect holder type: ${holder.javaClass.name}, must be: ${DragAndDropViewHolder::class.java.name}")

        // clear previous if was set by this adapter
        holder.motionTouchListener?.let {
            holder.draggableView?.setOnTouchListener(null)
            holder.motionTouchListener = null
        }

        val item = getItem(position)

        val touchListener = startDragListener
        if (touchListener != null && canDragItem(item, position) && holder.canDragItem(position, item)) {
            OnMotionTouchListener(holder, touchListener, holder.itemView.context).let {
                holder.draggableView?.setOnTouchListener(it)
                holder.motionTouchListener = it
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        holder as? DragAndDropViewHolder<Data>
            ?: throw IllegalArgumentException("Incorrect holder type: ${holder.javaClass.name}, must be: ${DragAndDropViewHolder::class.java.name}")
        holder.onViewRecycled()
    }

    override fun isDismissible(position: Int): Boolean = canDragItem(getItem(position), position)

    override fun isDraggable(position: Int): Boolean = canDragItem(getItem(position), position)

    override fun onItemDismiss(position: Int) {
        if (isDismissible(position)) {
            removeItem(position)
        }
    }

    override fun onItemMove(from: Int, to: Int): Boolean {
        if (!isDraggable(from) || !isDraggable(to)) {
            return false
        }
        moveItem(from, to)
        return true
    }

    fun registerItemsEventsListener(listener: ItemsEventsListener<Data>) {
        itemsEventsObservable.registerObserver(listener)
    }

    fun unregisterItemsEventsListener(listener: ItemsEventsListener<Data>) {
        itemsEventsObservable.unregisterObserver(listener)
    }

    @Throws(IndexOutOfBoundsException::class)
    fun getItem(at: Int): Data {
        rangeCheck(at)
        return items[at]
    }

    @Throws(IndexOutOfBoundsException::class)
    fun removeItem(position: Int): Data {
        rangeCheck(position)
        val newList = differ.currentList.toMutableList()
        val removedItem = newList.removeAt(position)
        differ.submitList(newList)
        onItemRemoved(position, removedItem)
        return removedItem
    }

    @Throws(IndexOutOfBoundsException::class)
    fun moveItem(from: Int, to: Int): Boolean {
        rangeCheck(from)
        rangeCheck(to)
        if (from == to) {
            return false
        }
        val newList = items.toMutableList()
        val item = newList.removeAt(from)
        val _to: Int = if (from < to) {
            to - 1
        } else {
            to
        }
        newList.add(_to, item)
        differ.submitList(newList)
        onItemsMoved(from, to, item)
        return true
    }

    protected open fun canDragItem(item: Data?, position: Int) = startDragListener != null

    @CallSuper
    protected open fun onItemRemoved(position: Int, item: Data) {
        itemsEventsObservable.notifyItemRemoved(position, item)
//        notifyItemRemoved(position)
    }

    @CallSuper
    protected open fun onItemsMoved(fromPosition: Int, toPosition: Int, item: Data) {
        itemsEventsObservable.notifyItemMoved(fromPosition, toPosition, item)
//        notifyItemMoved(fromPosition, toPosition)
    }

    protected fun rangeCheck(position: Int) {
        if (position < 0 || position >= itemCount) {
            throw IndexOutOfBoundsException("Incorrect position: $position")
        }
    }

    protected class ItemsEventsObservable<Data> : Observable<ItemsEventsListener<Data>>() {

        fun notifyItemRemoved(position: Int, item: Data) {
            synchronized(mObservers) {
                for (l in mObservers) {
                    l.onItemRemoved(position, item)
                }
            }
        }

        fun notifyItemMoved(fromPosition: Int, toPosition: Int, item: Data) {
            synchronized(mObservers) {
                for (l in mObservers) {
                    l.onItemMoved(fromPosition, toPosition, item)
                }
            }
        }
    }

    interface ItemsEventsListener<I> {

        fun onItemRemoved(position: Int, item: I)

        fun onItemMoved(fromPosition: Int, toPosition: Int, item: I)
    }

    open class DragAndDropViewHolder<Data>(view: View) : AdapterDelegateViewHolder<Data>(view) {

        open val draggableView: View? = null

        var motionTouchListener: OnMotionTouchListener? = null

        open fun canDragItem(position: Int, data: Data) = true

        @CallSuper
        open fun onViewRecycled() {
            motionTouchListener = null
        }

        companion object {

            fun <Data> View.createWithDraggable(@IdRes viewResId: Int) =
                object : DragAndDropViewHolder<Data>(this@createWithDraggable) {

                    override val draggableView: View? = itemView.findViewById(viewResId)
                }
        }
    }
}