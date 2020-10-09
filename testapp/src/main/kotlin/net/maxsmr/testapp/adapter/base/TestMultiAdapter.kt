package net.maxsmr.testapp.adapter.base

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import kotlinx.android.synthetic.main.item_test_multi.view.*
import net.maxsmr.android.recyclerview.adapters.base.selection.multi.BaseMultiSelectionRecyclerViewAdapter
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
    ) : BaseSelectableViewHolder<TestItem>(parent, R.layout.item_test_multi) {

        private val testCheck = itemView.test_cb
        private val testText = itemView.test_tv

        override val draggableView: View = testCheck

        override fun bindData(position: Int, item: TestItem, count: Int, isSelected: Boolean) {
            super.bindData(position, item, count, isSelected)
            testCheck.setOnCheckedChangeListener { buttonView, isChecked ->
                testCheck.isChecked = isSelected
            }
            testCheck.visibility = if (isSelectable) View.VISIBLE else View.GONE
            testText.text = item.data
        }
    }
}