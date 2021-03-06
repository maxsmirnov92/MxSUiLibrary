package net.maxsmr.testapp.recycler.adapter.base

import android.content.Context
import android.view.ViewGroup
import android.widget.TextView
import net.maxsmr.android.recyclerview.adapters.base.BaseRecyclerViewAdapter
import net.maxsmr.testapp.R
import net.maxsmr.testapp.recycler.adapter.TestItem

class TestNoneAdapter(
        context: Context
) : BaseRecyclerViewAdapter<TestItem, TestNoneAdapter.ViewHolder>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)

    override fun isItemEmpty(item: TestItem?, position: Int) = item == null || item.data.isEmpty()

    class ViewHolder(
            parent: ViewGroup
    ) : BaseRecyclerViewAdapter.ViewHolder<TestItem>(parent, R.layout.item_test) {

        private val testView = itemView as TextView

        override fun bindData(position: Int, item: TestItem, count: Int) {
            super.bindData(position, item, count)
            testView.text = item.data
        }
    }
}