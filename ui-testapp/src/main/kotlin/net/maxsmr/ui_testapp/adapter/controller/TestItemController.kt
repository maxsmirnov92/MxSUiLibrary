package net.maxsmr.ui_testapp.adapter.controller

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.ui_testapp.R
import net.maxsmr.android.recyclerview.adapters.itemcontroller.BaseFocusableItemController
import net.maxsmr.android.recyclerview.adapters.itemcontroller.view.LoadableItemView
import net.maxsmr.ui_testapp.adapter.TestItem
import ru.surfstudio.android.utilktx.ktx.text.EMPTY_STRING

class TestItemController : BaseFocusableItemController<TestItem, TestItemController.Holder>() {

    override fun createLoadableViewHolder(parent: ViewGroup) = Holder(parent)

    class Holder(parent: ViewGroup) : BaseFocusableItemController.FocusableViewHolder<TestItem>(parent, R.layout.item_test) {

        override val clickableView = itemView as TextView

        override val longClickableView: View = clickableView

        override val loadableView: LoadableItemView? = null

        override val allowSetBaseClickListener: Boolean = true

        override val allowSetBaseLongClickListener: Boolean = true

        override fun bind(item: TestItem?) {
            super.bind(item)
            clickableView.text = item?.data ?: EMPTY_STRING
        }
    }
}