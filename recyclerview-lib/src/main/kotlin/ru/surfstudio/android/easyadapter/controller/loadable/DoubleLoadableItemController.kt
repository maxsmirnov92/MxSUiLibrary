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
import ru.surfstudio.android.easyadapter.controller.DoubleBindableItemController
import ru.surfstudio.android.easyadapter.holder.DoubleLoadableViewHolder
import ru.surfstudio.android.easyadapter.view.LoadableItemView

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
