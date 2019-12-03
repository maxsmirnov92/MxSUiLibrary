package net.maxsmr.android.recyclerview.adapters

import android.content.Context
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.bejibx.android.recyclerview.selection.HolderClickListener
import com.bejibx.android.recyclerview.selection.SelectionHelper
import com.bejibx.android.recyclerview.selection.SelectionListener
import java.util.*

abstract class BaseMultiSelectionRecyclerViewAdapter<I, VH : BaseRecyclerViewAdapter.ViewHolder<*>>(
        context: Context,
        @LayoutRes baseItemLayoutId: Int = 0,
        items: Collection<I>? = null
) : BaseSelectionRecyclerViewAdapter<I, VH, BaseMultiSelectionRecyclerViewAdapter.ItemSelectedChangeListener>(
        context, baseItemLayoutId, items
), HolderClickListener, SelectionListener {

    override val itemSelectedObservable = ItemSelectedObservable()

    private val selectionHelper: SelectionHelper by lazy {
        SelectionHelper().apply {
            registerSelectionObserver(this@BaseMultiSelectionRecyclerViewAdapter)
            registerHolderClickObserver(this@BaseMultiSelectionRecyclerViewAdapter)
        }
    }

    override var isSelectable: Boolean
        get() = selectionHelper.isSelectable
        set(toggle) {
            selectionHelper.isSelectable = toggle
        }

    val isTogglingSelectionAllowed: Boolean =
            selectionHelper.isTogglingSelectionAllowed

    val selectedItems: Map<Int, I?>
        get() {
            val selectedItems = mutableMapOf<Int, I?>()
            val selectedPositions = selectedItemsPositions
            for (pos in selectedPositions) {
                selectedItems[pos] = getItem(pos)
            }
            return selectedItems
        }

    val unselectedItems: Map<Int, I?>
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
            val unselectedPositions = LinkedHashSet<Int>()
            val selectedPositions = selectedItemsPositions
            for (pos in 0 until itemCount) {
                if (!selectedPositions.contains(pos)) {
                    unselectedPositions.add(pos)
                }
            }
            return unselectedPositions
        }

    val selectedItemsCount: Int
        get() = if (itemCount > 0) selectionHelper.selectedItemsCount else 0

    @CallSuper
    override fun bindSelection(holder: VH, item: I?, position: Int) {
        selectionHelper.wrapSelectable(holder, getSelectModesForItem(item, position))

        val isSelected = isItemPositionSelected(position)
        handleSelected(getSelectableView(holder), isSelected)

        if (isSelected) {
            onHandleItemSelected(holder, item, position)
        } else {
            onHandleItemNotSelected(holder, item, position)
        }
    }

    override fun onViewRecycled(holder: VH) {
        super.onViewRecycled(holder)
        selectionHelper.recycleHolder(holder)
    }

    @CallSuper
    override fun release() {
        super.release()
        releaseSelectionHelper()
        itemSelectedObservable.unregisterAll()
    }

    override fun isItemPositionSelected(position: Int): Boolean {
        rangeCheck(position)
        return selectionHelper.isItemSelected(position)
    }

    @CallSuper
    override fun onSelectedChanged(holder: RecyclerView.ViewHolder, isSelected: Boolean, fromUser: Boolean) {
        val position = getListPosition(holder.adapterPosition)
        if (position in 0 until itemCount) {
            itemSelectedObservable.notifyItemSelected(position, isSelected, fromUser)
            if (allowNotifyOnChange)
                notifyItemChanged(position)
        }
    }

    @CallSuper
    override fun onReselected(holder: RecyclerView.ViewHolder, fromUser: Boolean) {
        val position = getListPosition(holder.adapterPosition)
        if (position in 0 until itemCount) {
            itemSelectedObservable.notifyItemReselected(position, fromUser)
        }
    }

    @CallSuper
    override fun onSelectableChanged(isSelectable: Boolean) {
        // call notify if it needed due to ViewHolder logic or something
        itemSelectedObservable.notifySelectableChanged(isSelectable)
    }

    @CallSuper
    override fun onAllowTogglingSelectionChanged(isAllowed: Boolean) {
        // call notify if it needed due to ViewHolder logic or something
        itemSelectedObservable.notifyAllowTogglingSelectionChanged(isAllowed)
    }

    override fun onHolderClick(holder: RecyclerView.ViewHolder) {
        // do nothing
    }

    override fun onHolderLongClick(holder: RecyclerView.ViewHolder): Boolean {
        // do nothing
        return false
    }

    override fun invalidateSelectionIndexOnAdd(to: Int, count: Int) {
        val targetSelected = LinkedHashSet<Int>()
        val targetUnselected = LinkedHashSet<Int>()
        if (count >= 1) {
            for (selection in selectedItemsPositions) {
                if (selection != RecyclerView.NO_POSITION && selection >= to) {
                    targetUnselected.add(selection)
                    val targetSelectedPosition = selection + count
                    if (targetSelectedPosition in 0 until itemCount) {
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
        setItemsSelectedByPositions(targetSelected, true)
    }

    override fun invalidateSelectionIndexOnRemove(from: Int, count: Int) {
        if (count >= 1) {
            val targetSelected = LinkedHashSet<Int>()
            val targetUnselected = LinkedHashSet<Int>()
            for (selection in selectedItemsPositions) {
                if (selection != RecyclerView.NO_POSITION) {
                    if (selection >= from && selection < from + count) {
                        if (selection in 0 until itemCount) {
                            targetUnselected.add(selection)
                        }
                    } else if (selection >= from + count) {
                        targetUnselected.add(selection)
                        val targetSelection = selection - count
                        if (targetSelection in 0 until itemCount) {
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
            setItemsSelectedByPositions(targetSelected, true)
        }
    }

    override fun clearSelection() {
        if (selectionHelper.selectedItemsCount > 0) {
            selectionHelper.clearSelection(false)
        }
    }

    fun setAllowTogglingSelection(toggle: Boolean) {
        selectionHelper.setAllowTogglingSelection(toggle)
    }

    fun setItemsSelectedByPositions(positions: Collection<Int>?, isSelected: Boolean) =
            setItemsSelectedByPositions(positions, isSelected, true)


    fun setItemSelectedByPosition(position: Int, isSelected: Boolean): Boolean {
        rangeCheck(position)
        return selectionHelper.setItemSelectedByPosition(position, isSelected, false)
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
        return selectionHelper.toggleItemSelectedByPosition(position, false)
    }

    fun setItemsSelected(items: Collection<I>?, isSelected: Boolean): Boolean {
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

    fun setItemSelected(item: I, isSelected: Boolean) =
            setItemsSelected(listOf(item), isSelected)

    fun toggleItemsSelected(items: Collection<I>?): Boolean {
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

    fun toggleItemSelected(item: I) =
            toggleItemsSelected(listOf(item))

    protected fun releaseSelectionHelper() {
        selectionHelper.clear()
//        selectionHelper.unregisterSelectionObserver(this)
//        selectionHelper.unregisterHolderClickObserver(this)
    }


    protected fun invalidateSelections() {
        val targetUnselected = LinkedHashSet<Int>()
        for (selection in selectedItemsPositions) {
            if (selection != RecyclerView.NO_POSITION
                    && selection !in 0 until itemCount) {
                targetUnselected.add(selection)
            }
        }
        setItemsSelectedByPositions(targetUnselected, false, false)
    }

    private fun setItemsSelectedByPositions(positions: Collection<Int>?, isSelected: Boolean, needRangeCheck: Boolean): Boolean {
        if (positions != null) {
            for (pos in positions) {
                if (needRangeCheck || isSelected) {
                    rangeCheck(pos)
                }
            }
        }
        return selectionHelper.setItemsSelectedByPositions(positions, isSelected, false)
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

        fun notifySelectableChanged(isSelectable: Boolean) {
            synchronized(mObservers) {
                for (l in mObservers) {
                    l.onSelectableChanged(isSelectable)
                }
            }
        }

        fun notifyAllowTogglingSelectionChanged(isAllowed: Boolean) {
            synchronized(mObservers) {
                for (l in mObservers) {
                    l.onAllowTogglingSelectionChanged(isAllowed)
                }
            }
        }

    }

    interface ItemSelectedChangeListener : BaseItemSelectedChangeListener {

        fun onItemSelected(position: Int, isSelected: Boolean, fromUser: Boolean)

        fun onItemReselected(position: Int, fromUser: Boolean)

        fun onSelectableChanged(isSelectable: Boolean)

        fun onAllowTogglingSelectionChanged(isAllowed: Boolean)
    }
}
