package net.maxsmr.testapp.recycler

import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.NO_POSITION
import net.maxsmr.android.recyclerview.adapters.base.BaseRecyclerViewAdapter
import net.maxsmr.android.recyclerview.adapters.base.drag.DragAndDropTouchHelperCallback
import net.maxsmr.android.recyclerview.adapters.base.drag.OnStartDragHelperListener
import net.maxsmr.android.recyclerview.adapters.base.selection.BaseSelectionRecyclerViewAdapter
import net.maxsmr.android.recyclerview.adapters.base.selection.BaseSingleSelectionRecyclerViewAdapter
import net.maxsmr.android.recyclerview.adapters.base.selection.multi.BaseMultiSelectionRecyclerViewAdapter
import net.maxsmr.android.recyclerview.adapters.itemcontroller.BaseFocusableItemController
import net.maxsmr.android.recyclerview.adapters.itemcontroller.BaseSelectableItemController
import net.maxsmr.android.recyclerview.adapters.itemcontroller.BaseSelectableItemController.SelectMode.MULTI
import net.maxsmr.android.recyclerview.adapters.itemcontroller.BaseSelectableItemController.SelectMode.SINGLE
import net.maxsmr.testapp.R
import net.maxsmr.testapp.recycler.AdapterType.BASE
import net.maxsmr.testapp.recycler.AdapterType.EASY
import net.maxsmr.testapp.recycler.adapter.TestItem
import net.maxsmr.testapp.recycler.adapter.base.TestMultiAdapter
import net.maxsmr.testapp.recycler.adapter.base.TestNoneAdapter
import net.maxsmr.testapp.recycler.adapter.base.TestSingleAdapter
import net.maxsmr.testapp.recycler.adapter.controller.TestItemController
import net.maxsmr.testapp.recycler.adapter.controller.TestMultiItemController
import ru.surfstudio.android.easyadapter.EasyAdapter
import ru.surfstudio.android.easyadapter.ItemList
import ru.surfstudio.android.easyadapter.item.wrapper.SelectableData
import java.util.*

@Suppress("UNCHECKED_CAST")
class RecyclerTestActivity : AppCompatActivity(), BaseRecyclerViewAdapter.ItemsEventsListener<TestItem> {

    private val defaultData = listOf(
            TestItem("test1"),
            TestItem("test2"),
            TestItem("test3"),
            TestItem("test4"),
            TestItem("test5")
    )

    private val easyAdapter = EasyAdapter()

    private val noneItemController = TestItemController()
    private val multiItemController = TestMultiItemController().apply {
        isSelectable = false
        selectTriggerModes = setOf(BaseSelectableItemController.SelectTriggerMode.CLICK)
    }

    private val noneAdapter = TestNoneAdapter(this).apply {
        allowInfiniteScroll = false
    }
    private val singleAdapter = TestSingleAdapter(this).apply {
        allowInfiniteScroll = false
        allowDragAndDrop = true
        isSelectable = false
        allowResetSelection = false
        selectTriggerModes = setOf(BaseSelectionRecyclerViewAdapter.SelectTriggerMode.CLICK)
    }
    private val multiAdapter = TestMultiAdapter(this).apply {
        allowInfiniteScroll = false
        allowDragAndDrop = true
        isSelectable = false
        selectTriggerModes = setOf(BaseSelectionRecyclerViewAdapter.SelectTriggerMode.CLICK)
    }

    private val singleTouchHelper = ItemTouchHelper(DragAndDropTouchHelperCallback(singleAdapter))
    private val multiTouchHelper = ItemTouchHelper(DragAndDropTouchHelperCallback(multiAdapter))

    private lateinit var typeSelectRadioGroup: RadioGroup
    private lateinit var testRecycler: RecyclerView
    private lateinit var typeSwitchButton: Button
    private lateinit var currentSelectedButton: Button
    private lateinit var generateDataButton: Button
    private lateinit var clearDataButton: Button

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

    private var isMultiItemControllerInUse = false

    private var currentBaseAdapter: BaseRecyclerViewAdapter<TestItem, BaseRecyclerViewAdapter.ViewHolder<TestItem>> =
            noneAdapter as BaseRecyclerViewAdapter<TestItem, BaseRecyclerViewAdapter.ViewHolder<TestItem>>

    private var currentTouchHelper: ItemTouchHelper? = null

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

        initViews()
        initListeners()

