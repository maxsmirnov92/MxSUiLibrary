package net.maxsmr.android.recyclerview.adapters

import android.content.Context
import android.database.Observable
import android.view.View
import android.widget.Checkable
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import com.bejibx.android.recyclerview.selection.SelectionHelper.SelectTriggerMode
import com.bejibx.android.recyclerview.selection.SelectionHelper.SelectTriggerMode.CLICK
import com.bejibx.android.recyclerview.selection.SelectionHelper.SelectTriggerMode.LONG_CLICK

abstract class BaseSelectionRecyclerViewAdapter<I, VH : BaseRecyclerViewAdapter.ViewHolder<*>, L : BaseSelectionRecyclerViewAdapter.BaseItemSelectedChangeListener>(
        context: Context,
        @LayoutRes
        baseItemLayoutId: Int = 0,
        items: Collection<I>? = null
) : BaseRecyclerViewAdapter<I, VH>(context, baseItemLayoutId, items) {

    protected abstract val itemsSelectedObservable: BaseItemSelectedObservable<L>

    // notify required here
    var selectTriggerModes = setOf<SelectTriggerMode>()
        set(value) {
            if (field != value) {
                field = value
                onSelectTriggerModesChanged(value)
            }
        }

    abstract var isSelectable: Boolean

    abstract var allowResetSelection: Boolean

    override fun allowSetClickListener(item: I?, position: Int): Boolean =
            !isItemEmpty(item, position)
                    && !getSelectTriggerModesForItem(item, position).contains(CLICK)


    override fun allowSetLongClickListener(item: I?, position: Int): Boolean =
            !isItemEmpty(item, position)
                    && !getSelectTriggerModesForItem(item, position).contains(LONG_CLICK)

    @CallSuper
    override fun bindItem(holder: VH, item: I?, position: Int) {
        super.bindItem(holder, item, position)
        bindSelection(holder, item, getListPosition(position))
    }

    @CallSuper
    override fun onItemAdded(to: Int, item: I?, previousSize: Int) {
        invalidateSelectionIndexOnAdd(to, 1)
        super.onItemAdded(to, item, previousSize)
    }

    @CallSuper
    override fun onItemsAdded(to: Int, items: Collection<I?>, previousSize: Int) {
        invalidateSelectionIndexOnAdd(to, items.size)
        super.onItemsAdded(to, items, previousSize)
    }

    @CallSuper
    override fun onItemsSet() {
        resetSelection()
        super.onItemsSet()
    }

    @CallSuper
    override fun onItemRemoved(from: Int, item: I?) {
        invalidateSelectionIndexOnRemove(from, 1)
        super.onItemRemoved(from, item)
    }

    @CallSuper
    override fun onItemsRangeRemoved(from: Int, to: Int, previousSize: Int, removedItems: List<I?>) {
        invalidateSelectionIndexOnRemove(from, if (from == to) 1 else to - from)
        super.onItemsRangeRemoved(from, to, previousSize, removedItems)
    }

    fun registerItemSelectedChangeListener(listener: L) {
        itemsSelectedObservable.registerObserver(listener)
    }

    fun unregisterItemSelectedChangeListener(listener: L) {
        itemsSelectedObservable.unregisterObserver(listener)
    }


    fun toggleSelectable() {
        isSelectable = !isSelectable
    }

    fun isItemSelected(item: I) =
            isItemPositionSelected(indexOf(item))

    abstract fun isItemPositionSelected(position: Int): Boolean

    abstract fun resetSelection()

    protected abstract fun bindSelection(holder: VH, item: I?, position: Int)

    protected abstract fun invalidateSelectionIndexOnAdd(to: Int, count: Int)

    protected abstract fun invalidateSelectionIndexOnRemove(from: Int, count: Int)

    protected open fun getSelectableView(holder: VH): View? = getClickableView(holder)

    // override if need various for each position
    protected open fun getSelectTriggerModesForItem(item: I?, position: Int): Set<SelectTriggerMode> = selectTriggerModes

    @CallSuper
    protected open fun onSelectTriggerModesChanged(selectTriggerModes: Set<SelectTriggerMode>) {
        itemsSelectedObservable.notifySelectModesChanged(selectTriggerModes)
        if (allowNotifyOnChange) {
            notifyDataSetChanged()
        }
    }

    protected open fun handleSelected(selectableView: View, isSelected: Boolean) {
            if (selectableView is Checkable) {
                selectableView.isChecked = isSelected
            } else {
                selectableView.isSelected = isSelected
            }
    }

    protected open fun onHandleItemSelected(holder: VH, item: I?, position: Int) {
        // override if needed
    }

    protected fun onHandleItemNotSelected(holder: VH, item: I?, position: Int) {
        // override if needed
    }

    protected abstract class BaseItemSelectedObservable<L : BaseItemSelectedChangeListener> : Observable<L>() {

        fun notifySelectModesChanged(selectTriggerModes: Set<SelectTriggerMode>) {
            synchronized(mObservers) {
                for (l in mObservers) {
                    l.onSelectModesChanged(selectTriggerModes)
                }
            }
        }

        fun notifySelectableChanged(isSelectable: Boolean) {
            synchronized(mObservers) {
                for (l in mObservers) {
                    l.onSelectableChanged(isSelectable)
                }
            }
        }

        fun notifyAllowResetSelectionChanged(isAllowed: Boolean) {
            synchronized(mObservers) {
                for (l in mObservers) {
                    l.onAllowResetSelectionChanged(isAllowed)
                }
            }
        }
    }

    interface BaseItemSelectedChangeListener {

        fun onSelectModesChanged(selectTriggerModes: Set<SelectTriggerMode>)

        fun onSelectableChanged(isSelectable: Boolean)

        fun onAllowResetSelectionChanged(isAllowed: Boolean)
    }
}