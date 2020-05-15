package net.maxsmr.android.recyclerview.adapters.base.selection

import android.content.Context
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView.NO_POSITION
import com.bejibx.android.recyclerview.selection.SelectionHelper.SelectTriggerMode.CLICK
import com.bejibx.android.recyclerview.selection.SelectionHelper.SelectTriggerMode.LONG_CLICK

abstract class BaseSingleSelectionRecyclerViewAdapter<I, VH : BaseSelectionRecyclerViewAdapter.BaseSelectableViewHolder<I>> @JvmOverloads constructor(
        context: Context,
        @LayoutRes baseItemLayoutId: Int = 0,
        items: Collection<I>? = null
) : BaseSelectionRecyclerViewAdapter<I, VH, BaseSingleSelectionRecyclerViewAdapter.OnItemSelectedChangeListener>(
        context, baseItemLayoutId, items
) {

    override val hasSelected: Boolean
        get() = selectedPosition != NO_POSITION

    override val itemsSelectedObservable = ItemSelectedObservable()

    val selectedPosition: Int
        get() = if (targetSelectionPosition in 0 until itemCount) {
            targetSelectionPosition
        } else {
            NO_POSITION
        }

    val selectedItem: I?
        get() {
            val selection = selectedPosition
            return if (selection != NO_POSITION) {
                getItem(selection)
            } else {
                null
            }
        }

    override var isSelectable = true
        set(value) {
            if (value != field) {
                field = value
                onSelectableChanged(value)
            }
        }

    override var allowResetSelection: Boolean = true
        set(value) {
            if (field != value) {
                field = value
                onAllowResetSelectionChanged(value)
            }
        }

    private var targetSelectionPosition = NO_POSITION
        set(value) {
            if (field != value) {
                field = value
                if (value >= itemCount) {
                    field = NO_POSITION
                }
            }
        }

    @CallSuper
    override fun onItemsSet() {
        resetSelection()
        super.onItemsSet()
    }

    @CallSuper
    override fun release() {
        super.release()
        itemsSelectedObservable.unregisterAll()
    }

    override fun isItemPositionSelected(position: Int): Boolean {
        rangeCheck(position)
        return targetSelectionPosition != NO_POSITION && targetSelectionPosition == getListPosition(position)
    }

    @CallSuper
    override fun bindSelection(holder: VH, item: I?, position: Int) {

        val clickableView = holder.clickableView
        val longClickableView = holder.longClickableView

        val selectTriggerModes = getSelectTriggerModesForItem(item, position)

        clickableView?.let {
            if (selectTriggerModes.contains(CLICK)) {
                it.setOnClickListener {
                    with(getListPosition(holder.adapterPosition)) {
                        changeSelectedStateFromUiNotify(this, holder)
                        itemsEventsObservable.notifyItemClick(this, item)
                    }
                }
            }
        }

        longClickableView?.let {
            if (selectTriggerModes.contains(LONG_CLICK)) {
                it.setOnLongClickListener {
                    with(getListPosition(holder.adapterPosition)) {
                        changeSelectedStateFromUiNotify(this, holder)
                        return@setOnLongClickListener itemsEventsObservable.notifyItemLongClick(this, item)
                    }
                }
            }
        }

        super.bindSelection(holder, item, position)
    }

    override fun invalidateSelectionIndexOnAdd(to: Int, count: Int) {
        val currentCount = itemCount
        if (count >= 1 && to in 0 until currentCount) {
            var previousSelection = NO_POSITION
            if (targetSelectionPosition != NO_POSITION) {
                if (targetSelectionPosition >= to) {
                    previousSelection = targetSelectionPosition
                    targetSelectionPosition += count
                }
            }
            if (allowNotifyOnChange) {
                if (previousSelection in 0 until currentCount) {
                    notifyItemChanged(previousSelection)
                }
                if (targetSelectionPosition != previousSelection && targetSelectionPosition >= 0 && targetSelectionPosition < currentCount) {
                    notifyItemChanged(targetSelectionPosition)
                }
            }
        }
    }

    override fun invalidateSelectionIndexOnRemove(from: Int, count: Int) {
        val currentCount = itemCount
        if (from in 0..currentCount && count >= 1) {
            var previousSelection = NO_POSITION
            if (targetSelectionPosition != NO_POSITION) {
                if (targetSelectionPosition >= from && targetSelectionPosition < from + count) {
                    previousSelection = targetSelectionPosition
                } else if (targetSelectionPosition >= from + count) {
                    previousSelection = targetSelectionPosition
                    targetSelectionPosition -= count
                }
            }
            if (allowNotifyOnChange) {
                if (previousSelection in 0 until currentCount) {
                    notifyItemChanged(previousSelection)
                }
                if (targetSelectionPosition != previousSelection && targetSelectionPosition >= 0 && targetSelectionPosition < currentCount) {
                    notifyItemChanged(targetSelectionPosition)
                }
            }
        }
    }

    override fun resetSelection() {
        resetSelection(false)
    }

    fun setSelectionByItem(item: I): Boolean {
        val index = indexOf(item)
        if (index in 0 until itemCount) {
            return setSelection(index)
        }
        return false
    }

    /**
     * @return true if selection changed, false - otherwise
     */
    @JvmOverloads
    fun setSelection(selection: Int, fromUser: Boolean = false): Boolean {
        rangeCheck(selection)
        if (isSelectionAtPositionAllowed(selection)) {
            var previousSelection = targetSelectionPosition
            targetSelectionPosition = selection
            var isNewSelection = true
            if (previousSelection in 0 until itemCount) {
                isNewSelection = targetSelectionPosition != previousSelection
            } else {
                previousSelection = NO_POSITION
            }
            // calling it anyway to trigger reselect if needed
            onSelectionChanged(previousSelection, targetSelectionPosition, fromUser)
            if (isNewSelection) {
                if (allowNotifyOnChange) {
                    notifyItemChanged(selection)
                    if (previousSelection != NO_POSITION) {
                        notifyItemChanged(previousSelection)
                    }
                }
                return true
            }
        }
        return false
    }

    fun toggleCurrentSelection() = with(selectedPosition) {
        if (this != NO_POSITION) {
            toggleSelection(this)
        } else {
            false
        }
    }

    fun toggleSelection(selection: Int): Boolean =
            toggleSelection(selection, false)

    fun previousSelection(loop: Boolean): Boolean {
        var changed = false
        var selection = selectedPosition
        if (selection != NO_POSITION) {
            if (selection >= 1) {
                setSelection(--selection)
                changed = true
            } else if (loop) {
                setSelection(items.size - 1)
                changed = true
            }
        }
        return changed
    }

    fun nextSelection(loop: Boolean): Boolean {
        var changed = false
        var selection = selectedPosition
        if (selection != NO_POSITION) {
            if (selection < items.size - 1) {
                setSelection(++selection)
                changed = true
            } else if (loop) {
                setSelection(0)
                changed = true
            }
        }
        return changed
    }

    @CallSuper
    protected open fun onSelectableChanged(isSelectable: Boolean) {
        if (!isSelectable) resetSelection()
        itemsSelectedObservable.notifySelectableChanged(isSelectable)
    }

    @CallSuper
    protected open fun onAllowResetSelectionChanged(isAllowed: Boolean) {
        itemsSelectedObservable.notifyAllowResetSelectionChanged(isAllowed)
    }

    /**
     * called before [notifyItemChanged]
     */
    @CallSuper
    protected open fun onSelectionChanged(from: Int, to: Int, fromUser: Boolean) {
        if (to != NO_POSITION) {
            if (from != to) {
                itemsSelectedObservable.notifyItemSetSelection(from, to, fromUser)
            } else {
                itemsSelectedObservable.notifyItemReselect(to, fromUser)
            }
        } else {
            itemsSelectedObservable.notifyItemResetSelection(from, fromUser)
        }
    }

    protected fun changeSelectedStateFromUi(position: Int): Boolean {
        if (isSelectionAtPositionAllowed(position)) {
            return if (position == targetSelectionPosition) {
                if (allowResetSelection) {
                    resetSelection(true)
                } else {
                    // current state is selected, triggering reselect, state must be not changed
                    setSelection(position, true)
                    false
                }
            } else {
                setSelection(position, true)
            }
        }
        return false
    }

    protected fun changeSelectedStateFromUiNotify(
            position: Int,
            holder: VH
    ) {
        val wasSelected = isItemPositionSelected(position)
        if (!changeSelectedStateFromUi(position)) {
            // если результат отрицательный - возвращаем в исходное состояние view (isSelected не изменился)
            handleSelected(holder, wasSelected)
        }
    }

    /**
     * @return true if was resetted, false - it was already not selected
     */
    private fun resetSelection(fromUser: Boolean): Boolean {
        if (hasSelected) {
            var previousSelection = targetSelectionPosition
            targetSelectionPosition = NO_POSITION
            if (previousSelection < 0 || previousSelection >= itemCount) {
                previousSelection = NO_POSITION
            }
            onSelectionChanged(previousSelection, targetSelectionPosition, fromUser)
            if (allowNotifyOnChange) {
                if (previousSelection in 0 until itemCount) {
                    notifyItemChanged(previousSelection)
                }
            }
            return true
        }
        return false
    }

    private fun toggleSelection(selection: Int, fromUser: Boolean): Boolean {
        rangeCheck(selection)
        return if (targetSelectionPosition == selection) {
            resetSelection(fromUser)
        } else {
            setSelection(selection, fromUser)
        }
    }

    protected class ItemSelectedObservable : BaseItemSelectedObservable<OnItemSelectedChangeListener>() {

        fun notifyItemSetSelection(fromIndex: Int, toIndex: Int, fromUser: Boolean) {
            synchronized(mObservers) {
                for (l in mObservers) {
                    l.onItemSetSelection(fromIndex, toIndex, fromUser)
                }
            }
        }

        fun notifyItemResetSelection(index: Int, fromUser: Boolean) {
            synchronized(mObservers) {
                for (l in mObservers) {
                    l.onItemResetSelection(index, fromUser)
                }
            }
        }

        fun notifyItemReselect(index: Int, fromUser: Boolean) {
            synchronized(mObservers) {
                for (l in mObservers) {
                    l.onItemReselect(index, fromUser)
                }
            }
        }
    }

    interface OnItemSelectedChangeListener : BaseItemSelectedChangeListener {

        fun onItemSetSelection(fromIndex: Int, toIndex: Int, fromUser: Boolean)

        fun onItemResetSelection(index: Int, fromUser: Boolean)

        fun onItemReselect(index: Int, fromUser: Boolean)
    }
}
