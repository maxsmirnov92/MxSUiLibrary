package net.maxsmr.ui_testapp.adapter.base

import android.content.Context
import android.view.ViewGroup
import android.widget.CheckBox
import com.example.ui_testapp.R
import net.maxsmr.android.recyclerview.adapters.BaseMultiSelectionRecyclerViewAdapter
import net.maxsmr.android.recyclerview.adapters.BaseRecyclerViewAdapter
import net.maxsmr.ui_testapp.adapter.TestItem

class TestMultiAdapter(
        context: Context
) : BaseMultiSelectionRecyclerViewAdapter<TestItem, TestMultiAdapter.ViewHolder>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)

    override fun isItemEmpty(item: TestItem?, position: Int) = item == null || item.data.isEmpty()

    class ViewHolder(
            parent: ViewGroup
    ) : BaseRecyclerViewAdapter.ViewHolder<TestItem>(parent, R.layout.item_test_multi) {

        private val testView = itemView.findViewById<CheckBox>(R.id.test_cb)

        override fun bindData(position: Int, item: TestItem, count: Int) {
            super.bindData(position, item, count)
            testView.text = item.data
        }
    }
}