/*
  Copyright (c) 2018-present, SurfStudio LLC, Maxim Tuev.

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
package ru.surfstudio.android.easyadapter.holder

import android.view.ViewGroup
import ru.surfstudio.android.easyadapter.view.LoadableItemView

abstract class DoubleLoadableViewHolder<T1, T2> @JvmOverloads constructor(viewGroup: ViewGroup, resId: Int = 0)
    : DoubleBindableViewHolder<T1, T2>(viewGroup, resId) {

    abstract val loadableView: LoadableItemView?

    var loading: Boolean = false
        set(value) {
            field = value
            postLoading()
        }

    fun postLoading() {
        loadableView?.post { loadableView?.loading = loading }
    }
}