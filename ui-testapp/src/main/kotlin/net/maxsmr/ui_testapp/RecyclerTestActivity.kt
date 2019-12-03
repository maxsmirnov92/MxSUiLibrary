package net.maxsmr.ui_testapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.NO_POSITION
import com.bejibx.android.recyclerview.selection.SelectionHelper
import com.example.ui_testapp.R
import kotlinx.android.synthetic.main.activity_recycler_test.*
import net.maxsmr.android.recyclerview.adapters.BaseMultiSelectionRecyclerViewAdapter
import net.maxsmr.android.recyclerview.adapters.BaseRecyclerViewAdapter
import net.maxsmr.android.recyclerview.adapters.BaseSingleSelectionRecyclerViewAdapter
import net.maxsmr.android.recyclerview.adapters.itemcontroller.BaseFocusableItemController
import net.maxsmr.ui_testapp.AdapterType.BASE
import net.maxsmr.ui_testapp.AdapterType.EASY
import net.maxsmr.ui_testapp.adapter.TestItem
import net.maxsmr.ui_testapp.adapter.base.TestMultiAdapter
import net.maxsmr.ui_testapp.adapter.base.TestNoneAdapter
import net.maxsmr.ui_testapp.adapter.base.TestSingleAdapter
import net.maxsmr.ui_testapp.adapter.controller.TestItemController
import net.maxsmr.ui_testapp.adapter.controller.TestMultiItemController
import ru.surfstudio.android.easyadapter.EasyAdapter
import ru.surfstudio.android.easyadapter.ItemList
import ru.surfstudio.android.utilktx.data.wrapper.selectable.SelectableData
import ru.surfstudio.android.utilktx.ktx.text.EMPTY_STRING
import ru.zenit.android.ui.common.controller.BaseSelectableItemController.SelectMode.*
import java.util.*

@Suppress("UNCHECKED_CAST")
class RecyclerTestActivity : AppCompatActivity() {

    private val defaultData = listOf(
            TestItem("test1"),
            TestItem("test2"),
            TestItem("test3"),
            TestItem("test4"),
            TestItem("test5")
    )

    private var adapterType: AdapterType = BASE
        set(value) {
            if (value != field) {
                val previousType = field
                field = value
                refreshRecyclerByAdapterType()
                Toast.makeText(
                        this,
                        getString(R.string.recycler_test_adapter_type_changed_message, previousType.name, value.name),
                        Toast.LENGTH_SHORT
                ).show()
            }
        }

    private val easyAdapter = EasyAdapter()

    private val noneItemController = TestItemController()
    private val multiItemController = TestMultiItemController()

    private val noneAdapter = TestNoneAdapter(this)
    private val singleAdapter = TestSingleAdapter(this).apply {
        allowResettingSelection = true
        setSelectModes(listOf(SelectionHelper.SelectMode.CLICK))
    }
    private val multiAdapter = TestMultiAdapter(this).apply {
        setAllowTogglingSelection(true)
        setSelectModes(listOf(SelectionHelper.SelectMode.CLICK))
    }

    private var isMultiItemControllerInUse = false

    private var currentBaseAdapter: BaseRecyclerViewAdapter<TestItem, BaseRecyclerViewAdapter.ViewHolder<TestItem>> =
            noneAdapter as BaseRecyclerViewAdapter<TestItem, BaseRecyclerViewAdapter.ViewHolder<TestItem>>

    init {
        noneAdapter.setItems(defaultData)
        singleAdapter.setItems(defaultData)
        multiAdapter.setItems(defaultData)
        noneItemController.items = defaultData
        multiItemController.items = defaultData.map { SelectableData(it, false) }
        noneItemController.adapter = easyAdapter
        multiItemController.adapter = easyAdapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycler_test)

        initListeners()
        initRecycler()

