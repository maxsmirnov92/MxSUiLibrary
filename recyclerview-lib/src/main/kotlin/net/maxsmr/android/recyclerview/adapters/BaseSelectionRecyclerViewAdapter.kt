package net.maxsmr.android.recyclerview.adapters

import android.content.Context
import android.database.Observable
import android.view.View
import android.widget.Checkable
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import com.bejibx.android.recyclerview.selection.SelectionHelper.SelectMode
import com.bejibx.android.recyclerview.selection.SelectionHelper.SelectMode.CLICK
import com.bejibx.android.recyclerview.selection.SelectionHelper.SelectMode.LONG_CLICK

abstract class BaseSelectionRecyclerViewAdapter<I, VH : BaseRecyclerViewAdapter.ViewHolder<*>, L: BaseSelectionRecyclerViewAdapter.BaseItemSelectedChangeListener>(
        context: Context,
        @LayoutRes
        baseItemLayoutId: Int = 0,
        items: Collection<I>? = null
) : BaseRecyclerViewAdapter<I, VH>(context, baseItemLayoutId, items) {

    protected abstract val itemSelectedObservable: BaseItemSelectedObservable<L>

    // notify required here
    val selectModes = mutableSetOf<SelectMode>()
//        get() = field.toMutableSet()

    abstract var isSelectable: Boolean

    override fun allowSetClickListener(item: I?, position: Int): Boolean =
            super.allowSetClickListener(item, position)
                    && !getSelectModesForItem(item, position).contains(CLICK)


    override fun allowSetLongClickListener(item: I?, position: Int): Boolean =
            super.allowSetLongClickListener(item, position)
                    && !getSelectModesForItem(item, position).contains(LONG_CLICK)

    @CallSuper
    override fun bindItem(holder: VH, item: I?, position: Int) {
        super.bindItem(holder, item, position)
        bindSelection(holder, item, getListPosition(position))
    }

    override fun onItemAdded(to: Int, item: I?) {
        invalidateSelectionIndexOnAdd(to, 1)
        super.onItemAdded(to, item)
    }

    override fun onItemsAdded(to: Int, items: Collection<I>) {
        invalidateSelectionIndexOnAdd(to, items.size)
        super.onItemsAdded(to, items)
    }

    override fun onItemsSet() {
        clearSelection()
        super.onItemsSet()
    }

    override fun onItemRemoved(from: Int, item: I?) {
        invalidateSelectionIndexOnRemove(from, 1)
        super.onItemRemoved(from, item)
    }

    override fun onItemsRangeRemoved(from: Int, to: Int, previousSize: Int) {
        invalidateSelectionIndexOnRemove(from, if (from == to) 1 else to - from)
        super.onItemsRangeRemoved(from, to, previousSize)
    }

    fun registerItemSelectedChangeListener(listener: L) {
        itemSelectedObservable.registerObserver(listener)
    }

    fun unregisterItemSelectedChangeListener(listener: L) {
        itemSelectedObservable.unregisterObserver(listener)
    }

    fun setSelectModes(selectModes: Collection<SelectMode>?) {
        if (this.selectModes != selectModes) {
            this.selectModes.clear()
            if (selectModes != null) {
                this.selectModes.addAll(selectModes)
            }
            itemSelectedObservable.notifySelectModesChanged(this.selectModes)
            if (allowNotifyOnChange) {
                notifyDataSetChanged()
            }
        }
    }

    fun isItemSelected(item: I) =
            isItemPositionSelected(indexOf(item))

    protected abstract fun isItemPositionSelected(position: Int): Boolean

    protected abstract fun bindSelection(holder: VH, item: I?, position: Int)

    protected abstract fun invalidateSelectionIndexOnAdd(to: Int, count: Int)

    protected abstract fun invalidateSelectionIndexOnRemove(from: Int, count: Int)

    protected abstract fun clearSelection()

    protected open fun getSelectableView(holder: VH): View? = getClickableView(holder)

    // override if need various for each position
    protected open fun getSelectModesForItem(item: I?, position: Int): Set<SelectMode> = selectModes

    protected open fun handleSelected(selectableView: View?, isSelected: Boolean) {
        selectableView?.apply {
            if (this is Checkable) {
                this.isChecked = isSelected
            } else {
                this.isSelected = isSelected
            }
        }
    }

    protected open fun onHandleItemSelected(holder: VH, item: I?, position: Int) {
        // override if needed
    }

    protected fun onHandleItemNotSelected(holder: VH, item: I?, position: Int) {
        // override if needed
    }

    protected abstract class BaseItemSelectedObservable<L : BaseItemSelectedChangeListener> : Observable<L>() {

        fun notifySelectModesChanged(selectModes: Set<SelectMode>) {
            synchronized(mObservers) {
                for (l in mObservers) {
                    l.onSelectModesChanged(selectModes)
                }
            }
        }
    }

    interface BaseItemSelectedChangeListener {

        fun onSelectModesChanged(selectModes: Set<SelectMode>)
    }
}