package com.bejibx.android.recyclerview.selection;

import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

public interface SelectionListener {

    /**
     * @param position from holder
     * @return fixed position (for example for infinite scroll)
     */
    int getListPosition(int position);

    boolean isSelectionAtPositionAllowed(int position);

    void onSelectedChanged(@NotNull RecyclerView.ViewHolder holder, boolean isSelected, boolean fromUser);

    void onReselected(@NotNull RecyclerView.ViewHolder holder, boolean fromUser);

    void onSelectableChanged(boolean isSelectable);

    void onAllowResetSelectionChanged(boolean isAllowed);

    void handleSelected(@NotNull RecyclerView.ViewHolder holder, boolean isSelected);
}
