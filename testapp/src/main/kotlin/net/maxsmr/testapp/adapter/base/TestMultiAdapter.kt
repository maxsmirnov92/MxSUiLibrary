package net.maxsmr.testapp.adapter.base

import android.content.Context
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.view.isVisible
import net.maxsmr.android.recyclerview.adapters.BaseMultiSelectionRecyclerViewAdapter
import net.maxsmr.android.recyclerview.adapters.BaseRecyclerViewAdapter
import net.maxsmr.testapp.R
import net.maxsmr.testapp.adapter.TestItem

class TestMultiAdapter(
        context: Context
) : BaseMultiSelectionRecyclerViewAdapter<TestItem, TestMultiAdapter.ViewHolder>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)

    override fun isItemEmpty(item: TestItem?, position: Int) = item == null || item.data.isEmpty()

    override fun onSelectableChanged(isSelectable: Boolean) {
        super.onSelectableChanged(isSelectable)
        if (allowNotifyOnChange) {
            notifyDataSetChanged()
        }
    }

    inner class ViewHolder(
            parent: ViewGroup
    ) : BaseRecyclerViewAdapter.ViewHolder<TestItem>(parent, R.layout.item_test_multi) {

        private val testCheck = itemView.findViewById<CheckBox>(R.id.test_cb)
        private val testText = itemView.findViewById<TextView>(R.id.test_tv)

        override fun bindData(position: Int, item: TestItem, count: Int) {
            super.bindData(position, item, count)
            testCheck.isVisible = isSelectable
            testText.text = item.data
        }
    }
}