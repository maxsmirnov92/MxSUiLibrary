package net.maxsmr.ui_testapp.adapter.base

import android.content.Context
import android.view.ViewGroup
import android.widget.RadioButton
import com.example.ui_testapp.R
import net.maxsmr.android.recyclerview.adapters.BaseRecyclerViewAdapter
import net.maxsmr.android.recyclerview.adapters.BaseSingleSelectionRecyclerViewAdapter
import net.maxsmr.ui_testapp.adapter.TestItem

class TestSingleAdapter(
        context: Context
) : BaseSingleSelectionRecyclerViewAdapter<TestItem, TestSingleAdapter.ViewHolder>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)

    override fun isItemEmpty(item: TestItem?, position: Int) = item == null || item.data.isEmpty()

    class ViewHolder(
            parent: ViewGroup
    ) : BaseRecyclerViewAdapter.ViewHolder<TestItem>(parent, R.layout.item_test_single) {

        private val testView = itemView.findViewById<RadioButton>(R.id.test_rb)

        override fun bindData(position: Int, item: TestItem, count: Int) {
            super.bindData(position, item, count)
            testView.text = item.data
        }
    }
}