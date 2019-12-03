package net.maxsmr.android.recyclerview.adapters.itemcontroller.view

/**
 * Интерфейс для вью поддерживающих состояние загрузки
 */
interface LoadableItemView {

    var loading : Boolean

    fun post(action: () -> Unit) {}
}