        recycler_type_select_rg.check(R.id.recycler_type_none_rb)
    }

    private fun initListeners() {
        recycler_type_select_rg.setOnCheckedChangeListener { group, checkedId ->
            refreshRecyclerByAdapterType()
        }
        recycler_type_switch_btn.setOnClickListener {
            toggleAdapterType()
        }
        recycler_current_selected_btn.setOnClickListener {
            showCurrentSelectedToast()
        }
        recycler_generate_data_btn.setOnClickListener {
            generateAdapterData()
        }
        recycler_clear_data_btn.setOnClickListener {
            clearAdapterData()
        }
    }

    private fun initRecycler() {
        recycler_test.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }

    private fun toggleAdapterType() {
        adapterType = if (adapterType == BASE) {
            EASY
        } else {
            BASE
        }
    }

    /**
     * По типу адаптера рефрешнуть [RecyclerView]
     */
    private fun refreshRecyclerByAdapterType() {
        SelectType.resolveByIndex(recycler_type_select_rg.getSelectedIndexInRadioGroup())?.let {
            val resultAdapter: RecyclerView.Adapter<*>
            if (adapterType == BASE) {
                val previousData = currentBaseAdapter.items as List<TestItem>
                currentBaseAdapter = when (it) {
                    SelectType.NONE -> noneAdapter as BaseRecyclerViewAdapter<TestItem, BaseRecyclerViewAdapter.ViewHolder<TestItem>>
                    SelectType.SINGLE -> singleAdapter as BaseRecyclerViewAdapter<TestItem, BaseRecyclerViewAdapter.ViewHolder<TestItem>>
                    SelectType.MULTI -> multiAdapter as BaseRecyclerViewAdapter<TestItem, BaseRecyclerViewAdapter.ViewHolder<TestItem>>
                }
                currentBaseAdapter.setItems(previousData)
                resultAdapter = currentBaseAdapter
            } else {
                when (it) {
                    SelectType.NONE -> {
                        val currentData = noneItemController.items
                        easyAdapter.setItems(ItemList.create()
                                .addAll(currentData, noneItemController as BaseFocusableItemController<TestItem, BaseFocusableItemController.FocusableViewHolder<TestItem>>))
                        isMultiItemControllerInUse = false
                    }
                    SelectType.SINGLE, SelectType.MULTI -> {
                        val currentData = multiItemController.items
                        multiItemController.selectMode =
                                if (it == SelectType.SINGLE) SINGLE else MULTI
                        easyAdapter.setItems(ItemList.create()
                                .addAll(currentData, multiItemController))
                        isMultiItemControllerInUse = true
                    }
                }
                resultAdapter = easyAdapter
            }
            recycler_test.adapter = resultAdapter
        }
    }

    private fun showCurrentSelectedToast() {
        var text: String = EMPTY_STRING
        if (adapterType == BASE) {
            with(currentBaseAdapter) {
                when {
                    this is BaseSingleSelectionRecyclerViewAdapter<*, *> -> {
                        selectedPosition.let {
                            if (it != NO_POSITION) {
                                val selectedItem = Pair(selectedItem, it)
                                text = selectedItem.toString()
                            }
                        }
                    }
                    this is BaseMultiSelectionRecyclerViewAdapter<*, *> -> {
                        selectedItems.let {
                            if (it.isNotEmpty()) {
                                val selectedItems = it
                                text = selectedItems.toString()
                            }
                        }
                    }
                }
            }
        } else {
            if (isMultiItemControllerInUse) {
                multiItemController.getSelectedItemsMap().let {
                    if (it.isNotEmpty()) {
                        text = it.toString()
                    }
                }
            }
        }
        Toast.makeText(this,
                if (text.isNotEmpty()) getString(R.string.recycler_test_selected_items_message, text) else getString(R.string.recycler_test_selected_items_empty_message),
                Toast.LENGTH_SHORT).show()
    }

    private fun generateAdapterData() {
        val count = currentBaseAdapter.itemCount.let {
            if (it > 0) {
                it
            } else {
                defaultData.size
            }
        }
        setAdapterData(generateData(count * 2))
    }

    private fun clearAdapterData() {
        setAdapterData(listOf())
    }

    private fun setAdapterData(data: List<TestItem>) {
        currentBaseAdapter.setItems(data)
        noneItemController.items = data
        multiItemController.items = data.map { SelectableData(it, false) }
        if (adapterType == EASY) {
            refreshRecyclerByAdapterType()
        }
    }

    companion object {

        // TODO move
        fun RadioGroup.getSelectedIndexInRadioGroup(): Int {
            val radioButtonId = checkedRadioButtonId
            val radioButton = findViewById<RadioButton>(radioButtonId) ?: null
            return radioButton?.let { indexOfChild(radioButton) } ?: RecyclerView.NO_POSITION
        }

        fun generateData(size: Int): List<TestItem> {
            val result = mutableListOf<TestItem>()
            for (i in 0..size) {
                val random = Random().nextInt()
                result.add(TestItem("test $random"))
            }
            return result
        }
    }
}

enum class SelectType {

    NONE, SINGLE, MULTI;

    companion object {

        fun resolveByIndex(index: Int): SelectType? =
                if (index >= 0 && index < values().size) {
                    values()[index]
                } else null
    }
}

enum class AdapterType {

    BASE, EASY
}
