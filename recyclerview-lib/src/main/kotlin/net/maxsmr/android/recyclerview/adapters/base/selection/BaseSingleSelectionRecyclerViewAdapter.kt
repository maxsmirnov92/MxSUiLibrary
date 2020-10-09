package net.maxsmr.android.recyclerview.adapters.base.selection

import android.content.Context
import android.view.View
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView.NO_POSITION

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

    var selectedPosition: Int = NO_POSITION
        private set
        get() = if (field in 0 until listItemCount) {
            field
        } else {
            field = NO_POSITION
            field
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

    @CallSuper
    override fun release() {
        super.release()
        itemsSelectedObservable.unregisterAll()
    }

    override fun isItemPositionSelected(position: Int): Boolean {
        rangeCheck(position)
        return selectedPosition != NO_POSITION && selectedPosition == position
    }

    @CallSuper
    override fun bindSelection(holder: VH, item: I?, position: Int) {

        holder.clickableView?.let { view ->
            if (canSelectItemByClick(item, position)) {
                val listener = View.OnClickListener {
                    with(getListPosition(holder.adapterPosition)) {
                        changeSelectedStateFromUiNotify(this, holder)
                        itemsEventsObservable.notifyItemClick(this, item)
                    }
                }
                holder.clickListener = listener
                view.setOnClickListener(listener)
            }
        }

        holder.longClickableView?.let { view ->
            if (canSelectItemByLongClick(item, position)) {
                val listener = View.OnLongClickListener {
                    with(getListPosition(holder.adapterPosition)) {
                        changeSelectedStateFromUiNotify(this, holder)
                        itemsEventsObservable.notifyItemLongClick(this, item)
                    }
                }
                holder.longClickListener = listener
                view.setOnLongClickListener(listener)
            }
        }

        super.bindSelection(holder, item, position)
    }

    override fun invalidateSelectionIndexOnAdd(to: Int, count: Int) {
        if (count >= 1 && to in 0 until listItemCount) {
            var shouldNotify = false
            var previousSelection = NO_POSITION
            if (selectedPosition != NO_POSITION) {
                if (selectedPosition >= to) {
                    previousSelection = selectedPosition
                    selectedPosition += count
                    shouldNotify = true
                }
            }
            if (shouldNotify) {
                onSelectionChanged(previousSelection, selectedPosition, false)
            }
        }
    }

    override fun invalidateSelectionIndexOnRemove(from: Int, count: Int) {
        if (from in 0..listItemCount && count >= 1) {
            var shouldNotify = false
            var previousSelection = NO_POSITION
            if (selectedPosition != NO_POSITION) {
                if (selectedPosition >= from && selectedPosition < from + count) {
                    previousSelection = selectedPosition
                    selectedPosition = NO_POSITION
                    shouldNotify = true
                } else if (selectedPosition >= from + count) {
                    previousSelection = selectedPosition
                    selectedPosition -= count
                    shouldNotify = true
                }
            }
            if (shouldNotify) {
                onSelectionChanged(previousSelection, selectedPosition, false)
            }
        }
    }

    override fun invalidateSelectionIndexOnSwap(from: Int, to: Int) {
        if (from in 0..listItemCount && to in 0..listItemCount) {
            var shouldNotify = false
            var previousSelection = NO_POSITION
            if (selectedPosition != NO_POSITION) {
                when (selectedPosition) {
                    from -> {
                        selectedPosition = to
                        previousSelection = from
                        shouldNotify = true
                    }
                    to -> {
                        selectedPosition = from
                        previousSelection = to
                        shouldNotify = true
                    }
                }
            }
            if (shouldNotify) {
                onSelectionChanged(previousSelection, selectedPosition, false)
            }
        }
    }

    override fun resetSelection() {
        resetSelection(false)
    }

    fun setSelectionByItem(item: I): Boolean {
        val index = indexOf(item)
        if (index in 0 until listItemCount) {
            return setSelection(index)
        }
        return false
    }

    /**
     * @return true if selection changed, false - otherwise
     */
    @JvmOverloads
    fun setSelection(
            selection: Int,
            fromUser: Boolean = false,
            notifySelectionChangedAction: (() -> Int)? = null
    ): Boolean {
        rangeCheck(selection)
        if (canSelectItem(getItem(selection), selection)) {
            val previousSelection = selectedPosition
            selectedPosition = selection
            val isNewSelection = selection != previousSelection
            // calling it anyway to trigger reselect if needed
            onSelectionChanged(previousSelection, selection, fromUser, notifySelectionChangedAction)
            if (isNewSelection) {
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
                setSelection(listItemCount - 1)
                changed = true
            }
        }
        return changed
    }

    fun nextSelection(loop: Boolean): Boolean {
        var changed = false
        var selection = selectedPosition
        if (selection != NO_POSITION) {
            if (selection < listItemCount - 1) {
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

    @CallSuper
    protected open fun onSelectionChanged(from: Int, to: Int, fromUser: Boolean, notifySelectionChangedAction: (() -> Int)? = null) {
        val isNewSelection = from != to
        if (to != NO_POSITION) {
            if (isNewSelection) {
                itemsSelectedObservable.notifyItemSetSelection(from, to, fromUser)
                if (allowNotifyOnChange) {
                    // позиция, для которой только что произошло обычное или кастомное оповещение
                    val notifiedPosition =
                            if (notifySelectionChangedAction == null) {
                                notifyItemChanged(to)
                                to
                            } else {
                                notifySelectionChangedAction()
                            }
                    notifyItemsChangedForInfiniteScroll(to, setOf(notifiedPosition))
                }
            } else {
                itemsSelectedObservable.notifyItemReselect(to, fromUser)
            }
        } else {
            itemsSelectedObservable.notifyItemResetSelection(from, fromUser)
        }
        if (isNewSelection) {
            notifyItemChangedInfiniteCheck(from)
        }
    }

    protected fun changeSelectedStateFromUiNotify(position: Int, holder: VH) {
        val wasSelected = isItemPositionSelected(position)
        if (!changeSelectedStateFromUi(position, notifySelectionChangedAction = {
                    // вместо обычного notify пользуем handleSelected на адаптере
                    // + позиция должна быть из холдера (например, с учётом infinite scroll)
                    handleSelected(holder, isItemPositionSelected(position))
                    holder.adapterPosition // позиция, для которой ui был изменён вручную
                })) {
            // если результат отрицательный - возвращаем в исходное состояние view (isSelected не изменился)
            handleSelected(holder, wasSelected)
        }
    }

    protected fun changeSelectedStateFromUi(position: Int, notifySelectionChangedAction: (() -> Int)? = null): Boolean {
        if (canSelectItem(getItem(position), position)) {
            return if (position == selectedPosition) {
                if (allowResetSelection) {
                    resetSelection(true)
                } else {
                    // current state is selected, triggering reselect, state must be not changed
                    setSelection(position, true, notifySelectionChangedAction)
                    false
                }
            } else {
                setSelection(position, true, notifySelectionChangedAction)
            }
        }
        return false
    }

    /**
     * @return true if was resetted, false - it was already not selected
     */
    private fun resetSelection(fromUser: Boolean): Boolean {
        if (hasSelected) {
            val previousSelection = selectedPosition
            selectedPosition = NO_POSITION
            onSelectionChanged(previousSelection, selectedPosition, fromUser)
            return true
        }
        return false
    }

    private fun toggleSelection(
            selection: Int,
            fromUser: Boolean,
            notifySelectionChangedAction: (() -> Int)? = null
    ): Boolean {
        rangeCheck(selection)
        return if (selectedPosition == selection) {
            resetSelection(fromUser)
        } else {
            setSelection(selection, fromUser, notifySelectionChangedAction)
        }
    }

    interface OnItemSelectedChangeListener : BaseItemSelectedChangeListener {

        fun onItemSetSelection(fromIndex: Int, toIndex: Int, fromUser: Boolean)

        fun onItemResetSelection(index: Int, fromUser: Boolean)

        fun onItemReselect(index: Int, fromUser: Boolean)
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
}
