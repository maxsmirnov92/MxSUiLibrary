package net.maxsmr.android.recyclerview.adapters.itemcontroller

import android.view.ViewGroup
import net.maxsmr.android.recyclerview.adapters.itemcontroller.view.LoadableItemView
import ru.surfstudio.android.easyadapter.controller.BindableItemController
import ru.surfstudio.android.easyadapter.controller.DoubleBindableItemController
import ru.surfstudio.android.easyadapter.controller.NoDataItemController
import ru.surfstudio.android.easyadapter.holder.BaseViewHolder
import ru.surfstudio.android.easyadapter.holder.BindableViewHolder
import ru.surfstudio.android.easyadapter.holder.DoubleBindableViewHolder
import ru.surfstudio.android.easyadapter.item.NoDataItem

abstract class LoadableItemController<T, H : LoadableViewHolder<T>>
    : BindableItemController<T, H>(), LoadableItemView {

    override var loading: Boolean = false
        set(value) {
            field = value
            viewHolders.forEach { it.loading = value }
        }

    val viewHolders = mutableSetOf<H>()

    abstract fun createLoadableViewHolder(parent: ViewGroup): H

    final override fun createViewHolder(parent: ViewGroup): H =
            createLoadableViewHolder(parent).also {
                viewHolders.add(it)
                it.loading = this.loading
            }
}

abstract class DoubleLoadableItemController<T1, T2, H : DoubleLoadableViewHolder<T1, T2>>
    : DoubleBindableItemController<T1, T2, H>(), LoadableItemView {

    override var loading: Boolean = false
        set(value) {
            field = value
            viewHolders.forEach { it.loading = value }
        }

    val viewHolders = mutableSetOf<H>()

    abstract fun createLoadableViewHolder(parent: ViewGroup): H

    final override fun createViewHolder(parent: ViewGroup): H =
            createLoadableViewHolder(parent).also {
                viewHolders.add(it)
                it.loading = this.loading
            }
}

abstract class LoadableViewHolder<T> @JvmOverloads constructor(viewGroup: ViewGroup, resId: Int = 0)
    : BindableViewHolder<T>(viewGroup, resId) {

    abstract val loadableView: LoadableItemView?

    var loading: Boolean = false
        set(value) {
            field = value
            animateShimmer()
        }

    fun animateShimmer() {
        loadableView?.post { loadableView?.loading = loading }
    }
}

abstract class DoubleLoadableViewHolder<T1, T2> @JvmOverloads constructor(viewGroup: ViewGroup, resId: Int = 0)
    : DoubleBindableViewHolder<T1, T2>(viewGroup, resId) {

    abstract val loadableView: LoadableItemView?

    var loading: Boolean = false
        set(value) {
            field = value
            animateShimmer()
        }

    fun animateShimmer() {
        loadableView?.post { loadableView?.loading = loading }
    }
}

abstract class LoadableNoDataItemController<H : NoDataLoadableViewHolder>
    : NoDataItemController<H>() {

    var loading: Boolean = false
        set(value) {
            field = value
            views.forEach { it.loading = value }
        }

    private val views = mutableSetOf<NoDataLoadableViewHolder>()

    abstract fun createLoadableViewHolder(parent: ViewGroup): H

    /**
     * Переопределяем getItemId, т.к могут быть вью-холдеры с одинаковыми ID
     * Если будут одинаковые ID, приложение падает со след. ошибкой:
     * java.lang.IllegalStateException: Two different ViewHolders have the same stable ID. Stable IDs in your adapter MUST BE unique and SHOULD NOT change.
     */
    override fun getItemId(item: NoDataItem<H>): String {
        // получаем строку типа: ru.surfstudio.android.easyadapter.item.NoDataItem@e49a14f
        // последнее значение "@e49a14f" - меняется.
        return item.toString()
    }

    override fun createViewHolder(parent: ViewGroup): H =
            createLoadableViewHolder(parent).also {
                views.add(it)
                it.loading = this.loading
            }
}

abstract class NoDataLoadableViewHolder @JvmOverloads constructor(viewGroup: ViewGroup, resId: Int = 0)
    : BaseViewHolder(viewGroup, resId) {

    abstract val loadableView: LoadableItemView?

    var loading: Boolean = false
        set(value) {
            field = value
            animateShimmer()
        }

    fun animateShimmer() {
        loadableView?.post { loadableView?.loading = loading }
    }
}