        typeSelectRadioGroup.check(R.id.recycler_type_none_rb)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_recycler_test, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var result = true
            when (item.itemId) {
                R.id.action_recycler_toggle_infinite_scroll -> toggleInfiniteScroll()
                R.id.action_recycler_toggle_draggable -> toggleDraggable()
                R.id.action_recycler_toggle_selectable -> toggleSelectable()
                R.id.action_recycler_toggle_selected -> toggleSelected()
                R.id.action_recycler_action_select_all -> setAllSelected()
                R.id.action_recycler_action_clear_all -> clearAllSelected()
                else -> result = false
            }
        return result
    }

    override fun onItemClick(position: Int, item: TestItem?) {
        // do nothing
    }

    override fun onItemLongClick(position: Int, item: TestItem?): Boolean {
        with(currentBaseAdapter) {
            if (this is BaseSelectionRecyclerViewAdapter<*, *, *>) {
                isSelectable = true
            }
        }
        return true
    }

    override fun onItemFocusChanged(position: Int, item: TestItem?) {
        // do nothing
    }

    override fun onItemAdded(to: Int, item: TestItem?, previousSize: Int) {
        // do nothing
    }

    override fun onItemsAdded(to: Int, items: Collection<TestItem?>, previousSize: Int) {
        // do nothing
    }

    override fun onItemSet(to: Int, item: TestItem?) {
        // do nothing
    }

    override fun onItemsSet(items: List<TestItem?>) {
        // do nothing
    }

    override fun onItemRemoved(position: Int, item: TestItem?) {
        // do nothing
    }

    override fun onItemsRangeRemoved(from: Int, to: Int, previousSize: Int, removedItems: List<TestItem?>) {
        // do nothing
    }

    override fun onItemsSwapped(fromPosition: Int, fromItem: TestItem?, toPosition: Int, toItem: TestItem?) {
        // do nothing
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        var result = false
        SelectType.resolveByIndex(getSelectedIndexInRadioGroup(typeSelectRadioGroup))?.let { type ->
            if (adapterType == BASE && type == SelectType.SINGLE) {
                val adapter = currentBaseAdapter as BaseSingleSelectionRecyclerViewAdapter<*,*>
                when(keyCode) {
                    KeyEvent.KEYCODE_DPAD_DOWN -> {
                        adapter.nextSelection(true)
                        result = true
                    }
                    KeyEvent.KEYCODE_DPAD_UP -> {
                        adapter.previousSelection(true)
                        result = true
                    }
                }
            }
        }
        return result || super.onKeyUp(keyCode, event)
    }

    private fun initViews() {
        typeSelectRadioGroup = findViewById(R.id.recycler_type_select_rg)
        testRecycler = findViewById(R.id.recycler_test_rv)
        typeSwitchButton = findViewById(R.id.recycler_type_switch_btn)
        currentSelectedButton = findViewById(R.id.recycler_current_selected_btn)
        generateDataButton = findViewById(R.id.recycler_generate_data_btn)
        clearDataButton = findViewById(R.id.recycler_clear_data_btn)

        initToolbar()
        initRecycler()
    }

    private fun initToolbar() {
        setSupportActionBar(findViewById(R.id.recycler_test_toolbar))
    }

    private fun initRecycler() {
        testRecycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }

    private fun initListeners() {
        singleAdapter.registerItemsEventsListener(this)
        singleAdapter.startDragListener = OnStartDragHelperListener(singleTouchHelper)

        multiAdapter.registerItemsEventsListener(this)
        multiAdapter.startDragListener = OnStartDragHelperListener(multiTouchHelper)

        multiItemController.onItemLongClickListener = { _, _ ->
            multiItemController.isSelectable = true
            true
        }

        typeSelectRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            refreshRecyclerByAdapterType()
        }
        typeSwitchButton.setOnClickListener {
            toggleAdapterType()
        }
        currentSelectedButton.setOnClickListener {
            showCurrentSelectedToast()
        }
        generateDataButton.setOnClickListener {
            generateAdapterData()
        }
        clearDataButton.setOnClickListener {
            clearAdapterData()
        }
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
        SelectType.resolveByIndex(getSelectedIndexInRadioGroup(typeSelectRadioGroup))?.let { type ->
            val resultAdapter: RecyclerView.Adapter<*>
            if (adapterType == BASE) {
                val previousData = currentBaseAdapter.items as List<TestItem>
                currentBaseAdapter = when (type) {
                    SelectType.NONE -> noneAdapter as BaseRecyclerViewAdapter<TestItem, BaseRecyclerViewAdapter.ViewHolder<TestItem>>
                    SelectType.SINGLE -> singleAdapter as BaseRecyclerViewAdapter<TestItem, BaseRecyclerViewAdapter.ViewHolder<TestItem>>
                    SelectType.MULTI -> multiAdapter as BaseRecyclerViewAdapter<TestItem, BaseRecyclerViewAdapter.ViewHolder<TestItem>>
                }
                val previousTouchHelper = currentTouchHelper
                currentTouchHelper = when (type) {
                    SelectType.NONE -> null
                    SelectType.SINGLE -> singleTouchHelper
                    SelectType.MULTI -> multiTouchHelper
                }
                previousTouchHelper?.attachToRecyclerView(null)
                currentTouchHelper?.attachToRecyclerView(testRecycler)
                currentBaseAdapter.setItems(previousData)
                resultAdapter = currentBaseAdapter
            } else {
                when (type) {
                    SelectType.NONE -> {
                        val currentData = noneItemController.items
                        easyAdapter.setItems(ItemList.create()
                                .addAll(currentData, noneItemController as BaseFocusableItemController<TestItem, BaseFocusableItemController.FocusableViewHolder<TestItem>>))
                        isMultiItemControllerInUse = false
                    }
                    SelectType.SINGLE, SelectType.MULTI -> {
                        val currentData = multiItemController.items
                        multiItemController.selectMode =
                                if (type == SelectType.SINGLE) SINGLE else MULTI
                        easyAdapter.setItems(ItemList.create()
                                .addAll(currentData, multiItemController))
                        isMultiItemControllerInUse = true
                    }
                }
                resultAdapter = easyAdapter
            }
            testRecycler.adapter = resultAdapter
        }
    }

    private fun showCurrentSelectedToast() {
        var text: String = ""
        if (adapterType == BASE) {
            with(currentBaseAdapter) {
                when (this) {
                    is BaseSingleSelectionRecyclerViewAdapter<*, *> -> {
                        selectedPosition.let {
                            if (it != NO_POSITION) {
                                val selectedItem = Pair(selectedItem, it)
                                text = selectedItem.toString()
                            }
                        }
                    }
                    is BaseMultiSelectionRecyclerViewAdapter<*, *> -> {
                        selectedItemsMap.let {
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
                multiItemController.selectedItemsMap.let {
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
        val defaultSize = defaultData.size
        setAdapterData(generateData(defaultSize * Random().nextInt(defaultSize) + defaultSize))
    }

    private fun clearAdapterData() {
        setAdapterData(listOf())
    }

    private fun setAdapterData(data: List<TestItem>) {
        when (adapterType) {
            BASE -> currentBaseAdapter.setItems(data)
            else -> {
                noneItemController.items = data
                multiItemController.items = data.map { SelectableData(it, false) }
                refreshRecyclerByAdapterType()
            }
        }
    }

    private fun toggleInfiniteScroll() {
        when (adapterType) {
            BASE -> currentBaseAdapter.toggleInfiniteScroll()
            else -> easyAdapter.setInfiniteScroll(!currentBaseAdapter.allowInfiniteScroll)
        }
    }

    private fun toggleDraggable() {
        when (adapterType) {
            BASE -> currentBaseAdapter.toggleDragAndDrop()
            else -> Toast.makeText(this, R.string.recycler_test_unable_to_toggle_draggable, Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleSelectable() {
        with(currentBaseAdapter) {
            if (this is BaseSelectionRecyclerViewAdapter<*, *, *>) {
                when (adapterType) {
                    BASE -> toggleSelectable()
                    else -> multiItemController.toggleSelectable()
                }
            } else {
                Toast.makeText(this@RecyclerTestActivity, R.string.recycler_test_unable_to_toggle_selectable, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun toggleSelected() {
        when (adapterType) {
            BASE -> {
                with(currentBaseAdapter) {
                    when (this) {
                        is BaseMultiSelectionRecyclerViewAdapter<*, *> -> toggleAllItemsSelected()
                        is BaseSingleSelectionRecyclerViewAdapter<*, *> -> toggleCurrentSelection()
                        else -> {
                            Toast.makeText(this@RecyclerTestActivity, R.string.recycler_test_unable_to_toggle_selected, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            else -> multiItemController.toggleAllSelections()
        }
    }

    private fun setAllSelected() {
        when (adapterType) {
            BASE -> with(currentBaseAdapter) {
                when (this) {
                    is BaseMultiSelectionRecyclerViewAdapter<*, *> -> setAllItemsSelected()
                    else -> Toast.makeText(this@RecyclerTestActivity, R.string.recycler_test_unable_to_set_all_selection, Toast.LENGTH_SHORT).show()
                }
            }
            else -> multiItemController.setAllSelected()
        }
    }

    private fun clearAllSelected() {
        when (adapterType) {
            BASE -> with(currentBaseAdapter) {
                when (this) {
                    is BaseMultiSelectionRecyclerViewAdapter<*, *> -> resetAllItemsSelection()
                    is BaseSingleSelectionRecyclerViewAdapter<*, *> -> resetSelection()
                    else -> {
                        Toast.makeText(this@RecyclerTestActivity, R.string.recycler_test_unable_to_clear_all_selection, Toast.LENGTH_SHORT).show()
                    }
                }
            }
            else -> multiItemController.resetAllSelected()
        }
    }

    companion object {

        fun generateData(size: Int): List<TestItem> {
            val result = mutableListOf<TestItem>()
            for (i in 0..size) {
                val random = Random().nextInt()
                result.add(TestItem("test $random"))
            }
            return result
        }

        fun getSelectedIndexInRadioGroup(group: RadioGroup): Int {
            val radioButtonId = group.checkedRadioButtonId
            val radioButton = group.findViewById<RadioButton>(radioButtonId) ?: null
            return radioButton?.let { group.indexOfChild(it) } ?: RecyclerView.NO_POSITION
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
