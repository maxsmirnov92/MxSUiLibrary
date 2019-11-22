package com.bejibx.android.recyclerview.selection;


import androidx.recyclerview.widget.RecyclerView;

public interface HolderClickListener {

    void onHolderClick(RecyclerView.ViewHolder holder);

    boolean onHolderLongClick(RecyclerView.ViewHolder holder);
}

