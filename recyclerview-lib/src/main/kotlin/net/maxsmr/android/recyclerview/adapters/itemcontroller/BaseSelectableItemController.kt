package net.maxsmr.android.recyclerview.adapters.itemcontroller

import android.view.View
import android.view.ViewGroup
import android.widget.Checkable
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView.NO_POSITION
import net.maxsmr.android.recyclerview.adapters.BaseMultiSelectionRecyclerViewAdapter
import ru.surfstudio.android.utilktx.data.wrapper.selectable.SelectableData

/**
 * Базовый item controller для реализации
 * selectable-свойства;
 * в отличие от [BaseMultiSelectionRecyclerViewAdapter]
 * не учитывает порядок, в котором менялся isSelectable
 */
abstract class BaseSelectableItemController<T, VH : BaseSelectableItemController.SelectableViewHolder<T>> :
        BaseFocusableItemController<SelectableData<T>, VH>() {

    val firstSelectedItem: T?
        get() = selectedItems.firstOrNull()

    val firstUnselectedItem: T?
        get() = unselectedItems.firstOrNull()

    val lastSelectedItem: T?
        get() = selectedItems.lastOrNull()

    val lastUnselectedItem: T?
        get() = unselectedItems.lastOrNull()

    val hasSelectedItems: Boolean
        get() = selectedItems.isNotEmpty()

    val hasUnselectedItems: Boolean
        get() = unselectedItems.isNotEmpty()

    /**
     * Элементы и их позиции в выбранном состоянии
     */
    val selectedItemsMap: Map<Int, T?>
        get() = getSelectedItemsMap(true)

    /**
     * Элементы и их позиции в невыбранном состоянии
     */
    val unselectedItemsMap: Map<Int, T?>
        get() = getSelectedItemsMap(false)

    val selectedItemsPositions: Set<Int>
        get() = selectedItemsMap.keys

    val unselectedItemsPositions: Set<Int>
        get() = unselectedItemsMap.keys

    /**
     * [T] в выбранном состоянии
     */
    val selectedItems: List<T?>
        get() = getSelectedItems(true)

    /**
     * [T] в невыбранном состоянии
     */
    val unselectedItems: List<T?>
        get() = getSelectedItems(false)

    /**
     * UI-ивенты, по которым будет срабатывать смена selected-состояния
     */
    var selectTriggerModes: Set<SelectTriggerMode> = setOf()
        set(value) {
            if (field != value) {
                field = value.toSet()
                onSelectTriggerModesChanged(selectTriggerModes)
            }
        }

    /**
     * Множественный или одиночный select
     * (логика учитывается только в UI-ивентах,
     * за правильность исходных данных контроллер не отвечает)
     */
    var selectMode = SelectMode.SINGLE
        set(value) {
            if (field != value) {
                field = value
                onSelectModeChanged(value)
            }
        }

    /**
     * Вкл/выкл возможность менять selected state
     */
    var isSelectable = true
        set(value) {
            if (field != value) {
                field = value
                onSelectableChanged(value)
            }
        }

    /**
     * Разрешить сброс isSelected для элемента из UI
     * (false может быть применимо для radiobutton)
     */
    var allowResetSelection = true
        set(value) {
            if (field != value) {
                field = value
                onAllowResetSelectionChanged(value)
            }
        }

    var onSelectedListener: ((position: Int, item: T?) -> Unit)? = null

    var onReselectedListener: ((position: Int, item: T?) -> Unit)? = null

    var onResetSelectionListener: ((position: Int, item: T?) -> Unit)? = null

    var onSelectTriggerModesChangedListener: ((selectModes: Set<SelectTriggerMode>) -> Unit)? = null

    var onSelectModeChangedListener: ((selectMode: SelectMode) -> Unit)? = null

    var onSelectableChangedListener: ((isSelectable: Boolean) -> Unit)? = null

    var onAllowResetSelectionChangedListener: ((isSelectable: Boolean) -> Unit)? = null

    override fun bind(holder: VH, data: SelectableData<T>?) {
        // актуализируем текущий контроллер на случай реюза холдера
        holder.selectableController = this
        holder.selectTriggerModes = getSelectTriggerModesForItem(data?.data, holder.adapterPosition)
        super.bind(holder, data)
    }

    override fun getItemId(item: SelectableData<T>?): String? = item?.data?.hashCode().toString()

    override fun transformItems(newItems: List<SelectableData<T>>): List<SelectableData<T>> {
        val copyItems = mutableListOf<SelectableData<T>>()
        newItems.forEach {
            copyItems.add(SelectableData(it.data, it.isSelected))
        }
        return copyItems
    }

    override fun onItemsChanged() {
        if (selectMode == SelectMode.SINGLE) {
            resetAllButFirstSelected()
        }
    }

    /**
     * @return найденная [SelectableData] или null
     */
    fun getItemByIndexNoThrow(index: Int): SelectableData<T>? =
            if (index >= 0 && index < items.size) items[index] else null


    fun toggleSelectable() {
        isSelectable = !isSelectable
    }

    fun resetSelected(targetItem: T?) =
            setSelected(targetItem, false, false)

    fun resetSelectedByIndex(targetIndex: Int) =
            setSelectedByIndex(targetIndex, false, false)

    fun toggleSelected(targetItem: SelectableData<T>, shouldInvalidateOthers: Boolean = true) =
            setSelected(targetItem.data, !targetItem.isSelected, shouldInvalidateOthers)

    fun toggleSelectedByIndex(targetIndex: Int, shouldInvalidateOthers: Boolean = true): Boolean {
        checkRange(targetIndex)
        return setSelectedByIndex(targetIndex, !items[targetIndex].isSelected, shouldInvalidateOthers)
    }

    /**
     * @return true, если selected-состояние было изменено в найденном элементе
     * false - в противном случае
     * @param targetItem элемент, selected-состояние которого необходимо изменить
     * @return true, если состояние было изменено, false - в противном случае
     */
    fun setSelected(targetItem: T?, toggle: Boolean, shouldInvalidateOthers: Boolean = true): Boolean =
            setSelectedByIndexNoThrow(internalIndexOf(targetItem), toggle, shouldInvalidateOthers)

    /**
     * Сменить selected-состояние 0-го
     * индекса в списке (если непустой)
     */
    fun setFirstSelected(toggle: Boolean) {
        if (isNotEmpty()) {
            setSelectedByIndex(0, toggle, true)
        }
    }

    /**
     * Сменить selected-состояние последнего
     * индекса в списке (если непустой)
     */
    fun setLastSelected(toggle: Boolean) {
        if (isNotEmpty()) {
            setSelectedByIndex(getCount() - 1, toggle, true)
        }
    }

    /**
     * @return true, если selected-состояние было изменено в указанном индексе
     * false - в противном случае
     * @param targetIndex индекс элемента, selected-состояние которого необходимо изменить
     * @return true, если состояние было изменено, false - в противном случае
     */
    fun setSelectedByIndex(
            targetIndex: Int,
            toggle: Boolean,
            shouldInvalidateOthers: Boolean = true
    ): Boolean {
        if (isSelectable || !toggle) {
            if (setSelectedInternal(targetIndex, toggle)) {
                if (toggle && selectMode == SelectMode.SINGLE && shouldInvalidateOthers) {
                    setSelectedExclude(false, listOf(targetIndex))
                }
                return true
            }
        }
        return false
    }


    fun resetSelected(targetItems: Collection<T>): Set<T> {
        val changedItems: MutableSet<T> = mutableSetOf()
        targetItems.forEachIndexed { indexOfIndex, index ->
            if (resetSelected(index/*, indexOfIndex == targetItems.size - 1*/)) {
                changedItems.add(index)
            }
        }
        return changedItems
    }

    fun resetSelectedByIndexes(targetIndexes: Collection<Int>): Set<Int> {
        val changedIndexes: MutableSet<Int> = mutableSetOf()
        targetIndexes.forEachIndexed { indexOfIndex, index ->
            if (resetSelectedByIndex(index /*, indexOfIndex == targetIndexes.size - 1*/)) {
                changedIndexes.add(index)
            }
        }
        return changedIndexes
    }

    fun toggleSelected(targetItems: Collection<SelectableData<T>>): Set<T> {
        val changedItems: MutableSet<T> = mutableSetOf()
        targetItems.forEachIndexed { index, item ->
            val result = if (selectMode == SelectMode.SINGLE) {
                resetSelected(item.data)
            } else {
                toggleSelected(item, index == targetItems.size - 1)
            }
            if (result) {
                changedItems.add(item.data)
            }
        }
        return changedItems
    }

    fun toggleSelectedByIndexes(targetIndexes: Collection<Int>): Set<Int> {
        val changedIndexes: MutableSet<Int> = mutableSetOf()
        targetIndexes.forEachIndexed { indexOfIndex, index ->
            if (toggleSelectedByIndex(index, indexOfIndex == targetIndexes.size - 1)) {
                changedIndexes.add(index)
            }
        }
        return changedIndexes
    }

    /**
     * @return индексы, в которых произошли изменения
     */
    fun setSelected(targetItems: Collection<T>, toggle: Boolean): Set<Int> {
        val targetIndexes: MutableSet<Int> = mutableSetOf()
        targetItems.forEach {
            val index = internalIndexOf(it)
            if (index >= 0 && index < items.size) {
                targetIndexes.add(index)
            }
        }
        return setSelectedByIndexes(targetIndexes, toggle)
    }

    /**
     * @return индексы, в которых произошли изменения
     */
    fun setSelectedByIndexes(targetIndexes: Collection<Int>, toggle: Boolean): Set<Int> {
        val result: MutableSet<Int> = mutableSetOf()
        if (!toggle || selectMode == SelectMode.MULTI || targetIndexes.size == 1) {
            targetIndexes.forEachIndexed { indexOfIndex, index ->
                if (setSelectedByIndex(index, toggle, indexOfIndex == targetIndexes.size - 1)) {
                    result.add(index)
                }
            }
        }
        return result
    }

    /**
     * Сбросить выбор у всех выставленных
     */
    fun resetAllSelected() {
        setAllSelected(false)
    }


    /**
     * Выставить выбранными все
     */
    fun setAllSelected() {
        setAllSelected(true)
    }

    /**
     * Инвертировать selection у всех выставленных
     */
    fun toggleAllSelections() {
        if (selectMode == SelectMode.SINGLE) {
            resetAllSelected()
        } else {
            items.forEachIndexed { index, item ->
                toggleSelectedByIndex(index, index == items.size - 1)
            }
        }
    }

    /**
     * Выставить [source] в целевом состоянии [toggle]
     * @return новый выставленный список
     */
    fun setNewItemsSelected(source: Collection<T>, toggle: Boolean): List<SelectableData<T>> {
        val result: MutableList<SelectableData<T>> = mutableListOf()
        if (!toggle || selectMode == SelectMode.MULTI || source.size == 1) {
            source.forEach { item ->
                result.add(SelectableData(item, toggle))
            }
            items = result
        }
        return result
    }

    /**
     * Завернуть и выставить [source] в список с [SelectableData]
     * с известным выбранным элементом
     */
    fun setNewItemsWithSelectItemCheck(source: Collection<T>, selectedItem: T?): List<SelectableData<T>> =
            setNewItemsWithSelectItemCheck(source, setOf(selectedItem))

    /**
     * Завернуть и выставить [source] в список с [SelectableData]
     * с известным выбранным индексом
     */
    fun setNewItemsWithSelectIndexCheck(source: Collection<T>, selectedIndex: Int): List<SelectableData<T>> =
            setNewItemsWithSelectIndexCheck(source, setOf(selectedIndex))

    // override if need various for each position
    protected open fun getSelectTriggerModesForItem(item: T?, position: Int): Set<SelectTriggerMode> = selectTriggerModes

    @CallSuper
    protected open fun onSelectTriggerModesChanged(selectModes: Set<SelectTriggerMode>) {
        onSelectTriggerModesChangedListener?.invoke(selectModes)
        if (allowNotifyOnChange) {
            notifyDataSetChanged()
        }
    }

    @CallSuper
    protected open fun onSelectModeChanged(selectMode: SelectMode) {
        if (selectMode == SelectMode.SINGLE) {
            resetAllButFirstSelected()
        }
        onSelectModeChangedListener?.invoke(selectMode)
    }

    @CallSuper
    protected open fun onSelectableChanged(isSelectable: Boolean) {
        if (!isSelectable) {
            resetAllSelected()
        }
        onSelectableChangedListener?.invoke(isSelectable)
    }

    @CallSuper
    protected open fun onAllowResetSelectionChanged(isAllowed: Boolean) {
        onAllowResetSelectionChangedListener?.invoke(isAllowed)
    }

    /**
     * @return id нижележащего элемента
     */
    protected open fun getInnerItemId(item: T?): String = item?.hashCode().toString()

    /**
     * @return найденный индекс или [NO_POSITION]
     */
    protected open fun internalIndexOf(item: T?): Int = items.indexOfFirst { getInnerItemId(it.data) == getInnerItemId(item) }

    private fun getSelectedItemsMap(isSelected: Boolean): Map<Int, T?> {
        val result = mutableMapOf<Int, T?>()
        items.forEachIndexed { index, selectableData ->
            if (selectableData.isSelected == isSelected) {
                result[index] = selectableData.data
            }
        }
        return result
    }

    private fun getSelectedItems(isSelected: Boolean): List<T?> = items
            .filter { it.isSelected == isSelected }
            .map { it.data }

    private fun setSelectedByIndexNoThrow(targetIndex: Int, toggle: Boolean, shouldInvalidateOthers: Boolean = true): Boolean {
        if (targetIndex >= 0 && targetIndex < items.size) {
            return setSelectedByIndex(targetIndex, toggle, shouldInvalidateOthers)
        }
        return false
    }

    /**
     * @param index индекс, для которого требуются изменения
     * @param index индекс, для которого требуются изменения
     * @return true, если произошли изменения,
     * false - если изменений не было
     */
    private fun setSelectedInternal(index: Int, toggle: Boolean): Boolean {
        checkRange(index)
        val item = items[index].data
        val wasSelected = items[index].isSelected
        if (wasSelected != toggle) {
            items[index].isSelected = toggle

            if (toggle) {
                onSelectedListener?.invoke(index, item)
            } else {
                onResetSelectionListener?.invoke(index, item)
            }

            if (allowNotifyOnChange) {
                notifyItemChanged(index)
            }
            return true

        } else {
            if (toggle) {
                onReselectedListener?.invoke(index, item)
            }
        }
        return false
    }

    /**
     * Сброс в текущих [items] всех, кроме первого выбранного
     */
    private fun resetAllButFirstSelected(reverse: Boolean = false) {
        var targetIndex = NO_POSITION
        val items = if (reverse) items.reversed() else items
        for (index in items.indices) {
            val selectableData = items[index]
            if (selectableData.isSelected) {
                targetIndex = index
                break
            }
        }
        if (targetIndex != NO_POSITION) {
            setSelectedExclude(false, listOf(targetIndex))
        }
    }

    private fun setSelectedExclude(toggle: Boolean = false, excludeIndexes: Collection<Int>) {
        // для SINGLE режима все остальные сбрасываем
        items.forEachIndexed { index, _ ->
            if (!excludeIndexes.contains(index)) {
                setSelectedInternal(index, toggle)
            }
        }
    }

    /**
     * Изменить selection на [toggle] у всех выставленных
     */
    private fun setAllSelected(toggle: Boolean) {
        if (toggle && selectMode == SelectMode.SINGLE) {
            if (isNotEmpty()) {
                setSelectedByIndex(0, toggle, true)
            }
        } else {
            items.forEachIndexed { index, _ ->
                setSelectedByIndex(index, toggle, index == items.size - 1)
            }
        }
    }

    /**
     * Завернуть и выставить [source] в список с [SelectableData]
     * с известными выбранными элементами (индекс будет определён)
     */
    private fun setNewItemsWithSelectItemCheck(source: Collection<T>, selectedItems: Set<T?>): List<SelectableData<T>> {
        val selectedIndexes: MutableSet<Int> = mutableSetOf()
        selectedItems.forEach {
            val selectedIndex = source.indexOfFirst { source -> getInnerItemId(source) == getInnerItemId(it) }
            if (selectedIndex != NO_POSITION) {
                selectedIndexes.add(selectedIndex)
            }
        }
        return setNewItemsWithSelectIndexCheck(source, selectedIndexes)
    }

    /**
     * Завернуть и выставить [source] в список с [SelectableData]
     * с известным выбранными индексами
     */
    private fun setNewItemsWithSelectIndexCheck(source: Collection<T>, selectedIndexes: Set<Int>): List<SelectableData<T>> {
        val result: MutableList<SelectableData<T>> = mutableListOf()
        source.forEachIndexed { index, item ->
            result.add(SelectableData(item, selectedIndexes.contains(index)))
        }
        items = result
        return result
    }

    private fun checkRange(index: Int) {
        require(!(index < 0 || index >= items.size)) { "Incorrect index: $index" }
    }

    abstract class SelectableViewHolder<T>(
            parent: ViewGroup,
            @LayoutRes layoutResId: Int
    ) : BaseFocusableItemController.FocusableViewHolder<SelectableData<T>>(parent, layoutResId) {

        /**
         * view, selected-состояние которой должно быть изменено
         */
        protected abstract val selectableView: View?

        override val allowSetBaseClickListener: Boolean
            get() = !selectTriggerModes.contains(SelectTriggerMode.CLICK)

        override val allowSetBaseLongClickListener: Boolean
            get() = !selectTriggerModes.contains(SelectTriggerMode.LONG_CLICK)

        lateinit var selectTriggerModes: Set<SelectTriggerMode>

        lateinit var selectableController: BaseSelectableItemController<T, out SelectableViewHolder<T>>

        protected open fun handleItemSelected(item: SelectableData<T>?) {
            val isItemSelected = item?.isSelected ?: false
            handleSelected(isItemSelected)
            if (isItemSelected) {
                onHandleItemSelected()
            } else {
                onHandleItemNotSelected()
            }
        }

        protected open fun handleSelected(isSelected: Boolean) {
            selectableView?.apply {
                if (this is Checkable) {
                    this.isChecked = isSelected
                } else {
                    this.isSelected = isSelected
                }
            }
        }

        @CallSuper
        override fun bind(item: SelectableData<T>?) {
            super.bind(item)
            bindSelection(item)
        }

        override fun bindListeners(item: SelectableData<T>?) {
            super.bindListeners(item)
            if (selectTriggerModes.contains(SelectTriggerMode.CLICK)) {
                clickableView?.let { view ->
                    item?.let {
                        view.setOnClickListener {
                            if (selectableController.selectTriggerModes.contains(SelectTriggerMode.CLICK)) {
                                val position = adapterPosition - selectableController.preItemsCount
                                changeSelectedStateFromUiNotify(position)
                            }
                            selectableController.onItemClickListener?.invoke(adapterPosition, item)
                        }
                    }
                }
            }
            if (selectTriggerModes.contains(SelectTriggerMode.LONG_CLICK)) {
                longClickableView?.setOnLongClickListener {
                    if (selectableController.selectTriggerModes.contains(SelectTriggerMode.LONG_CLICK)) {
                        val position = adapterPosition - selectableController.preItemsCount
                        changeSelectedStateFromUiNotify(position)
                    }
                    selectableController.onItemLongClickListener?.let { listener ->
                        return@setOnLongClickListener listener.invoke(adapterPosition, item)
                    }
                    return@setOnLongClickListener false
                }
            }
        }

        @CallSuper
        protected open fun bindSelection(item: SelectableData<T>?) {
            handleItemSelected(item)
        }

        /**
         * Кастомное действие биндинга, если состояние элемента "выбрано"
         */
        protected open fun onHandleItemSelected() {

        }

        /**
         * Кастомное действие биндинга, если состояние элемента "невыбрано"
         */
        protected open fun onHandleItemNotSelected() {

        }

        /**
         * @param item - целевой элемент, требующий изменения
         * @return true, если selection был обработан,
         * false - в противном случае - требуется вернуть view в исходное состояние (если было изменено)
         */
        protected fun changeSelectedStateFromUi(itemIndex: Int): Boolean {
            val item = selectableController.getItemByIndexNoThrow(itemIndex) ?: return false
            if (selectableController.isSelectable) {
                if (item.isSelected) {
                    if (selectableController.allowResetSelection) {
                        selectableController.resetSelectedByIndex(itemIndex)
                    } else {
                        // current state is selected, triggering reselect, state must be not changed
                        // (for e.g. like checkbox)
                        selectableController.setSelectedByIndex(itemIndex, true, true)
                        return false
                    }
                } else {
                    selectableController.setSelectedByIndex(itemIndex, true, true)
                }
                return true
            }
            // в данный момент смена selected state невозможна
            return false
        }

        /**
         * То же самое, что [changeSelectedStateFromUi], но с возвратом view в исходное состояние, если смена невозможна
         */
        protected fun changeSelectedStateFromUiNotify(itemIndex: Int) {
            val item = selectableController.getItemByIndexNoThrow(itemIndex) ?: return
            val isMultiple = selectableController.selectMode == SelectMode.MULTI
            val wasAllowed = selectableController.allowNotifyOnChange
            // чтобы избежать лишнего мелькания при notify в multiple-режиме
            if (isMultiple) {
                selectableController.allowNotifyOnChange = false
            }
            val result = changeSelectedStateFromUi(itemIndex)
            if (!result || isMultiple) {
                // если результат отрицательный - возвращаем в исходное состояние view (isSelected не изменился)
                // если результат любой и это не одиночный выбор - применяем без notify
                handleSelected(item.isSelected)
            }
            if (isMultiple) {
                selectableController.allowNotifyOnChange = wasAllowed
            }
        }
    }

    /**
     * Режим выбора в контроллере:
     * одиночный или множественный
     */
    enum class SelectMode {
        SINGLE, MULTI
    }

    /**
     * Событие смены selected-состояния
     * триггерится по клик или лонг-клик
     */
    enum class SelectTriggerMode {
        CLICK, LONG_CLICK
    }
}