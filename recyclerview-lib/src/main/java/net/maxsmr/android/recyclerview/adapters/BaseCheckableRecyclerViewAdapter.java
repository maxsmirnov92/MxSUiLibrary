package net.maxsmr.android.recyclerview.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import android.support.v7.widget.RecyclerView;
import android.widget.Checkable;

import com.bejibx.android.recyclerview.selection.HolderClickObserver;
import com.bejibx.android.recyclerview.selection.SelectionHelper;
import com.bejibx.android.recyclerview.selection.SelectionObserver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public abstract class BaseCheckableRecyclerViewAdapter<I, VH extends BaseRecyclerViewAdapter.ViewHolder> extends BaseRecyclerViewAdapter<I, VH> implements HolderClickObserver, SelectionObserver {

    private SelectionHelper mSelectionHelper;

    @NotNull
//    private final Map<Integer, Set<SelectionHelper.SelectMode>> mSelectionModes = new LinkedHashMap<>();
    private final Set<SelectionHelper.SelectMode> mSelectionModes = new LinkedHashSet<>();

    @Nullable
    private Drawable defaultDrawable, selectionDrawable;

    public BaseCheckableRecyclerViewAdapter(@NotNull Context context, @LayoutRes int itemLayoutId, @Nullable Collection<I> items) {
        this(context, itemLayoutId, items, null, null, true);
    }

    public BaseCheckableRecyclerViewAdapter(@NotNull Context context, @LayoutRes int itemLayoutId, @Nullable Collection<I> items, @Nullable Drawable defaultDrawable, @Nullable Drawable selectionDrawable, boolean selectable) {
        super(context, itemLayoutId, items);
        initSelectionHelper();
        setSelectable(selectable);
        if (defaultDrawable != null) {
            setDefaultDrawable(defaultDrawable);
        }
        if (selectionDrawable != null) {
            setSelectionDrawable(selectionDrawable);
        }
    }

    private void initSelectionHelper() {
        if (mSelectionHelper == null) {
            mSelectionHelper = new SelectionHelper();
            mSelectionHelper.registerSelectionObserver(this);
            mSelectionHelper.registerHolderClickObserver(this);
        }
    }

    //    protected final SelectionHelper getSelectionHelper() {
//        return mSelectionHelper;
//    }

    public boolean isSelectable() {
        return mSelectionHelper.isSelectable();
    }

    public void setSelectable(boolean toggle) {
        mSelectionHelper.setSelectable(toggle);
    }

//    private void checkSelectionModes(@Nullable Map<Integer, Set<SelectionHelper.SelectMode>> selectionModes) {
//        if (selectionModes != null) {
//            for (int pos : selectionModes.keySet()) {
//                rangeCheck(pos);
//            }
//        }
//    }

//    @SuppressLint("NewApi")
//    public void setSelectionModes(@Nullable Map<Integer, Set<SelectionHelper.SelectMode>> selectionModes) {
//        checkSelectionModes(selectionModes);
//        synchronized (mSelectionModes) {
//            if (!Objects.equals(selectionModes, mSelectionModes)) {
//                mSelectionModes.clear();
//                if (selectionModes != null) {
//                    mSelectionModes.putAll(selectionModes);
//                }
//            }
//        }
//    }


    @NotNull
    public Set<SelectionHelper.SelectMode> getSelectionModes() {
        return new LinkedHashSet<>(mSelectionModes);
    }

    @SuppressLint("NewApi")
    public void setSelectionModes(@Nullable Set<SelectionHelper.SelectMode> selectionModes) {
        synchronized (mSelectionModes) {
            if (!Objects.equals(selectionModes, mSelectionModes)) {
                mSelectionModes.clear();
                if (selectionModes != null) {
                    mSelectionModes.addAll(selectionModes);
                }
                if (isNotifyOnChange()) {
                    notifyDataSetChanged();
                }
            }
        }
    }

    protected void processSelection(@NotNull VH holder, @Nullable I item, int position) {
        mSelectionHelper.wrapSelectable(holder, mSelectionModes); /* mSelectionModes.get(position) */

        final boolean isSelected = isItemSelected(position);

        if ((holder.itemView instanceof Checkable)) {
            ((Checkable) holder.itemView).setChecked(isSelected);
        }

        if (isSelected) {
            onProcessItemSelected(holder);
        } else {
            onProcessItemNotSelected(holder);
        }
    }

    @Override
    public void onViewRecycled(VH holder) {
        super.onViewRecycled(holder);
        mSelectionHelper.recycleHolder(holder);
    }

    @Override
    @CallSuper
    protected void processItem(@NotNull VH holder, @Nullable I item, int position) {
        super.processItem(holder, item, position);
        processSelection(holder, item, position);
    }

    @Override
    protected final boolean allowSetClickListener(@Nullable I item, int position) {
        return false;
    }

    @Override
    protected final boolean allowSetLongClickListener(@Nullable I item, int position) {
        return false;
    }

    @Nullable
    public Drawable getDefaultDrawable() {
        return defaultDrawable;
    }

    public void setDefaultDrawable(@Nullable Drawable defaultDrawable) {
        this.defaultDrawable = defaultDrawable;
        if (isNotifyOnChange())
            notifyDataSetChanged();
    }

    @Nullable
    public Drawable getSelectionDrawable() {
        return selectionDrawable;
    }

    public void setSelectionDrawable(@Nullable Drawable selectionDrawable) {
        this.selectionDrawable = selectionDrawable;
        if (isNotifyOnChange())
            notifyDataSetChanged();
    }

    @Override
    protected void onItemAdded(int to, @Nullable I item) {
        fixSelectionIndexOnAdd(to, 1);
        super.onItemAdded(to, item);
    }

    @Override
    protected void onItemsAdded(int to, @NotNull Collection<I> items) {
        fixSelectionIndexOnAdd(to, items.size());
        super.onItemsAdded(to, items);
    }

    @Override
    protected void onItemRemoved(int from, @Nullable I item) {
        fixSelectionIndexOnRemove(from, 1);
        super.onItemRemoved(from, item);
    }

    @Override
    protected void onItemsRangeRemoved(int from, int to, int previousSize) {
        fixSelectionIndexOnRemove(from, from == to? 1: to - from);
        super.onItemsRangeRemoved(from, to, previousSize);
    }

    @Override
    protected void onItemsSet() {
        clearSelection();
        super.onItemsSet();
    }

    private void fixSelectionIndexOnAdd(int to, int count) {
        Set<Integer> newSet = new LinkedHashSet<>();
        if (count >= 1) {
            Iterator<Integer> it = getSelectedItemsPositions().iterator();
            while (it.hasNext()) {
                Integer selection = it.next();
                if (selection != RecyclerView.NO_POSITION) {
                    if (selection >= to) {
                        selection += count;
                    }
                    newSet.add(selection);
                }
            }
        }
        setItemsSelectedByPositions(newSet, true);
    }

    private void fixSelectionIndexOnRemove(int from, int count) {
        Set<Integer> newSet = new LinkedHashSet<>();
        if (count >= 1) {
            Iterator<Integer> it = getSelectedItemsPositions().iterator();
            while (it.hasNext()) {
                Integer selection = it.next();
                if (selection != RecyclerView.NO_POSITION) {
                    if (selection > from && from + count < selection) {
                        selection -= count;
                    } else if (selection >= from && selection <= from + count) {
                        selection = RecyclerView.NO_POSITION;
                    }
                    if (selection != RecyclerView.NO_POSITION) {
                        newSet.add(selection);
                    }
                }
            }
        }
        setItemsSelectedByPositions(newSet, true);
    }

    @NotNull
    public List<I> getSelectedItems() {
        List<I> selectedItems = new ArrayList<>();
        Set<Integer> selectedPositions = getSelectedItemsPositions();
        for (Integer pos : selectedPositions) {
            selectedItems.add(getItem(pos));
        }
        return selectedItems;
    }

    @NotNull
    public Set<I> getUnselectedItems() {
        Set<I> unselectedItems = new LinkedHashSet<>();
        Set<Integer> unselectedPositions = getUnselectedItemsPositions();
        for (Integer pos : unselectedPositions) {
            unselectedItems.add(getItem(pos));
        }
        return unselectedItems;
    }

    @NotNull
    public Set<Integer> getSelectedItemsPositions() {
//        if (mSelectionHelper == null) {
//            throw new IllegalStateException(SelectionHelper.class.getSimpleName() + " was not initialized");
//        }
        return mSelectionHelper != null? mSelectionHelper.getSelectedItems() : Collections.<Integer>emptySet();
    }

    @NotNull
    public Set<Integer> getUnselectedItemsPositions() {
        LinkedHashSet<Integer> unselectedPositions = new LinkedHashSet<>();
        Set<Integer> selectedPositions = getSelectedItemsPositions();
        for (int pos = 0; pos < getItemCount(); pos++) {
            if (!selectedPositions.contains(pos)) {
                unselectedPositions.add(pos);
            }
        }
        return unselectedPositions;
    }

    public boolean isItemSelected(int position) {
//        if (mSelectionHelper == null) {
//            throw new IllegalStateException(SelectionHelper.class.getSimpleName() + " was not initialized");
//        }
        rangeCheck(position);
        return mSelectionHelper.isItemSelected(position);
    }

    public boolean isItemSelected(I item) {
        return isItemSelected(indexOf(item));
    }

    public int getSelectedItemsCount() {
//        if (mSelectionHelper == null) {
//            throw new IllegalStateException(SelectionHelper.class.getSimpleName() + " was not initialized");
//        }
        return getItemCount() > 0 ? mSelectionHelper != null? mSelectionHelper.getSelectedItemsCount() : 0 : 0;
    }

    public boolean setItemsSelectedByPositions(@Nullable Collection<Integer> positions, boolean isSelected) {
//        if (mSelectionHelper == null) {
//            throw new IllegalStateException(SelectionHelper.class.getSimpleName() + " was not initialized");
//        }
        if (positions != null) {
            for (int pos : positions) {
                rangeCheck(pos);
            }
        }
        return mSelectionHelper != null && mSelectionHelper.setItemsSelectedByPositions(positions, isSelected, false);
    }

    public boolean setItemSelectedByPosition(int position, boolean isSelected) {
//        if (mSelectionHelper == null) {
//            throw new IllegalStateException(SelectionHelper.class.getSimpleName() + " was not initialized");
//        }
        rangeCheck(position);
        return mSelectionHelper != null && mSelectionHelper.setItemSelectedByPosition(position, isSelected, false);
    }

    public boolean toggleItemsSelectedByPositions(@Nullable Collection<Integer> positions) {
//        if (mSelectionHelper == null) {
//            throw new IllegalStateException(SelectionHelper.class.getSimpleName() + " was not initialized");
//        }
        if (positions != null) {
            for (int pos : positions) {
                rangeCheck(pos);
            }
        }
        return mSelectionHelper != null && mSelectionHelper.toggleItemsSelectedByPositions(positions, false);
    }

    public boolean toggleItemSelectedByPosition(int position) {
//        if (mSelectionHelper == null) {
//            throw new IllegalStateException(SelectionHelper.class.getSimpleName() + " was not initialized");
//        }
        rangeCheck(position);
        return mSelectionHelper != null && mSelectionHelper.toggleItemSelectedByPosition(position, false);
    }

    public boolean setItemsSelected(@Nullable Collection<I> items, boolean isSelected) {
        List<Integer> positions = new ArrayList<>();
        if (items != null) {
            for (I item : items) {
                int index = indexOf(item);
                if (index > -1) {
                    positions.add(index);
                }
            }
        }
        return setItemsSelectedByPositions(positions, isSelected);
    }

    public boolean setItemSelected(I item, boolean isSelected) {
        return setItemsSelected(Collections.singletonList(item), isSelected);
    }

    public boolean toggleItemsSelected(Collection<I> items) {
        List<Integer> positions = new ArrayList<>();
        if (items != null) {
            for (I item : items) {
                int index = indexOf(item);
                if (index > -1) {
                    positions.add(index);
                }
            }
        }
        return toggleItemsSelectedByPositions(positions);
    }

    public boolean toggleItemSelected(I item) {
        return toggleItemsSelected(Collections.singletonList(item));
    }

    public void clearSelection() {
//        if (mSelectionHelper == null) {
//            throw new IllegalStateException(SelectionHelper.class.getSimpleName() + " was not initialized");
//        }
        if (mSelectionHelper != null) {
            if (mSelectionHelper.getSelectedItemsCount() > 0) {
                mSelectionHelper.clearSelection(false);
            }
        }
    }

    protected void onProcessItemSelected(@NotNull VH holder) {

    }

    protected void onProcessItemNotSelected(@NotNull VH holder) {

    }

    @SuppressWarnings("unchecked")
    @Override
    public final void onSelectedChanged(RecyclerView.ViewHolder holder, boolean isSelected, boolean fromUser) {
        if (itemSelectedChangeListener != null) {
            itemSelectedChangeListener.onItemSelectedChange(holder.getAdapterPosition(), isSelected);
        }
        if (isNotifyOnChange())
            notifyItemChanged(holder.getAdapterPosition());
    }

    @Override
    public void onSelectableChanged(boolean isSelectable) {

    }

    @Override
    public void onHolderClick(RecyclerView.ViewHolder holder) {

    }

    @Override
    public boolean onHolderLongClick(RecyclerView.ViewHolder holder) {
        return false;
    }


    @Nullable
    private OnItemSelectedChangeListener itemSelectedChangeListener;

    public void setOnSelectedChangeListener(@Nullable OnItemSelectedChangeListener selectedChangeListener) {
        this.itemSelectedChangeListener = selectedChangeListener;
    }

    public interface OnItemSelectedChangeListener {
        void onItemSelectedChange(int position, boolean isSelected);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (mSelectionHelper != null) {
            mSelectionHelper.unregisterSelectionObserver(this);
            mSelectionHelper.unregisterHolderClickObserver(this);
            mSelectionHelper = null;
        }
    }
}
