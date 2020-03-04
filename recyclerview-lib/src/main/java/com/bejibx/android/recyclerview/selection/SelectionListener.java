package com.bejibx.android.recyclerview.selection;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

public interface SelectionListener {

    void onSelectedChanged(@NotNull RecyclerView.ViewHolder holder, boolean isSelected, boolean fromUser);

    void onReselected(@NotNull RecyclerView.ViewHolder holder, boolean fromUser);

    void onSelectableChanged(boolean isSelectable);

    void onAllowResetSelectionChanged(boolean isAllowed);

    void handleSelected(@NotNull View selectableView, boolean isSelected);
}
