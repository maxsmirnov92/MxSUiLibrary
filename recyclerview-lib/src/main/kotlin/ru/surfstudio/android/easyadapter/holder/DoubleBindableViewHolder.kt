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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes

/**
 * / **
 * [androidx.recyclerview.widget.RecyclerView.ViewHolder] with binding and displaying data support for two blocks of data.
 *
 *
 * To use with [ru.surfstudio.android.easyadapter.controller.BindableItemController]
 *
 *
 * This holder also has some convenient features, see [BaseViewHolder]
 *
 * @param <T1> first data type
 * @param <T2> second data type
</T2></T1> */
abstract class DoubleBindableViewHolder<T1, T2>(itemView: View) : BaseViewHolder(itemView) {

    constructor(parent: ViewGroup, @LayoutRes layoutRes: Int) : this(LayoutInflater.from(parent.context).inflate(layoutRes, parent, false))

    /**
     * Display two blocks of data in ViewHolder
     * This method will be executed on each call to [androidx.recyclerview.widget.RecyclerView.Adapter.onBindViewHolder]
     *
     * @param firstData  first data to display
     * @param secondData second data to display
     */
    abstract fun bind(firstData: T1, secondData: T2)
}
