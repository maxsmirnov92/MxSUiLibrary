package com.bejibx.android.recyclerview.selection;

import android.support.v7.widget.RecyclerView;

public interface SelectionListener {

    void onSelectedChanged(RecyclerView.ViewHolder holder, boolean isSelected, boolean fromUser);

    void onReselected(RecyclerView.ViewHolder holder, boolean fromUser);

    void onSelectableChanged(boolean isSelectable);

    void onAllowTogglingSelectionChanged(boolean isAllowed);
}
