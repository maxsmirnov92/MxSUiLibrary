package net.maxsmr.ui_testapp.adapter.controller

import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import com.example.ui_testapp.R
import net.maxsmr.android.recyclerview.adapters.itemcontroller.view.LoadableItemView
import net.maxsmr.ui_testapp.adapter.TestItem
import ru.surfstudio.android.utilktx.data.wrapper.selectable.SelectableData
import ru.surfstudio.android.utilktx.ktx.text.EMPTY_STRING
import ru.zenit.android.ui.common.controller.BaseSelectableItemController

class TestMultiItemController : BaseSelectableItemController<TestItem, BaseSelectableItemController.SelectableViewHolder<TestItem>>() {

    override fun createLoadableViewHolder(parent: ViewGroup) = Holder(parent)

    class Holder(parent: ViewGroup) : BaseSelectableItemController.SelectableViewHolder<TestItem>(parent, R.layout.item_test_multi) {

        override val clickableView = itemView.findViewById<CheckBox>(R.id.test_cb)

        override val selectableView: View? = clickableView

        override val loadableView: LoadableItemView? = null

        override fun bind(item: SelectableData<TestItem>?) {
            super.bind(item)
            clickableView.text = item?.data?.data ?: EMPTY_STRING
        }
    }
}