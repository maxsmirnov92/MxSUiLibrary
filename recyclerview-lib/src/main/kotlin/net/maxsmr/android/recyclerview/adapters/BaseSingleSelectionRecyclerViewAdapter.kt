package net.maxsmr.android.recyclerview.adapters

import android.content.Context
import android.view.View
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.bejibx.android.recyclerview.selection.SelectionHelper
import com.bejibx.android.recyclerview.selection.SelectionHelper.SelectMode.CLICK
import com.bejibx.android.recyclerview.selection.SelectionHelper.SelectMode.LONG_CLICK

abstract class BaseSingleSelectionRecyclerViewAdapter<I, VH : BaseRecyclerViewAdapter.ViewHolder<I>> @JvmOverloads constructor(
        context: Context,
        @LayoutRes baseItemLayoutId: Int = 0,
        items: Collection<I>? = null
) : BaseSelectionRecyclerViewAdapter<I, VH, BaseSingleSelectionRecyclerViewAdapter.OnItemSelectedChangeListener>(
        context, baseItemLayoutId, items
) {

    override val itemSelectedObservable = ItemSelectedObservable()

    val isSelected: Boolean
        get() = selectedPosition != RecyclerView.NO_POSITION

    val selectedPosition: Int
        get() = if (targetSelectionPosition in 0 until itemCount) {
            targetSelectionPosition
        } else {
            RecyclerView.NO_POSITION
        }

    val selectedItem: I?
        get() {
            val selection = selectedPosition
            return if (selection != RecyclerView.NO_POSITION) {
                getItem(selection)
            } else {
                null
            }
        }

    override var isSelectable = true
        set(value) {
            if (value != field) {
                field = value
                if (!value) clearSelection()
            }
        }

    var allowResettingSelection = true

    private var targetSelectionPosition = RecyclerView.NO_POSITION

    override fun onItemsSet() {
        clearSelection()
        super.onItemsSet()
    }

    override fun release() {
        super.release()
        itemSelectedObservable.unregisterAll()
    }

    override fun isItemPositionSelected(position: Int): Boolean {
        rangeCheck(position)
        return targetSelectionPosition != RecyclerView.NO_POSITION && targetSelectionPosition == position
    }

    @CallSuper
    override fun bindSelection(holder: VH, item: I?, position: Int) {

        val isSelected = isItemPositionSelected(position)
        val selectableView = getSelectableView(holder)
        val clickableView = getClickableView(holder)

        if (clickableView != null) {
            val selectModes = getSelectModesForItem(item, position)
            if (selectModes.contains(CLICK)) {
                clickableView.setOnClickListener { v ->
                    changeSelectedStateFromUiNotify(position, isSelected, selectableView)
                    itemsEventsObservable.notifyItemClick(position, item)
                }

            }
            if (selectModes.contains(LONG_CLICK)) {
                clickableView.setOnLongClickListener { v ->
                    changeSelectedStateFromUiNotify(position, isSelected, selectableView)
                    return@setOnLongClickListener itemsEventsObservable.notifyItemLongClick(position, item)
                }
                false
            }
        }

        handleSelected(selectableView, isSelected)
        if (isSelected) {
            onHandleItemSelected(holder, item, position)
        } else {
            onHandleItemNotSelected(holder, item, position)
        }
    }

    override fun invalidateSelectionIndexOnAdd(to: Int, count: Int) {
        val currentCount = itemCount
        if (count >= 1 && to in 0 until currentCount) {
            var previousSelection = RecyclerView.NO_POSITION
            if (targetSelectionPosition != RecyclerView.NO_POSITION) {
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
            var previousSelection = RecyclerView.NO_POSITION
            if (targetSelectionPosition != RecyclerView.NO_POSITION) {
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

    override fun clearSelection() {
        clearSelection(false)
    }

    fun setSelectionModes(selectionModes: Collection<SelectionHelper.SelectMode>?) {
        if (this.selectModes != selectionModes) {
            this.selectModes.clear()
            if (selectionModes != null) {
                this.selectModes.addAll(selectionModes)
            }
            itemSelectedObservable.notifySelectModesChanged(this.selectModes)
            if (allowNotifyOnChange) {
                notifyDataSetChanged()
            }
        }
    }

    fun setSelectionByItem(item: I) {
        setSelection(indexOf(item))
    }

    fun setSelection(selection: Int) {
        setSelection(selection, false)
    }

    /**
     * @return true if selection changed, false - otherwise
     */
    fun setSelection(selection: Int, fromUser: Boolean): Boolean {
        if (isSelectable) {
            rangeCheck(selection)
            var previousSelection = targetSelectionPosition
            targetSelectionPosition = selection
            var isNewSelection = true
            if (previousSelection >= 0 && previousSelection < itemCount) {
                isNewSelection = targetSelectionPosition != previousSelection
            } else {
                previousSelection = RecyclerView.NO_POSITION
            }
            // calling it anyway to trigger reselect if needed
            onSelectionChanged(previousSelection, targetSelectionPosition, fromUser)
            if (isNewSelection) {
                if (allowNotifyOnChange) {
                    notifyItemChanged(selection)
                    if (previousSelection != RecyclerView.NO_POSITION) {
                        notifyItemChanged(previousSelection)
                    }
                }
                return true
            }
        }
        return false
    }

    fun toggleSelection(selection: Int) {
        toggleSelection(selection, false)
    }

    fun previousSelection(loop: Boolean): Boolean {
        var changed = false
        var selection = selectedPosition
        if (selection != RecyclerView.NO_POSITION) {
            if (selection >= 1) {
                setSelection(--selection)
                changed = true
            } else if (loop) {
                setSelection(itemCount - 1)
                changed = true
            }
        }
        return changed
    }

    fun nextSelection(loop: Boolean): Boolean {
        var changed = false
        var selection = selectedPosition
        if (selection != RecyclerView.NO_POSITION) {
            if (selection < itemCount - 1) {
                setSelection(++selection)
                changed = true
            } else if (loop) {
                setSelection(0)
                changed = true
            }
        }
        return changed
    }

    /**
     * called before [.notifyItemChanged]}
     */
    @CallSuper
    protected open fun onSelectionChanged(from: Int, to: Int, fromUser: Boolean) {
        if (to != RecyclerView.NO_POSITION) {
            if (from != to) {
                itemSelectedObservable.notifyItemSetSelection(from, to, fromUser)
            } else {
                itemSelectedObservable.notifyItemReselect(to, fromUser)
            }
        } else {
            itemSelectedObservable.notifyItemResetSelection(from, fromUser)
        }
    }

    protected fun changeSelectedStateFromUi(position: Int): Boolean {
        if (isSelectable) {
            if (position == targetSelectionPosition) {
                if (allowResettingSelection) {
                    clearSelection(true)
                } else {
                    // current state is selected, triggering reselect, state must be not changed
                    setSelection(position, true)
                    return false
                }
            } else {
                setSelection(position, true)
            }
            return true
        }
        return false
    }

    protected fun changeSelectedStateFromUiNotify(
            position: Int,
            wasSelected: Boolean,
            selectableView: View?
    ) {
        if (!changeSelectedStateFromUi(position)) {
            // если результат отрицательный - возвращаем в исходное состояние view (isSelected не изменился)
            handleSelected(selectableView, wasSelected)
        }
    }

    /**
     * @return true if was resetted, false - it was already not selected
     */
    private fun clearSelection(fromUser: Boolean): Boolean {
        if (isSelected) {
            var previousSelection = targetSelectionPosition
            targetSelectionPosition = RecyclerView.NO_POSITION
            if (previousSelection < 0 || previousSelection >= itemCount) {
                previousSelection = RecyclerView.NO_POSITION
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

    private fun toggleSelection(selection: Int, fromUser: Boolean) {
        rangeCheck(selection)
        if (targetSelectionPosition == selection) {
            clearSelection(fromUser)
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
