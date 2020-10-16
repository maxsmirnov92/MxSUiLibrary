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
package ru.surfstudio.android.easyadapter.item.wrapper

/**
 * Интерфейс сущности, которая может быть выделяемым
 */
interface SelectableDataInterface {

    var isSelected: Boolean

    fun toggleSelected() {
        isSelected = !isSelected
    }
}

/**
 * Поддерживает одиночное выделение, используется в extension-функциях. [SelectableExtension]
 * Если необходимо множественное выделение -> смотри [CheckableData]
 */
data class SelectableData<T>(override var data: T,
                             override var isSelected: Boolean = false)
    : DataWrapperInterface<T>, SelectableDataInterface