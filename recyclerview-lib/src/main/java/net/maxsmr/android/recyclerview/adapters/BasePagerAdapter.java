package net.maxsmr.android.recyclerview.adapters;

import android.view.View;
import android.view.ViewGroup;

import androidx.viewpager.widget.PagerAdapter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class BasePagerAdapter<I, VH extends BaseRecyclerViewAdapter.ViewHolder<I>> extends PagerAdapter {

    @NotNull
    private final List<I> items = new ArrayList<>();

    protected BasePagerAdapter() {
        this(null);
    }

    protected BasePagerAdapter(@Nullable List<I> items) {
        setItems(items);
    }

    @NotNull
    public ArrayList<I> getItems() {
        return new ArrayList<>(items);
    }

    public void setItems(Collection<I> items) {
        this.items.clear();
        if (items != null) {
            this.items.addAll(items);
        }
        notifyDataSetChanged();
    }

    public I getItem(int position) {
        rangeCheck(position);
        return items.get(position);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public int getItemPosition(@NotNull Object object) {
        return POSITION_NONE;
    }

    @Override
    public boolean isViewFromObject(@NotNull View view, @NotNull Object o) {
        return view == o;
    }

    @NotNull
    @Override
    public View instantiateItem(@NotNull ViewGroup container, int position) {
        final VH holder = createViewHolder(container, position);

        final int count = getCount();
        final I item = getItem(position);

        bindItem(holder, position, item, count);

        container.addView(holder.itemView);
        return holder.itemView;
    }

    @Override
    public void destroyItem(@NotNull ViewGroup container, int position, @NotNull Object object) {
        if (object instanceof View) {
            container.removeView((View) object);
        }
    }

    public boolean isEmpty() {
        return getCount() == 0;
    }

    @NotNull
    protected abstract VH createViewHolder(@NotNull ViewGroup container, int position);

    protected void bindItem(@NotNull VH holder, int position, @Nullable I item, int count) {
        final boolean isEmpty = isItemEmpty(position, item);
        if (item != null && !isEmpty) {
            holder.bindData(position, item, count);
        } else {
            holder.bindEmptyData(position, item, count);
        }
    }

    protected boolean isItemEmpty(int position, I item) {
        return item == null;
    }

    private void rangeCheck(int position) {
        if (position < 0 || position >= getCount()) {
            throw new IllegalArgumentException("Incorrect position: " + position);
        }
    }
}
