package net.maxsmr.android.recyclerview.adapters.base.selection

import android.content.Context
import android.database.Observable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Checkable
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import com.bejibx.android.recyclerview.selection.SelectionHelper.SelectTriggerMode
import net.maxsmr.android.recyclerview.adapters.base.BaseRecyclerViewAdapter

abstract class BaseSelectionRecyclerViewAdapter<I, VH : BaseSelectionRecyclerViewAdapter.BaseSelectableViewHolder<I>, L : BaseSelectionRecyclerViewAdapter.BaseItemSelectedChangeListener>(
        context: Context,
        @LayoutRes
        baseItemLayoutId: Int = 0,
        items: Collection<I>? = null
) : BaseRecyclerViewAdapter<I, VH>(context, baseItemLayoutId, items) {

    abstract val hasSelected: Boolean

    protected abstract val itemsSelectedObservable: BaseItemSelectedObservable<L>

    abstract var isSelectable: Boolean

    abstract var allowResetSelection: Boolean

    // notify required here
    var selectTriggerModes = setOf<SelectTriggerMode>()
        set(value) {
            if (field != value) {
                field = value
                onSelectTriggerModesChanged(value)
            }
        }

    override fun allowSetClickListener(item: I?, position: Int): Boolean =
            !isItemEmpty(item, position)
                    && !getSelectTriggerModesForItem(item, position).contains(SelectTriggerMode.CLICK)


    override fun allowSetLongClickListener(item: I?, position: Int): Boolean =
            !isItemEmpty(item, position)
                    && !getSelectTriggerModesForItem(item, position).contains(SelectTriggerMode.LONG_CLICK)

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

    final override fun bindData(holder: VH, position: Int, item: I) {
        holder.bindData(position, item, itemCount, isItemPositionSelected(position))
    }

    final override fun bindEmptyData(holder: VH, position: Int, item: I?) {
        holder.bindEmptyData(position, item, itemCount, isItemPositionSelected(position))
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

    protected abstract fun invalidateSelectionIndexOnAdd(to: Int, count: Int)

    protected abstract fun invalidateSelectionIndexOnRemove(from: Int, count: Int)

    @CallSuper
    protected open fun bindSelection(holder: VH, item: I?, position: Int) {
        val isSelected = isItemPositionSelected(position)
        // not calling handleSelected(holder, isSelected) cause it's already in VH
        if (isSelected) {
            onHandleItemSelected(holder, item, position)
        } else {
            onHandleItemNotSelected(holder, item, position)
        }
    }

    protected open fun isSelectionAtPositionAllowed(position: Int) = isSelectable

    // override if need various for each position
    protected open fun getSelectTriggerModesForItem(item: I?, position: Int): Set<SelectTriggerMode> = selectTriggerModes

    @CallSuper
    protected open fun onSelectTriggerModesChanged(selectTriggerModes: Set<SelectTriggerMode>) {
        itemsSelectedObservable.notifySelectModesChanged(selectTriggerModes)
        if (allowNotifyOnChange) {
            notifyDataSetChanged()
        }
    }

    protected open fun handleSelected(holder: VH, isSelected: Boolean) {
        holder.handleSelected(isSelected)
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

    abstract class BaseSelectableViewHolder<I>(
            view: View
    ): BaseRecyclerViewAdapter.ViewHolder<I>(view) {

        open val selectableView: View? = itemView

        constructor(parent: ViewGroup, @LayoutRes layoutId: Int) :
                this(LayoutInflater.from(parent.context).inflate(layoutId, parent, false))

        final override fun bindData(position: Int, item: I, count: Int) {
            super.bindData(position, item, count)
        }

        final override fun bindEmptyData(position: Int, item: I?, count: Int) {
            super.bindEmptyData(position, item, count)
        }

        @CallSuper
        open fun bindData(position: Int, item: I, count: Int, isSelected: Boolean) {
            bindData(position, item, count)
            handleSelected(isSelected)
        }

        @CallSuper
        open fun bindEmptyData(position: Int, item: I?, count: Int, isSelected: Boolean) {
            bindEmptyData(position, item, count)
            handleSelected(isSelected)
        }

        open fun handleSelected(isSelected: Boolean) {
            // дефолтное поведение с selectableView, если есть
            selectableView?.let {
                if (it is Checkable) {
                    it.isChecked = isSelected
                } else {
                    it.isSelected = isSelected
                }
            }
        }
    }
}