package net.maxsmr.android.recyclerview.adapters.base.delegation

import androidx.recyclerview.widget.DiffUtil
import java.io.Serializable

interface BaseAdapterData : Serializable {

    fun isSame(other: BaseAdapterData): Boolean

    fun areContentsSame(other: BaseAdapterData): Boolean = this == other
}

interface ItemClickListener<T> {

    fun onItemClick(item: T)
}

fun <T : BaseAdapterData> itemCallback(): DiffUtil.ItemCallback<T> = object : DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem.isSame(newItem)
    }

    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem.areContentsSame(newItem)
    }
}