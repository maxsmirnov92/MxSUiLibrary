package net.maxsmr.android.recyclerview.adapters.base.selection.multi;


import net.maxsmr.android.recyclerview.adapters.base.BaseRecyclerViewAdapter;

interface HolderClickListener {

    void onHolderClick(int position, BaseRecyclerViewAdapter.ViewHolder holder);

    boolean onHolderLongClick(int position, BaseRecyclerViewAdapter.ViewHolder holder);
}

