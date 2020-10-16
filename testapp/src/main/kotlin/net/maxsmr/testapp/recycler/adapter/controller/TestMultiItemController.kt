package net.maxsmr.testapp.recycler.adapter.controller

import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import net.maxsmr.testapp.recycler.adapter.TestItem
import net.maxsmr.android.recyclerview.adapters.itemcontroller.BaseSelectableItemController
import net.maxsmr.testapp.R
import ru.surfstudio.android.easyadapter.item.wrapper.SelectableData
import ru.surfstudio.android.easyadapter.view.LoadableItemView

class TestMultiItemController : BaseSelectableItemController<TestItem, TestMultiItemController.Holder>() {

    override fun createLoadableViewHolder(parent: ViewGroup) = Holder(parent)

    override fun onSelectableChanged(isSelectable: Boolean) {
        super.onSelectableChanged(isSelectable)
        notifyDataSetChanged()
    }

    inner class Holder(parent: ViewGroup) : BaseSelectableItemController.BaseSelectableViewHolder<TestItem>(parent, R.layout.item_test_multi) {

        private val testCheck = itemView.findViewById<CheckBox>(R.id.test_cb)
        private val testText = itemView.findViewById<TextView>(R.id.test_tv)

        override val clickableView: View = itemView

        override val longClickableView: View = clickableView

        override val selectableView: View = clickableView

        override val loadableView: LoadableItemView? = null

        override fun bind(item: SelectableData<TestItem>?) {
            super.bind(item)
            testCheck.visibility = if (isSelectable) View.VISIBLE else View.GONE
            testText.text = item?.data?.data ?: ""
        }
    }
}