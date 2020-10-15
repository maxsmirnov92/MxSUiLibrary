package net.maxsmr.testapp.recycler.adapter.base

import android.content.Context
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_test_single.view.*
import net.maxsmr.android.recyclerview.adapters.base.selection.BaseSingleSelectionRecyclerViewAdapter
import net.maxsmr.testapp.R
import net.maxsmr.testapp.recycler.adapter.TestItem

class TestSingleAdapter(
        context: Context
) : BaseSingleSelectionRecyclerViewAdapter<TestItem, TestSingleAdapter.ViewHolder>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)

    override fun isItemEmpty(item: TestItem?, position: Int) = item == null || item.data.isEmpty()

//    override fun canSetClickListener(item: TestItem?, position: Int): Boolean = false

//    override fun canSetLongClickListener(item: TestItem?, position: Int): Boolean = false

//    override fun canSelectItem(item: TestItem?, position: Int): Boolean = false

//    override fun isDraggable(position: Int): Boolean = true

//    override fun isDismissible(position: Int): Boolean = true

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
    ) : BaseSelectableViewHolder<TestItem>(parent, R.layout.item_test_single) {

        private val testRadio = itemView.test_rb
        private val testText = itemView.test_tv

        // важно, иначе не будет скролла из-за перехвата от корневой itemView
        override val draggableView: View = testRadio

        override fun bindData(position: Int, item: TestItem, count: Int, isSelected: Boolean) {
            super.bindData(position, item, count, isSelected)
            testRadio.setOnCheckedChangeListener { buttonView, isChecked ->
                testRadio.isChecked = isSelected
                // возврат актуального значения, на которое нельзя повлиять иначе из-за onTouch
            }
            with(isSelectable) {
                testRadio.visibility = if (this) View.VISIBLE else View.GONE
//                testText.isClickable = !this
//                testText.isFocusable = !this
            }
            testText.text = item.data
        }
    }
}