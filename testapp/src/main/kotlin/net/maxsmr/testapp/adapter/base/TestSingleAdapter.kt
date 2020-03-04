package net.maxsmr.testapp.adapter.base

import android.content.Context
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.core.view.isVisible
import net.maxsmr.android.recyclerview.adapters.BaseRecyclerViewAdapter
import net.maxsmr.android.recyclerview.adapters.BaseSingleSelectionRecyclerViewAdapter
import net.maxsmr.testapp.R
import net.maxsmr.testapp.adapter.TestItem

class TestSingleAdapter(
        context: Context
) : BaseSingleSelectionRecyclerViewAdapter<TestItem, TestSingleAdapter.ViewHolder>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)

    override fun isItemEmpty(item: TestItem?, position: Int) = item == null || item.data.isEmpty()

//    override fun getLongClickableView(holder: ViewHolder): View? {
//        return holder.testText
//    }

    override fun onSelectableChanged(isSelectable: Boolean) {
        super.onSelectableChanged(isSelectable)
        if (allowNotifyOnChange) {
            notifyDataSetChanged()
        }
    }

    inner class ViewHolder(
            parent: ViewGroup
    ) : BaseRecyclerViewAdapter.ViewHolder<TestItem>(parent, R.layout.item_test_single) {

        private val testRadio = itemView.findViewById<RadioButton>(R.id.test_rb)
        private val testText = itemView.findViewById<TextView>(R.id.test_tv)

        override fun bindData(position: Int, item: TestItem, count: Int) {
            super.bindData(position, item, count)
            with(isSelectable) {
                testRadio.isVisible = this
//                testText.isClickable = !this
//                testText.isFocusable = !this
            }
            testText.text = item.data
        }
    }
}