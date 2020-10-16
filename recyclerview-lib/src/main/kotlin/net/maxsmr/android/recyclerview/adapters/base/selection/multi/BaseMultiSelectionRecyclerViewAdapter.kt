package net.maxsmr.android.recyclerview.adapters.base.selection.multi

import android.content.Context
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView.NO_POSITION
import net.maxsmr.android.recyclerview.adapters.base.selection.BaseSelectionRecyclerViewAdapter
import net.maxsmr.android.recyclerview.adapters.itemcontroller.BaseSelectableItemController
import java.util.*

abstract class BaseMultiSelectionRecyclerViewAdapter<I, VH : BaseSelectionRecyclerViewAdapter.BaseSelectableViewHolder<I>>(
        context: Context,
        @LayoutRes baseItemLayoutId: Int = 0,
        items: Collection<I>? = null
) : BaseSelectionRecyclerViewAdapter<I, VH, BaseMultiSelectionRecyclerViewAdapter.ItemSelectedChangeListener>(
        context, baseItemLayoutId, items
), HolderClickListener, SelectionListener {

    override val hasSelected: Boolean get() = hasSelectedItems

    override val itemsSelectedObservable = ItemSelectedObservable()

    val firstSelectedItem: I?
        get() = selectedItems.firstOrNull()

    val firstUnselectedItem: I?
        get() = unselectedItems.firstOrNull()

    val lastSelectedItem: I?
        get() = selectedItems.lastOrNull()

    val lastUnselectedItem: I?
        get() = unselectedItems.lastOrNull()

    val hasSelectedItems: Boolean
        get() = selectedItemsMap.isNotEmpty()

    val hasUnselectedItems: Boolean
        get() = unselectedItemsMap.isNotEmpty()

    val selectedItemsMap: Map<Int, I?>
        get() {
            val selectedItems = mutableMapOf<Int, I?>()
            val selectedPositions = selectedItemsPositions
            for (pos in selectedPositions) {
                selectedItems[pos] = getItem(pos)
            }
            return selectedItems
        }

    val unselectedItemsMap: Map<Int, I?>
        get() {
            val unselectedItems = mutableMapOf<Int, I?>()
            val unselectedPositions = unselectedItemsPositions
            for (pos in unselectedPositions) {
                unselectedItems[pos] = getItem(pos)
            }
            return unselectedItems
        }

    val selectedItemsPositions: Set<Int>
        get() = selectionHelper.selectedItems

    val unselectedItemsPositions: Set<Int>
        get() {
            val unselectedPositions = mutableSetOf<Int>()
            val selectedPositions = selectedItemsPositions
            for (pos in 0 until listItemCount) {
                if (!selectedPositions.contains(pos)) {
                    unselectedPositions.add(pos)
                }
            }
            return unselectedPositions
        }

    val selectedItems: Collection<I?>
        get() = selectedItemsMap.values

    val unselectedItems: Collection<I?>
        get() = unselectedItemsMap.values

    val selectedItemsCount: Int
        get() = if (listItemCount > 0) selectionHelper.selectedItemsCount else 0

    private val selectionHelper: SelectionHelper by lazy {
        SelectionHelper(this@BaseMultiSelectionRecyclerViewAdapter, this@BaseMultiSelectionRecyclerViewAdapter)
    }

    override var isSelectable: Boolean
        get() = selectionHelper.isSelectable
        set(toggle) {
            selectionHelper.isSelectable = toggle
        }

    override var allowResetSelection: Boolean
        get() = selectionHelper.isResetSelectionAllowed
        set(toggle) {
            selectionHelper.setAllowResetSelection(toggle)
        }


    @CallSuper
    override fun bindSelection(holder: VH, item: I?, position: Int) {
        selectionHelper.wrapSelectable(
                holder,
                canSelectItemByClick(item, position),
                canSelectItemByLongClick(item, position)
        )
        super.bindSelection(holder, item, position)
    }

    override fun onViewRecycled(holder: VH) {
        super.onViewRecycled(holder)
        selectionHelper.recycleHolder(holder)
    }

    @CallSuper
    override fun release() {
        super.release()
        releaseSelectionHelper()
        itemsSelectedObservable.unregisterAll()
    }

    override fun isItemPositionSelected(position: Int): Boolean {
        rangeCheck(position)
        return selectionHelper.isItemSelected(position)
    }

    override fun getListPosition(adapterPosition: Int): Int {
        return super.getListPosition(adapterPosition)
    }

    override fun canSelectAtPosition(position: Int): Boolean = canSelectItem(getItem(position), position)

    @CallSuper
    override fun onSelectionChanged(position: Int, holder: ViewHolder<*>?, isSelected: Boolean, fromUser: Boolean) {
        itemsSelectedObservable.notifyItemSelected(position, isSelected, fromUser)
        if (allowNotifyOnChange) {
            var wasNotifiedManually = false
            if (holder is BaseSelectableViewHolder<*>) {
                holder.handleSelected(isSelected)
                wasNotifiedManually = true
            }
            notifyItemChangedInfiniteCheck(position, if (holder != null && wasNotifiedManually) setOf(holder.adapterPosition) else emptySet())
        }
    }

    @CallSuper
    override fun onReselected(position: Int, holder: ViewHolder<*>?, fromUser: Boolean) {
        itemsSelectedObservable.notifyItemReselected(position, fromUser)
    }

    @CallSuper
    override fun onSelectableChanged(isSelectable: Boolean) {
        itemsSelectedObservable.notifySelectableChanged(isSelectable)
    }

    @CallSuper
    override fun onAllowResetSelectionChanged(isAllowed: Boolean) {
        itemsSelectedObservable.notifyAllowResetSelectionChanged(isAllowed)
    }

    @Suppress("UNCHECKED_CAST")
    override fun handleSelected(holder: ViewHolder<*>, isSelected: Boolean) {
        if (holder is BaseSelectableItemController.BaseSelectableViewHolder<*>) {
            handleSelected(holder as VH, isSelected)
        }
    }

    override fun onHolderClick(position: Int, holder: ViewHolder<*>) {
        itemsEventsObservable.notifyItemClick(position, getItem(position))
    }

    override fun onHolderLongClick(position: Int, holder: ViewHolder<*>): Boolean {
        return itemsEventsObservable.notifyItemLongClick(position, getItem(position))
    }

    override fun invalidateSelectionIndexOnAdd(to: Int, count: Int) {
        val targetSelected = mutableSetOf<Int>()
        val targetUnselected = mutableSetOf<Int>()
        if (count >= 1) {
            for (selection in selectedItemsPositions) {
                if (selection != NO_POSITION && selection >= to) {
                    targetUnselected.add(selection)
                    val targetSelectedPosition = selection + count
                    if (targetSelectedPosition in 0 until listItemCount) {
                        targetSelected.add(targetSelectedPosition)
                    }
                }
            }
        }
        val it = targetUnselected.iterator()
        while (it.hasNext()) {
            val index = it.next()
            // unselect old if it's not clashes with target selected
            if (targetSelected.contains(index)) {
                it.remove()
            }
        }
        setItemsSelectedByPositions(targetUnselected, false, false)
        setItemsSelectedByPositions(targetSelected, true, false)
    }

    override fun invalidateSelectionIndexOnRemove(from: Int, count: Int) {
        if (count >= 1) {
            val targetSelected = mutableSetOf<Int>()
            val targetUnselected = mutableSetOf<Int>()
            for (selection in selectedItemsPositions) {
                if (selection != NO_POSITION) {
                    if (selection >= from && selection < from + count) {
                        if (selection in 0 until listItemCount) {
                            targetUnselected.add(selection)
                        }
                    } else if (selection >= from + count) {
                        targetUnselected.add(selection)
                        val targetSelection = selection - count
                        if (targetSelection in 0 until listItemCount) {
                            targetSelected.add(targetSelection)
                        }
                    }
                }
            }
            val it = targetUnselected.iterator()
            while (it.hasNext()) {
                val index = it.next()
                // unselect old if it's not clashes with target selected
                if (targetSelected.contains(index)) {
                    it.remove()
                }
            }
            setItemsSelectedByPositions(targetUnselected, false, false)
            setItemsSelectedByPositions(targetSelected, true, false)
        }
    }

    override fun invalidateSelectionIndexOnSwap(from: Int, to: Int) {
        val targetSelected = mutableListOf<Int>()
        val targetUnselected = mutableListOf<Int>()
        if (to in 0..listItemCount && from in 0..listItemCount) {
            val containsFrom = selectedItemsPositions.contains(from)
            val containsTo = selectedItemsPositions.contains(to)
			if (containsFrom && !containsTo 
					|| !containsFrom && containsTo) {
			if (containsFrom) {
                targetSelected.add(to)
                targetUnselected.add(from)
            }
            if (containsTo) {
                targetSelected.add(from)
                targetUnselected.add(to)
            }
			}
        }
        setItemsSelectedByPositions(targetUnselected, false, false)
        setItemsSelectedByPositions(targetSelected, true, false)
    }

    override fun resetSelection() {
        if (selectionHelper.selectedItemsCount > 0) {
            selectionHelper.clearSelection(false)
        }
    }

    fun setAllItemsSelected() =
            setAllItemsSelected(true)

    fun resetAllItemsSelection() =
            setAllItemsSelected(false)
    // resetSelection()

    fun setAllItemsSelected(toggle: Boolean): Boolean {
        var hasChanged = false
        items.forEachIndexed { index, item ->
            if (setItemSelectedByPosition(index, toggle)) {
                hasChanged = true
            }
        }
        return hasChanged
    }

    fun setItemsSelectedByPositions(positions: Collection<Int>?, isSelected: Boolean) =
            setItemsSelectedByPositions(positions, isSelected, true)

    fun setItemSelectedByPosition(position: Int, isSelected: Boolean): Boolean {
        rangeCheck(position)
        return selectionHelper.setItemSelectedByPosition(position, position, isSelected, false, true)
    }

    fun toggleItemsSelectedByPositions(positions: Collection<Int>?): Boolean {
        if (positions != null) {
            for (pos in positions) {
                rangeCheck(pos)
            }
        }
        return selectionHelper.toggleItemsSelectedByPositions(positions, false)
    }

    fun toggleItemSelectedByPosition(position: Int): Boolean {
        rangeCheck(position)
        return selectionHelper.toggleItemSelectedByPosition(position, position, false)
    }

    fun setItemsSelected(items: Collection<I?>?, isSelected: Boolean): Boolean {
        val positions = ArrayList<Int>()
        if (items != null) {
            for (item in items) {
                val index = indexOf(item)
                if (index > -1) {
                    positions.add(index)
                }
            }
        }
        return setItemsSelectedByPositions(positions, isSelected)
    }

    fun setItemSelected(item: I?, isSelected: Boolean) =
            setItemsSelected(listOf(item), isSelected)

    fun toggleAllItemsSelected() =
            toggleItemsSelected(items)

    fun toggleItemsSelected(items: Collection<I?>?): Boolean {
        val positions = ArrayList<Int>()
        if (items != null) {
            for (item in items) {
                val index = indexOf(item)
                if (index > -1) {
                    positions.add(index)
                }
            }
        }
        return toggleItemsSelectedByPositions(positions)
    }

    fun toggleItemSelected(item: I?) =
            toggleItemsSelected(listOf(item))

    protected fun releaseSelectionHelper() {
        selectionHelper.clear()
    }

    private fun setItemsSelectedByPositions(positions: Collection<Int>?, isSelected: Boolean, needRangeCheck: Boolean): Boolean =
            selectionHelper.setItemsSelectedByPositions(positions, isSelected, false, needRangeCheck)

    interface ItemSelectedChangeListener : BaseItemSelectedChangeListener {

        fun onItemSelected(position: Int, isSelected: Boolean, fromUser: Boolean)

        fun onItemReselected(position: Int, fromUser: Boolean)
    }

    protected class ItemSelectedObservable : BaseItemSelectedObservable<ItemSelectedChangeListener>() {

        fun notifyItemSelected(position: Int, isSelected: Boolean, fromUser: Boolean) {
            synchronized(mObservers) {
                for (l in mObservers) {
                    l.onItemSelected(position, isSelected, fromUser)
                }
            }
        }

        fun notifyItemReselected(position: Int, fromUser: Boolean) {
            synchronized(mObservers) {
                for (l in mObservers) {
                    l.onItemReselected(position, fromUser)
                }
            }
        }

    }
}
