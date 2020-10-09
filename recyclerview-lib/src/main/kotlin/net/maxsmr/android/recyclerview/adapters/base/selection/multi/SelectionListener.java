package net.maxsmr.android.recyclerview.adapters.base.selection.multi;

import net.maxsmr.android.recyclerview.adapters.base.selection.BaseSelectionRecyclerViewAdapter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

interface SelectionListener {

    /**
     * @param position from holder
     * @return fixed position (for example for infinite scroll)
     */
    int getListPosition(int position);

    boolean canSelectAtPosition(int position);

    void onSelectionChanged(int position, @Nullable BaseSelectionRecyclerViewAdapter.ViewHolder<?> holder, boolean isSelected, boolean fromUser);

    void onReselected(int position, @Nullable BaseSelectionRecyclerViewAdapter.ViewHolder<?> holder, boolean fromUser);

    void onSelectableChanged(boolean isSelectable);

    void onAllowResetSelectionChanged(boolean isAllowed);

    void handleSelected(@NotNull BaseSelectionRecyclerViewAdapter.ViewHolder<?> holder, boolean isSelected);
}
