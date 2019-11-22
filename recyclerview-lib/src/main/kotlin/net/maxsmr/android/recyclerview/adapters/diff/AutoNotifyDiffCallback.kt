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
package net.maxsmr.android.recyclerview.adapters.diff

import androidx.recyclerview.widget.DiffUtil
import ru.surfstudio.android.easyadapter.EasyAdapter.INFINITE_SCROLL_LOOPS_COUNT

/**
 * Implementation of [DiffUtil.Callback].
 * It is used to calculate difference between two lists of data depending on their [ItemInfo].
 */
class AutoNotifyDiffCallback constructor(
        private val lastItemsInfo: List<ItemInfo>,
        private val newItemsInfo: List<ItemInfo>,
        private val infiniteScroll: Boolean
) : DiffUtil.Callback() {

    private val MAGIC_NUMBER = 3578121127L.toString() // used for making ids unique

    override fun getOldListSize(): Int {
        return if (infiniteScroll) {
            lastItemsInfo.size * INFINITE_SCROLL_LOOPS_COUNT
        } else {
            lastItemsInfo.size
        }
    }

    override fun getNewListSize(): Int {
        return if (infiniteScroll) {
            newItemsInfo.size * INFINITE_SCROLL_LOOPS_COUNT
        } else {
            newItemsInfo.size
        }
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        if (infiniteScroll) {
            //magic numbers make every element id unique
            val lastItemsFakeItemId = lastItemsInfo[oldItemPosition % lastItemsInfo.size].id +
                    oldItemPosition.toString() +
                    MAGIC_NUMBER
            val newItemsFakeItemId = newItemsInfo[newItemPosition % newItemsInfo.size].id +
                    newItemPosition.toString() +
                    MAGIC_NUMBER

            return lastItemsFakeItemId == newItemsFakeItemId
        }
        return lastItemsInfo[oldItemPosition].id == newItemsInfo[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        var oldItemPosition = oldItemPosition
        var newItemPosition = newItemPosition
        if (infiniteScroll) {
            oldItemPosition %= lastItemsInfo.size
            newItemPosition %= newItemsInfo.size
        }
        return lastItemsInfo[oldItemPosition].hash == newItemsInfo[newItemPosition].hash
    }
}