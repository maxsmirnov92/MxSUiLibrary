package net.maxsmr.android.recyclerview.adapters.base

import android.content.Context
import android.database.Observable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.annotation.MainThread
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.NO_ID
import net.maxsmr.android.recyclerview.adapters.diff.AutoNotifyDiffCallback
import net.maxsmr.android.recyclerview.adapters.diff.ItemInfo
import java.util.*

const val DEFAULT_INFINITE_SCROLL_LOOPS_COUNT = 100

@MainThread
abstract class BaseRecyclerViewAdapter<I, VH : BaseRecyclerViewAdapter.ViewHolder<I>>(
        protected val context: Context,
        @LayoutRes
        protected val baseItemLayoutId: Int = 0,
        items: Collection<I>? = null
) : RecyclerView.Adapter<VH>() {

    val listItemCount get() = items.size

    val firstItem: I?
        get() = if (isNotEmpty) {
            getItem(0)
        } else {
            null
        }

    val lastItem: I?
        get() {
            return if (isNotEmpty) {
                getItem(listItemCount - 1)
            } else {
                null
            }
        }

    val isEmpty: Boolean
        get() = listItemCount == 0

    val isNotEmpty: Boolean
        get() = !isEmpty

    val items = mutableListOf<I>()
//        get() = field.toMutableList()

    protected val itemsEventsObservable = ItemsEventsObservable<I, VH>()

    /**
     * Маппинг view type <-> LayoutRes для этого типа
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
     * (если [allowNotifyOnChange] true)
     */
    var allowDiffNotifyOnChange = true

    var allowInfiniteScroll: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                if (allowNotifyOnChange) {
                    notifyDataSetChanged()
                }
            }
        }

    var infiniteScrollLoopsCount: Int = DEFAULT_INFINITE_SCROLL_LOOPS_COUNT
        set(value) {
            require(value > 0) { "infiniteScrollLoopsCount cannot be less or equal zero: $value" }
            if (field != value) {
                field = value
                if (allowInfiniteScroll && allowNotifyOnChange) {
                    notifyDataSetChanged()
                }
            }
        }

    protected var pendingFocusPosition = RecyclerView.NO_POSITION

    private var lastItemsInfo = listOf<ItemInfo>()

    init {
        setItems(items, false)
    }

    abstract override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH

    override fun getItemCount() = if (allowInfiniteScroll) infiniteScrollLoopsCount * listItemCount else listItemCount

    @CallSuper
    override fun onViewRecycled(holder: VH) {
        super.onViewRecycled(holder)
        holder.onViewRecycled()
    }

    final override fun onBindViewHolder(holder: VH, position: Int) {
        val targetPosition = getListPosition(position)
        val item = if (targetPosition in 0 until listItemCount) items[targetPosition] else throw IndexOutOfBoundsException("Incorrect position: $position")
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

    fun toggleInfiniteScroll() {
        allowInfiniteScroll = !allowInfiniteScroll
    }

    @Throws(IndexOutOfBoundsException::class)
    fun getItem(at: Int): I {
        rangeCheck(at)
        return items[at]
    }

    fun getItemNoThrow(at: Int) =
            try {
                getItem(at)
            } catch (e: IndexOutOfBoundsException) {
                null
            }

    fun indexOf(item: I?): Int = items.indexOf(item)

    fun lastIndexOf(item: I): Int =
            items.lastIndexOf(item)

    /**
     * Запросить фокус в указанном [position];
     * если не входит в текущий диапазон,
     * то только присвоить для дальнейших bind
     */
    fun requestFocus(position: Int) {
        pendingFocusPosition = position
        if (allowNotifyOnChange) {
            if (position in 0 until listItemCount) {
                notifyItemChangedInfiniteCheck(position)
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
            val previousSize = listItemCount
            val clearedItems = items.toList()
            items.clear()
            onItemsRangeRemoved(0, previousSize - 1, previousSize, clearedItems)
        }
    }

    @Throws(IndexOutOfBoundsException::class)
    fun addItem(to: Int, item: I) {
        rangeCheckForAdd(to)
        val previousSize = listItemCount
        items.add(to, item)
        onItemAdded(to, item, previousSize)
    }

    fun addItem(item: I) {
        addItem(listItemCount, item)
    }

    @Throws(IndexOutOfBoundsException::class)
    fun addFirstItem(item: I) {
        addItem(0, item)
    }

    fun addItems(to: Int, items: Collection<I>?) {
        rangeCheckForAdd(to)
        if (items != null) {
            val previousSize = listItemCount
            this.items.addAll(to, items)
            onItemsAdded(to, items, previousSize)
        }
    }

    fun addItems(items: Collection<I>?) {
        addItems(listItemCount, items)
    }

    fun setItem(`in`: Int, item: I) {
        rangeCheck(`in`)
        items[`in`] = item
        onItemSet(`in`, item)
    }

    fun replaceItem(`in`: Int, newItem: I): I? {
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

    fun replaceItem(replaceableItem: I?, newItem: I): I? {
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
        val previousSize = listItemCount
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
            onItemsRangeRemoved(from, to, previousSize, removed)
        }
        return removed
    }

    fun removeAllItems() {
        if (isNotEmpty) {
            removeItemsRange(0, listItemCount - 1)
        }
    }

    protected open fun areItemsEqual(items: Collection<I>?) = this.items == items

    // override to disable equals check on items set
    protected open fun shouldSetItems(items: Collection<I>?): Boolean = !areItemsEqual(items)

    protected open fun onInflateView(parent: ViewGroup, viewType: Int): View =
            LayoutInflater.from(parent.context)
                    .inflate(getLayoutIdForViewType(viewType), parent, false)

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
    protected open fun onItemAdded(to: Int, item: I?, previousSize: Int) {
        itemsEventsObservable.notifyItemAdded(to, item, previousSize)
        if (allowNotifyOnChange) {
            if (to == 0 && previousSize == 0) {
                notifyDataSetChanged()
            } else {
                notifyItemInserted(to)
            }
        }
    }

    @CallSuper
    protected open fun onItemsAdded(to: Int, items: Collection<I?>, previousSize: Int) {
        itemsEventsObservable.notifyItemsAdded(to, items, previousSize)
        if (allowNotifyOnChange) {
            if (to == 0 && previousSize == 0) {
                notifyDataSetChanged()
            } else {
                notifyItemRangeInserted(to, listItemCount)
            }
        }
    }

    @CallSuper
    protected open fun onItemsRangeRemoved(from: Int, to: Int, previousSize: Int, removedItems: List<I?>) {
        itemsEventsObservable.notifyItemsRangeRemoved(from, to, previousSize, removedItems)
        if (allowNotifyOnChange) {
            notifyItemRangeRemoved(from, to - from)
        }
        val isCleared = from == 0 && to == previousSize - 1
        if (isCleared) {
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
        if (position < 0 || position >= listItemCount) {
            throw IndexOutOfBoundsException("Incorrect position: $position")
        }
    }

    protected fun rangeCheckForAdd(position: Int) {
        if (position < 0 || position > listItemCount) {
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

        // clear previous if was set by this adapter
        holder.clickListener?.let {
            holder.clickableView?.setOnClickListener { view ->
                view.setOnClickListener(null)
            }
            holder.clickListener = null
        }
        holder.longClickListener?.let {
            holder.longClickableView?.setOnClickListener { view ->
                view.setOnLongClickListener(null)
            }
            holder.clickListener = null
        }

        holder.clickableView?.let { view ->
            if (allowSetClickListener(item, listPosition)) {
                val listener = View.OnClickListener {
                    itemsEventsObservable.notifyItemClick(getListPosition(holder.adapterPosition), item)
                }
                view.setOnClickListener(listener)
                holder.clickListener = listener
            }
        }

        holder.longClickableView?.let { view ->
            if (allowSetLongClickListener(item, listPosition)) {
                val listener = View.OnLongClickListener {
                    itemsEventsObservable.notifyItemLongClick(getListPosition(holder.adapterPosition), item)
                }
                view.setOnLongClickListener(listener)
                holder.longClickListener = listener
            }
        }

        if (allowFillHolderForItem(holder, item, listPosition)) {
            if (item != null && !isItemEmpty(item, listPosition)) {
                bindData(holder, listPosition, item)
            } else {
                bindEmptyData(holder, listPosition, item)
            }
        }

        if (allowBindFocusForItem(holder, item, listPosition)) {
            bindItemFocus(holder, item, listPosition)
            itemsEventsObservable.notifyItemFocused(listPosition, item)
            pendingFocusPosition = RecyclerView.NO_POSITION
        }
    }

    protected open fun bindData(holder: VH, position: Int, item: I) {
        holder.bindData(position, item, listItemCount)
    }

    protected open fun bindEmptyData(holder: VH, position: Int, item: I?) {
        holder.bindEmptyData(position, item, listItemCount)
    }

    /**
     * Реализация запроса фокуса на фокусабельной view
     */
    @CallSuper
    protected open fun bindItemFocus(holder: VH, item: I?, position: Int) {
        holder.focusableView?.requestFocus()
    }

    /**
     * @return позиция с учётом включённости бесконечного скролла
     */
    protected open fun getListPosition(adapterPosition: Int) = if (allowInfiniteScroll) {
        adapterPosition % listItemCount
    } else {
        adapterPosition
    }

    protected fun notifyItemChangedInfiniteCheck(position: Int, excludedIndexes: Set<Int> = emptySet()) {
        if (position in 0 until listItemCount) {
            if (!allowInfiniteScroll) {
                notifyItemChanged(position)
            } else {
                notifyItemsChangedForInfiniteScroll(position, excludedIndexes)
            }
        }
    }

    /**
     * Оповещает об изменении selection'а все изменившиеся позиции в случае infinite scroll
     */
    protected fun notifyItemsChangedForInfiniteScroll(firstChangedPosition: Int, excludedIndexes: Set<Int> = emptySet()
    ): Set<Int> {
        val notifiedPositions = mutableSetOf<Int>()
        if (firstChangedPosition in 0 until listItemCount) {
            if (isNotEmpty && allowInfiniteScroll) {
                for (i in 0 until infiniteScrollLoopsCount - 1) {
                    val newChangedPosition = firstChangedPosition + listItemCount * i
                    if (newChangedPosition in 0 until itemCount // itemCount not items.size !!
                            && !excludedIndexes.contains(newChangedPosition)) {
                        notifyItemChanged(newChangedPosition)
                        notifiedPositions.add(newChangedPosition)
                    }
                }
            }
        }
        return notifiedPositions
    }


    /**
     * Extract real items info, despite of infinite or ordinary scroll.
     */
    private fun extractRealItemInfo(): List<ItemInfo> {
        val itemCount = listItemCount
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
                AutoNotifyDiffCallback(
                        infiniteScrollLoopsCount,
                        lastItemsInfo,
                        newItemInfo,
                        allowInfiniteScroll
                )
        )
        diffResult.dispatchUpdatesTo(this)
        lastItemsInfo = newItemInfo
    }

    abstract class ViewHolder<I>(view: View) : RecyclerView.ViewHolder(view) {

        open val clickableView: View? = itemView

        open val longClickableView: View? = itemView

        open val focusableView: View? = itemView

        var clickListener: View.OnClickListener? = null
        var longClickListener: View.OnLongClickListener? = null

        constructor(parent: ViewGroup, @LayoutRes layoutId: Int) :
                this(LayoutInflater.from(parent.context).inflate(layoutId, parent, false))

        protected val context: Context = view.context

        open fun bindData(position: Int, item: I, count: Int) {
            itemView.visibility = View.VISIBLE
        }

        open fun bindEmptyData(position: Int, item: I?, count: Int) {
            itemView.visibility = View.GONE
        }

        @CallSuper
        open fun onViewRecycled() {
            clickListener = null
            longClickListener = null
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

        fun notifyItemAdded(to: Int, item: I?, previousSize: Int) {
            synchronized(mObservers) {
                for (l in mObservers) {
                    l.onItemAdded(to, item, previousSize)
                }
            }
        }

        fun notifyItemsAdded(to: Int, items: Collection<I?>, previousSize: Int) {
            synchronized(mObservers) {
                for (l in mObservers) {
                    l.onItemsAdded(to, items, previousSize)
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

        fun notifyItemsRangeRemoved(from: Int, to: Int, previousSize: Int, removedItems: List<I?>) {
            synchronized(mObservers) {
                for (l in mObservers) {
                    l.onItemsRangeRemoved(from, to, previousSize, removedItems)
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

        fun onItemAdded(to: Int, item: I?, previousSize: Int)

        fun onItemsAdded(to: Int, items: Collection<I?>, previousSize: Int)

        fun onItemSet(to: Int, item: I?)

        fun onItemsSet(items: List<I?>)

        fun onItemRemoved(from: Int, item: I?)

        fun onItemsRangeRemoved(from: Int, to: Int, previousSize: Int, removedItems: List<I?>)
    }
}
