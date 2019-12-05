package net.maxsmr.android.recyclerview.adapters

import android.content.Context
import android.database.Observable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.NO_ID
import net.maxsmr.android.recyclerview.adapters.diff.AutoNotifyDiffCallback
import net.maxsmr.android.recyclerview.adapters.diff.ItemInfo
import java.util.*

private const val INFINITE_SCROLL_LOOPS_COUNT = 100

abstract class BaseRecyclerViewAdapter<I, VH : BaseRecyclerViewAdapter.ViewHolder<*>>(
        protected val context: Context,
        @LayoutRes
        protected val baseItemLayoutId: Int = 0,
        items: Collection<I>? = null
) : RecyclerView.Adapter<VH>() {

    val firstItem: I?
        get() = if (isNotEmpty) {
            getItem(0)
        } else null

    val lastItem: I?
        get() {
            return if (isNotEmpty) {
                getItem(itemCount - 1)
            } else null
        }

    val isEmpty: Boolean
        get() = itemCount == 0

    val isNotEmpty: Boolean
        get() = !isEmpty

    val items = mutableListOf<I?>()
//        get() = field.toMutableList()

    protected val itemsEventsObservable = ItemsEventsObservable<I, VH>()

    /**
     * Маппинг позиция в адаптере <-> LayoutRes разметки в этой позиции
     */
    var viewTypeLayoutResMap = mapOf<Int, Int>()

    /**
     * Разрешить обновление элементов в адаптере
     * при изменении данных
     */
    var allowNotifyOnChange = true

    /**
     * Разрешить выборочное обновление элементов в адаптере
     * по позициям с изменившимися данными
     */
    var allowDiffNotifyOnChange = true

    var allowInfiniteScroll: Boolean = false

    protected var pendingFocusPosition = RecyclerView.NO_POSITION

    private var lastItemsInfo = listOf<ItemInfo>()

    init {
        setItems(items, false)
    }

    abstract override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH

    override fun getItemCount() = if (allowInfiniteScroll) INFINITE_SCROLL_LOOPS_COUNT * items.size else items.size;

    @CallSuper
    override fun onViewRecycled(holder: VH) {
        super.onViewRecycled(holder)
        holder.onViewRecycled()
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val targetPosition = getListPosition(position)
        val item = if (targetPosition in 0 until items.size) items[targetPosition] else null
        bindItem(holder, item, targetPosition)
    }

    /**
     * @see RecyclerView.Adapter.getItemId
     */
    override fun getItemId(position: Int): Long =
            getItemStringId(position).hashCode().toLong()

    /**
     * Get the unique id from item at certain position
     *
     * @param position position of item
     * @return unique item id
     */
    open fun getItemStringId(position: Int): String = NO_ID.toString()

    /**
     * Get the item's hashcode at certain position
     *
     * @param position position of item
     * @return item's hashcode
     */
    open fun getItemHash(position: Int): String {
        val item = items[getListPosition(position)]
        return (item?.hashCode() ?: 0).toString()
    }

    @CallSuper
    open fun release() {
        itemsEventsObservable.unregisterAll()
        items.clear()
    }

    fun registerItemsEventsListener(listener: ItemsEventsListener<I>) {
        itemsEventsObservable.registerObserver(listener)
    }

    fun unregisterItemsEventsListener(listener: ItemsEventsListener<I>) {
        itemsEventsObservable.unregisterObserver(listener)
    }


    @Throws(IndexOutOfBoundsException::class)
    fun getItem(at: Int): I? {
        synchronized(items) {
            rangeCheck(at)
            return items[at]
        }
    }

    fun indexOf(item: I?): Int =
            synchronized(items) {
            return items.indexOf(item)
        }

    fun lastIndexOf(item: I): Int =
            synchronized(items) {
            items.lastIndexOf(item)
        }


    /**
     * Запросить фокус в указанном [index];
     * если не входит в текущий диапазон,
     * то только присвоить для дальнейших bind
     */
    fun requestFocus(index: Int) {
        pendingFocusPosition = index
        if (allowNotifyOnChange) {
            if (index in 0 until itemCount) {
                notifyItemChanged(index)
            }
        }
    }

    /**
     * @param items null for reset adapter
     */
    fun setItems(items: Collection<I>?, shouldNotify: Boolean = true) {
        if (shouldSetItems(items)) {
            clearItems()
            if (items != null) {
                this.items.addAll(items)
            }
            if (shouldNotify) {
                onItemsSet()
            }
        }
    }

    fun clearItems() {
        if (isNotEmpty) {
            val previousSize = itemCount
            items.clear()
            onItemsRangeRemoved(0, previousSize - 1, previousSize)
        }
    }

    @Throws(IndexOutOfBoundsException::class)
    fun addItem(to: Int, item: I?) {
        synchronized(items) {
            rangeCheckForAdd(to)
            items.add(to, item)
            onItemAdded(to, item)
        }
    }

    fun addItem(item: I?) {
        addItem(itemCount, item)
    }

    @Throws(IndexOutOfBoundsException::class)
    fun addFirstItem(item: I?) {
        addItem(0, item)
    }

    fun addItems(to: Int, items: Collection<I>?) {
        synchronized(this.items) {
            rangeCheckForAdd(to)
            if (items != null) {
                this.items.addAll(to, items)
                onItemsAdded(to, items)
            }
        }
    }

    fun addItems(items: Collection<I>?) {
        addItems(itemCount, items)
    }

    fun setItem(`in`: Int, item: I?) {
        rangeCheck(`in`)
        items[`in`] = item
        onItemSet(`in`, item)
    }

    fun replaceItem(`in`: Int, newItem: I?): I? {
        synchronized(items) {
            rangeCheck(`in`)
            allowNotifyOnChange = false
            val replacedItem = getItem(`in`)
            items.removeAt(`in`)
            onItemRemoved(`in`, replacedItem)
            addItem(`in`, newItem)
            allowNotifyOnChange = true
            notifyItemChanged(`in`)
            return replacedItem
        }
    }

    fun replaceItem(replaceableItem: I?, newItem: I?): I? {
        return replaceItem(indexOf(replaceableItem), newItem)
    }

    fun replaceItemsRange(from: Int, to: Int, newItems: Collection<I>?): List<I?> {
        allowNotifyOnChange = false
        val replacedItems = removeItemsRange(from, to)
        addItems(newItems)
        allowNotifyOnChange = true
        notifyItemRangeChanged(from, to - from)
        return replacedItems
    }

    fun removeItem(item: I?): I? {
        return removeItem(indexOf(item))
    }

    fun removeItem(from: Int): I? {
        rangeCheck(from)
        val removedItem = getItem(from)
        items.removeAt(from)
        onItemRemoved(from, removedItem)
        return removedItem
    }

    fun removeItemsRange(from: Int, to: Int): List<I?> {
        rangeCheck(from)
        rangeCheck(to)
        val previousSize = itemCount
        val removed = mutableListOf<I?>()
        var position = 0
        val iterator = items.iterator()
        while (iterator.hasNext()) {
            if (position in from..to) {
                val item = iterator.next()
                iterator.remove()
                removed.add(item)
            }
            position++
        }
        if (removed.isNotEmpty()) {
            onItemsRangeRemoved(from, to, previousSize)
        }
        return removed
    }

    fun removeAllItems() {
        if (isNotEmpty) {
            removeItemsRange(0, itemCount - 1)
        }
    }

    protected open fun areItemsEqual(items: Collection<I>?) = this.items == items

    // override to disable equals check on items set
    protected open fun shouldSetItems(items: Collection<I>?): Boolean = !areItemsEqual(items)

    protected open fun onInflateView(parent: ViewGroup, viewType: Int): View =
            LayoutInflater.from(parent.context)
                    .inflate(getLayoutIdForViewType(viewType), parent, false)

    protected open fun getClickableView(holder: VH): View? =
            holder.itemView

    protected open fun getLongClickableView(holder: VH): View? =
            getClickableView(holder)

    protected open fun getFocusableView(holder: VH): View? =
            getClickableView(holder)

    @LayoutRes
    protected open fun getLayoutIdForViewType(viewType: Int): Int =
            viewTypeLayoutResMap[viewType] ?: baseItemLayoutId

    @CallSuper
    protected open fun onItemSet(`in`: Int, item: I?) {
        itemsEventsObservable.notifyItemSet(`in`, item)
        if (allowNotifyOnChange) {
            notifyItemChanged(`in`)
        }
    }

    @CallSuper
    protected open fun onItemsAdded(to: Int, items: Collection<I>) {
        itemsEventsObservable.notifyItemsAdded(to, items)
        if (allowNotifyOnChange) {
            if (to == 0) {
                notifyDataSetChanged()
            } else {
                notifyItemRangeInserted(to, items.size)
            }
        }
    }

    @CallSuper
    protected open fun onItemAdded(to: Int, item: I?) {
        itemsEventsObservable.notifyItemAdded(to, item)
        if (allowNotifyOnChange) {
            if (to == 0) {
                notifyDataSetChanged()
            } else {
                notifyItemInserted(to)
            }
        }
    }

    @CallSuper
    protected open fun onItemsRangeRemoved(from: Int, to: Int, previousSize: Int) {
        itemsEventsObservable.notifyItemsRangeRemoved(from, to, previousSize)
        if (allowNotifyOnChange) {
            notifyItemRangeRemoved(from, to - from)
        }
        if (from == 0 && to == previousSize - 1) {
            onItemsCleared(previousSize)
        }
    }

    @CallSuper
    protected open fun onItemRemoved(from: Int, item: I?) {
        itemsEventsObservable.notifyItemRemoved(from, item)
        if (allowNotifyOnChange) {
            notifyItemRemoved(from)
        }
    }

    protected open fun onItemsCleared(previousSize: Int) {
        // override if needed
    }

    protected open fun onItemsSet() {
        itemsEventsObservable.notifyItemsSet(items)
        if (allowNotifyOnChange) {
            if (allowDiffNotifyOnChange) {
                diffNotifyDataSetChanged()
            } else {
                notifyDataSetChanged()
            }
        }
    }

    protected fun rangeCheck(position: Int) {
        if (position < 0 || position >= items.size) {
            throw IndexOutOfBoundsException("Incorrect position: $position")
        }
    }

    protected fun rangeCheckForAdd(position: Int) {
        if (position < 0 || position > items.size) {
            throw IndexOutOfBoundsException("Incorrect add position: $position")
        }
    }

    protected open fun allowSetClickListener(item: I?, position: Int) = !isItemEmpty(item, position)

    protected open fun allowSetLongClickListener(item: I?, position: Int) = allowSetClickListener(item, position)

    protected open fun allowFillHolderForItem(holder: VH, item: I?, position: Int) = true

    protected open fun allowBindFocusForItem(holder: VH, item: I?, position: Int) = pendingFocusPosition == position

    protected open fun isItemEmpty(item: I?, position: Int): Boolean = item == null

    @Suppress("UNCHECKED_CAST")
    @CallSuper
    protected open fun bindItem(holder: VH, item: I?, position: Int) {

        holder as ViewHolder<Any>

        val listPosition = getListPosition(position)
        val allowFill = allowFillHolderForItem(holder, item, listPosition)

        if (!isItemEmpty(item, listPosition) && item != null) {

            if (allowSetClickListener(item, listPosition)) {
                getClickableView(holder)?.setOnClickListener {
                    itemsEventsObservable.notifyItemClick(getListPosition(holder.adapterPosition), item)
                }
            }

            if (allowSetLongClickListener(item, listPosition)) {
                getLongClickableView(holder)?.setOnLongClickListener { itemsEventsObservable.notifyItemLongClick(holder.adapterPosition, item) }
            }

            if (allowFill) {
                holder.bindData(position, item, itemCount)
            }

        } else {

            if (allowFill) {
                holder.bindEmptyData(listPosition, item, itemCount)
            }
        }

        if (allowBindFocusForItem(holder, item, listPosition)) {
            bindItemFocus(holder, item, listPosition)
            itemsEventsObservable.notifyItemFocused(listPosition, item)
            pendingFocusPosition = RecyclerView.NO_POSITION
        }
    }

    /**
     * Реализация запроса фокуса на фокусабельной view
     */
    @CallSuper
    protected open fun bindItemFocus(holder: VH, item: I?, position: Int) {
        getFocusableView(holder)?.requestFocus()
    }

    /**
     * @return позиция с учётом включённости бесконечного скролла
     */
    protected fun getListPosition(adapterPosition: Int) = if (allowInfiniteScroll) {
        adapterPosition % items.size
    } else {
        adapterPosition
    }

    /**
     * Extract real items info, despite of infinite or ordinary scroll.
     */
    private fun extractRealItemInfo(): List<ItemInfo> {
        val itemCount = items.size
        val currentItemsInfo = ArrayList<ItemInfo>(itemCount)
        for (i in 0 until itemCount) {
            currentItemsInfo.add(
                    ItemInfo(getItemStringId(i),
                            getItemHash(i))
            )
        }
        return currentItemsInfo
    }

    /**
     * Automatically call necessary notify... methods.
     */
    private fun diffNotifyDataSetChanged() {
        val newItemInfo = extractRealItemInfo()
        val diffResult = DiffUtil.calculateDiff(
                AutoNotifyDiffCallback(lastItemsInfo,
                        newItemInfo,
                        allowInfiniteScroll)
        )
        diffResult.dispatchUpdatesTo(this)
        lastItemsInfo = newItemInfo
    }

    abstract class ViewHolder<I>(view: View) : RecyclerView.ViewHolder(view) {

        constructor(parent: ViewGroup, @LayoutRes layoutId: Int) :
                this(LayoutInflater.from(parent.context).inflate(layoutId, parent, false))

        protected val context: Context = view.context

        open fun bindData(position: Int, item: I, count: Int) {
            itemView.visibility = View.VISIBLE
        }

        open fun bindEmptyData(position: Int, item: I?, count: Int) {
            itemView.visibility = View.GONE
        }

        open fun onViewRecycled() {
            // override if needed
        }
    }

    protected class ItemsEventsObservable<I, VH : ViewHolder<*>> : Observable<ItemsEventsListener<I>>() {

        fun notifyItemClick(position: Int, item: I?) {
            synchronized(mObservers) {
                for (l in mObservers) {
                    l.onItemClick(position, item)
                }
            }
        }

        fun notifyItemLongClick(position: Int, item: I?): Boolean {
            synchronized(mObservers) {
                var consumed = false
                for (l in mObservers) {
                    if (l.onItemLongClick(position, item)) {
                        consumed = true
                    }
                }
                return consumed
            }
        }

        fun notifyItemFocused(position: Int, item: I?) {
            synchronized(mObservers) {
                for (l in mObservers) {
                    l.onItemFocusChanged(position, item)
                }
            }
        }

        fun notifyItemAdded(to: Int, item: I?) {
            synchronized(mObservers) {
                for (l in mObservers) {
                    l.onItemAdded(to, item)
                }
            }
        }

        fun notifyItemsAdded(to: Int, items: Collection<I>) {
            synchronized(mObservers) {
                for (l in mObservers) {
                    l.onItemsAdded(to, items)
                }
            }
        }

        fun notifyItemSet(to: Int, item: I?) {
            synchronized(mObservers) {
                for (l in mObservers) {
                    l.onItemSet(to, item)
                }
            }
        }

        fun notifyItemsSet(items: List<I?>) {
            synchronized(mObservers) {
                for (l in mObservers) {
                    l.onItemsSet(items)
                }
            }
        }

        fun notifyItemRemoved(from: Int, item: I?) {
            synchronized(mObservers) {
                for (l in mObservers) {
                    l.onItemRemoved(from, item)
                }
            }
        }

        fun notifyItemsRangeRemoved(from: Int, to: Int, previousSize: Int) {
            synchronized(mObservers) {
                for (l in mObservers) {
                    l.onItemsRangeRemoved(from, to, previousSize)
                }
            }
        }
    }

    interface ItemsEventsListener<I> {

        fun onItemClick(position: Int, item: I?)

        /**
         * @return true if event consumed
         */
        fun onItemLongClick(position: Int, item: I?): Boolean

        /**
         * Колбек при получении фокуса на указанную позицию
         */
        fun onItemFocusChanged(position: Int, item: I?)

        fun onItemAdded(to: Int, item: I?)

        fun onItemsAdded(to: Int, items: Collection<I>)

        fun onItemSet(to: Int, item: I?)

        fun onItemsSet(items: List<I?>)

        fun onItemRemoved(from: Int, item: I?)

        fun onItemsRangeRemoved(from: Int, to: Int, previousSize: Int)
    }
}
