/*
  Copyright (c) 2018-present, SurfStudio LLC

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */
package ru.surfstudio.android.easyadapter.controller.loadable

import android.view.ViewGroup
import ru.surfstudio.android.easyadapter.controller.NoDataItemController
import ru.surfstudio.android.easyadapter.holder.NoDataLoadableViewHolder
import ru.surfstudio.android.easyadapter.item.NoDataItem

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