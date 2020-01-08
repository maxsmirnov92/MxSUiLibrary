package net.maxsmr.testapp.adapter.controller

import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.view.isVisible
import net.maxsmr.android.recyclerview.adapters.itemcontroller.view.LoadableItemView
import net.maxsmr.testapp.adapter.TestItem
import ru.surfstudio.android.utilktx.data.wrapper.selectable.SelectableData
import ru.surfstudio.android.utilktx.ktx.text.EMPTY_STRING
import net.maxsmr.android.recyclerview.adapters.itemcontroller.BaseSelectableItemController
import net.maxsmr.testapp.R

class TestMultiItemController : BaseSelectableItemController<TestItem, TestMultiItemController.Holder>() {

    override fun createLoadableViewHolder(parent: ViewGroup) = Holder(parent)

    override fun onSelectableChanged(isSelectable: Boolean) {
        super.onSelectableChanged(isSelectable)
        notifyDataSetChanged()
    }

    inner class Holder(parent: ViewGroup) : BaseSelectableItemController.SelectableViewHolder<TestItem>(parent, R.layout.item_test_multi) {

        private val testCheck = itemView.findViewById<CheckBox>(R.id.test_cb)
        private val testText = itemView.findViewById<TextView>(R.id.test_tv)

        override val clickableView: View = itemView

        override val longClickableView: View = clickableView

        override val selectableView: View = clickableView

        override val loadableView: LoadableItemView? = null

        override fun bind(item: SelectableData<TestItem>?) {
            super.bind(item)
            testCheck.isVisible = isSelectable
            testText.text = item?.data?.data ?: EMPTY_STRING
        }
    }
}