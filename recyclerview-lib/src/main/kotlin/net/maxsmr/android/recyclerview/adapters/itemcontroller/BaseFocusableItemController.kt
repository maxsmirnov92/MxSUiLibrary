package net.maxsmr.android.recyclerview.adapters.itemcontroller

import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.NO_POSITION
import ru.surfstudio.android.easyadapter.controller.loadable.LoadableItemController
import ru.surfstudio.android.easyadapter.holder.LoadableViewHolder

/**
 * Базовый класс, для [LoadableItemController],
 * которому требуется отслеживать текущие items,
 * с возможностью получения фокуса на контретном индексе
 */
// FIXME: allowAtPosition, запомнинание listeners для VH, infinite scroll, handleSelected из клика
abstract class BaseFocusableItemController<T, VH : BaseFocusableItemController.FocusableViewHolder<T>> : LoadableItemController<T, VH>() {

    /**
     * Адаптер, для которого используется данный
     * инстанс контроллера
     */
    var adapter: RecyclerView.Adapter<*>? = null

    /**
     * Текущие items, привязанные к адаптеру
     * (невозможно получить напрямую из EasyAdapter)
     */
    var items: List<T> = listOf()
    set(value) {
        field = transformItems(value)
        onItemsChanged()
    }

    /**
     * Кол-во элементов, которые не имеют отношения к этому [BaseFocusableItemController],
     * но должны быть учтены, когда спрашиваем adapterPosition у текущего холдера
     */
    var preItemsCount = 0
        set(value) {
            if (value >= 0) {
                field = value
            }
        }

    /**
     * Разрешить обновление элемента в адаптере
     * при изменении selectable в модели
     */
    var allowNotifyOnChange = true

    /**
     * Колбек при клике в указанную позицию
     */
    var onItemClickListener: ((position: Int, item: T?) -> Unit)? = null

    /**
     * Колбек при долгом клике в указанную позицию
     */
    var onItemLongClickListener: ((position: Int, item: T?) -> Boolean)? = null

    /**
     * Колбек при получении фокуса на указанную позицию
     */
    var onItemFocusedListener: ((position: Int, item: T?) -> Unit)? = null

    protected var pendingFocusPosition = NO_POSITION

    // T? - перестраховка на случай нульного значения
    // из базового контроллера
    override fun bind(holder: VH, data: T?) {
        // актуализируем текущий контроллер на случай реюза холдера
        holder.baseController = this
        super.bind(holder, data)
    }

    override fun getItemId(item: T?): String? = item?.hashCode().toString()

    open fun notifyDataSetChanged() {
        adapter?.notifyDataSetChanged()
    }

    /**
     * Оповестить об изменении элемента в позиции [position]
     */
    open fun notifyItemChanged(position: Int) {
        adapter?.notifyItemChanged(position)
                ?: throw IllegalStateException("Recycler adapter was not attached")
    }

    /**
     * Запросить фокус в указанном [index];
     * если не входит в текущий диапазон,
     * то только присвоить для дальнейших bind
     */
    fun requestFocus(index: Int) {
        pendingFocusPosition = index
        if (allowNotifyOnChange) {
            if (index >= 0 && index < getCount()) {
                notifyItemChanged(index)
            }
        }
    }

    /**
     * Очистить целевой индекс для фокуса
     */
    fun clearFocus() {
        pendingFocusPosition = NO_POSITION
    }

    fun getCount() = items.size

    fun isEmpty() = getCount() == 0

    fun isNotEmpty() = isEmpty().not()

    /**
     * @return найденный индекс или [NO_POSITION]
     */
    protected open fun indexOf(item: T?): Int = items.indexOfFirst { getItemId(it) == getItemId(item) }

    /**
     * Вызывается при изменении поля [items]
     */
    protected open fun onItemsChanged() {
        // do nothing
    }

    protected open fun transformItems(newItems: List<T>): List<T> = newItems

    /**
     * [LoadableViewHolder] с возможностью фокуса
     */
    abstract class FocusableViewHolder<T>(
            parent: ViewGroup,
            @LayoutRes layoutResId: Int
    ) : LoadableViewHolder<T>(parent, layoutResId) {

        protected open val focusableView: View? = null

        /**
         * view, на которую навешивается листенер смены selected-состояния
         */
        protected abstract val clickableView: View?

        protected abstract val longClickableView: View?

        /**
         * разрешить выставление базового клик листенера
         */
        protected abstract val canSetBaseClickListener: Boolean

        /**
         * разрешить выставление базового лонг-клик листенера
         */
        protected abstract val canSetBaseLongClickListener: Boolean

        lateinit var baseController: BaseFocusableItemController<T, out FocusableViewHolder<T>>

        protected open fun requestFocus(view: View) {
            view.requestFocus()
        }

        @CallSuper
        override fun bind(item: T?) {
            bindListeners(item)
            bindFocus(item)
        }

        @CallSuper
        protected open fun bindFocus(item: T?) {
            focusableView?.let { view ->
                with(baseController) {
                    if (pendingFocusPosition == adapterPosition - preItemsCount) {
                        requestFocus(view)
                        onItemFocusedListener?.invoke(adapterPosition, item)
                        pendingFocusPosition = NO_POSITION
                    }
                }
            }
        }

        @CallSuper
        protected open fun bindListeners(item: T?) {
            if (canSetBaseClickListener) {
                clickableView?.let {
                    it.setOnClickListener {
                        baseController.onItemClickListener?.invoke(adapterPosition - baseController.preItemsCount, item)
                    }
                }
            }
            if (canSetBaseLongClickListener) {
                longClickableView?.let {
                    it.setOnLongClickListener {
                        baseController.onItemLongClickListener?.invoke(adapterPosition - baseController.preItemsCount, item) ?: false
                    }
                }
            }
        }

        /**
         * Применить позицию фокуса без notify
         */
        protected fun applyFocusWithoutNotify(item: T?) {
            with(baseController) {
                val wasAllowed = allowNotifyOnChange
                allowNotifyOnChange = false
                requestFocus(adapterPosition)
                bindFocus(item)
                allowNotifyOnChange = wasAllowed
            }
        }
    }
}