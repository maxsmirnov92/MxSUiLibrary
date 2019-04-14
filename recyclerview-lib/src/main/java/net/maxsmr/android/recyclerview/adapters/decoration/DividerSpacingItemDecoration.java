package net.maxsmr.android.recyclerview.adapters.decoration;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.View;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Реализация {@linkplain RecyclerView.ItemDecoration} с настраиваемыми
 * отступами и Drawable-разделителями в различных ориентациях
 */
public class DividerSpacingItemDecoration extends RecyclerView.ItemDecoration {

    public static final int HORIZONTAL_LIST = LinearLayoutManager.HORIZONTAL;

    public static final int VERTICAL_LIST = LinearLayoutManager.VERTICAL;

    private DecorationSettings settings;

    /**
     * Ориентации для которых применять разделитель/отступы
     */
    private Set<Integer> orientations;

    private boolean isReverse;

    @Nullable
    private Drawable divider;

    /**
     * Отступ между элементами;
     * при ненулевом значении будет использовано оно;
     * если задан разделитель, то его intrinsic значения
     */
    private int spacePx;

    /**
     * Отступ от левого края для разделителя
     * при ориентации {@linkplain DividerSpacingItemDecoration#VERTICAL_LIST}
     * и от верха при {@linkplain DividerSpacingItemDecoration#HORIZONTAL_LIST}
     * в абсолютных пикселях
     */
    private int dividerStartMarginPx;

    /**
     * Отступ от правого края для разделителя
     * при ориентации {@linkplain DividerSpacingItemDecoration#VERTICAL_LIST}
     * и от низа при {@linkplain DividerSpacingItemDecoration#HORIZONTAL_LIST}
     * в абсолютных пикселях
     */
    private int dividerEndMarginPx;

    private DividerSpacingItemDecoration(@NotNull Builder builder) {
        setSettings(builder.settings);
        setOrientations(builder.orientations);
        setReverse(builder.isReverse);
        setDivider(builder.divider);
        setSpacePx(builder.spacePx);
        setDividerMargins(builder.dividerStartMarginPx, builder.dividerEndMarginPx);
    }

    public boolean isReverse() {
        return isReverse;
    }

    public void setReverse(boolean reverse) {
        isReverse = reverse;
    }

    @NotNull
    public DecorationSettings getSettings() {
        if (settings == null) {
            settings = DecorationSettings.getDefaultDecorationSettings();
        }
        return settings;
    }

    public void setSettings(@NotNull DecorationSettings settings) {
        this.settings = settings;
    }

    @NotNull
    public Set<Integer> getOrientations() {
        if (orientations == null) {
            orientations = new LinkedHashSet<>();
        }
        return new LinkedHashSet<>(orientations);
    }

    public void setOrientations(Collection<Integer> orientations) {
        this.orientations = new LinkedHashSet<>();
        if (orientations != null) {
            for (Integer orientation : orientations) {
                if (orientation != null) {
                    if (orientation != HORIZONTAL_LIST && orientation != VERTICAL_LIST) {
                        throw new IllegalArgumentException("Incorrect orientation: " + orientation);
                    }
                    this.orientations.add(orientation);
                }
            }
        }
        if (this.orientations.isEmpty()) {
            throw new IllegalArgumentException("No orientations specified");
        }
    }

    @Nullable
    public Drawable getDivider() {
        return divider;
    }

    public void setDivider(@Nullable Drawable divider) {
        this.divider = divider;
    }

    public int getSpacePx() {
        return spacePx;
    }

    public void setSpacePx(int spacePx) {
        if (spacePx < 0) {
            throw new IllegalArgumentException("incorrect space: " + spacePx);
        }
        this.spacePx = spacePx;
    }

    @NotNull
    public Pair<Integer, Integer> getDividerMargins() {
        return new Pair<>(dividerStartMarginPx, dividerEndMarginPx);
    }

    public void setDividerMargins(int startMarginPx, int endMarginPx) {
        if (startMarginPx < 0) {
            throw new IllegalArgumentException("Incorrect dividerStartMarginPx: " + startMarginPx);
        }
        if (endMarginPx < 0) {
            throw new IllegalArgumentException("Incorrect dividerEndMarginPx: " + startMarginPx);
        }
        this.dividerStartMarginPx = startMarginPx;
        this.dividerEndMarginPx = endMarginPx;
    }

    @Override
    public void onDraw(@NotNull Canvas c, @NotNull RecyclerView parent, @NotNull RecyclerView.State state) {
        super.onDraw(c, parent, state);
        if (hasOrientation(VERTICAL_LIST)) {
            drawVertical(c, parent);
        }
        if (hasOrientation(HORIZONTAL_LIST)) {
            drawHorizontal(c, parent);
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        if (isDecorated(view, parent, false)) {

            int width = 0;
            int height = 0;

            if (divider != null && spacePx == 0) {
                width = divider.getIntrinsicWidth();
                height = divider.getIntrinsicHeight();
            } else {
                width = spacePx;
                height = spacePx;
            }
            if (width < 0) {
                width = 0;
            }
            if (height < 0) {
                height = 0;
            }

            if (hasOrientation(VERTICAL_LIST)) {
                if (!isReverse) {
                    outRect.set(0, 0, 0, height);
                } else {
                    outRect.set(0, height, 0, 0);
                }
            }
            if (hasOrientation(HORIZONTAL_LIST)) {
                if (!isReverse) {
                    outRect.set(0, 0, width, 0);
                } else {
                    outRect.set(width, 0, 0, 0);
                }
            }
        }
    }

    protected boolean isDecorated(View view, RecyclerView parent, boolean dividerOrSpacing) {

        final RecyclerView.Adapter adapter = parent.getAdapter();

        if (adapter == null) {
            throw new RuntimeException("Adapter not set");
        }

        int childPos = parent.getChildAdapterPosition(view);

        boolean result;

        switch (settings.getMode()) {
            case ALL:
                result = true;
                break;
            case ALL_EXCEPT_LAST:
                result = childPos < adapter.getItemCount() - 1;
                break;
            case FIRST:
                result = childPos == 0;
                break;
            case LAST:
                result = childPos == adapter.getItemCount() - 1;
                break;
            case CUSTOM:
                result = dividerOrSpacing ? settings.getDividerPositions().contains(childPos) : (settings.getDividerPositions().contains(childPos) || settings.getSpacingPositions().contains(childPos));
                break;
            default:
                throw new IllegalArgumentException("incorrect " + DecorationSettings.Mode.class.getSimpleName() + ": " + settings.getMode());
        }

        return result;
    }

    private void drawVertical(Canvas c, RecyclerView parent) {
        if (divider != null) {

            final int left = parent.getPaddingLeft() + dividerStartMarginPx;
            final int right = parent.getWidth() - parent.getPaddingRight() - dividerEndMarginPx;

            final int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);
                if (isDecorated(child, parent, true)) {
                    final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                    final int top;
                    final int bottom;
                    int height = divider.getIntrinsicHeight();
                    if (height < 0) {
                        height = 0;
                    }
                    if (!isReverse) {
                        top = child.getBottom() + params.bottomMargin;
                        bottom = top + height;
                    } else {
                        bottom = child.getTop() + params.topMargin;
                        top = bottom + height;
                    }
                    divider.setBounds(left, top, right, bottom);
                    divider.draw(c);
                }
            }
        }
    }

    private void drawHorizontal(Canvas c, RecyclerView parent) {
        if (divider != null) {

            final int top = parent.getPaddingTop() + dividerStartMarginPx;
            final int bottom = parent.getHeight() - parent.getPaddingBottom() - dividerEndMarginPx;

            final int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);
                if (isDecorated(child, parent, true)) {
                    final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                    final int left;
                    final int right;
                    int width = divider.getIntrinsicWidth();
                    if (width < 0) {
                        width = 0;
                    }
                    if (!isReverse) {
                        left = child.getRight() + params.rightMargin;
                        right = left + width;
                    } else {
                        right = child.getLeft() + params.leftMargin;
                        left = right + width;
                    }
                    divider.setBounds(left, top, right, bottom);
                    divider.draw(c);
                }
            }
        }
    }

    private boolean hasOrientation(@RecyclerView.Orientation int orientation) {
        return orientations.contains(orientation);
    }

    /**
     * Опции для отображения decorations
     */
    public static class DecorationSettings {

        @NotNull
        private final Mode mode;

        /**
         * Позиции для отображения разделителей (для режима {@linkplain Mode#CUSTOM})
         */
        @NotNull
        private final Set<Integer> dividerPositions = new LinkedHashSet<>();

        /**
         * Позиции для отображения отступов (для режима {@linkplain Mode#CUSTOM})
         */
        private final Set<Integer> spacingPositions = new LinkedHashSet<>();

        @NotNull
        public Mode getMode() {
            return mode;
        }

        @NotNull
        public Set<Integer> getDividerPositions() {
            return Collections.unmodifiableSet(dividerPositions);
        }

        public Set<Integer> getSpacingPositions() {
            return Collections.unmodifiableSet(spacingPositions);
        }

        public DecorationSettings() {
            this(Mode.ALL_EXCEPT_LAST);
        }

        public DecorationSettings(@NotNull Mode mode) {
            this(mode, null, null);
        }

        public DecorationSettings(@NotNull Mode mode, @Nullable Collection<Integer> dividerPositions, @Nullable Collection<Integer> spacingPositions) {
            this.mode = mode;
            if (dividerPositions != null) {
                this.dividerPositions.addAll(dividerPositions);
            }
            if (spacingPositions != null) {
                this.spacingPositions.addAll(spacingPositions);
            }
        }

        /**
         * Режим отображения decorations в позициях
         */
        public enum Mode {

            /**
             * Все
             */
            ALL,

            /**
             * Все,
             * кроме последнего
             */
            ALL_EXCEPT_LAST,

            /**
             * Только первый
             */
            FIRST,

            /**
             * Только последний
             */
            LAST,

            /*
             * В указанных позициях
             */
            CUSTOM
        }

        public static DecorationSettings getDefaultDecorationSettings() {
            return new DecorationSettings();
        }
    }

    /**
     * Builder для создания инстанса {@linkplain DividerSpacingItemDecoration}
     */
    public static class Builder {

        private static final int[] ATTRS = new int[]{
                android.R.attr.listDivider
        };

        @NotNull
        private DecorationSettings settings = DecorationSettings.getDefaultDecorationSettings();

        @NotNull
        private Set<Integer> orientations = new HashSet<>();

        private boolean isReverse = false;

        @Nullable
        private Drawable divider;

        private int spacePx = 0;

        private int dividerStartMarginPx = 0;

        private int dividerEndMarginPx = 0;

        @NotNull
        public Builder setSettings(@NotNull DecorationSettings settings) {
            this.settings = settings;
            return this;
        }

        @NotNull
        public Builder setOrientations(@Nullable Set<Integer> orientations) {
            this.orientations.clear();
            if (orientations != null) {
                this.orientations.addAll(orientations);
            }
            return this;
        }

        @NotNull
        public Builder setOrientation(int orientation) {
            orientations.add(orientation);
            return this;
        }

        @NotNull
        public Builder setReverse(boolean reverse) {
            isReverse = reverse;
            return this;
        }

        @NotNull
        public Builder setDivider(Context context, @DrawableRes int dividerResId) {
            setDivider(ContextCompat.getDrawable(context, dividerResId));
            return this;
        }

        @NotNull
        public Builder setDividerFromAttrs(@NotNull Context context) {
            final TypedArray styledAttributes = context.obtainStyledAttributes(ATTRS);
            setDivider(styledAttributes.getDrawable(0));
            styledAttributes.recycle();
            return this;
        }

        @NotNull
        public Builder setDivider(@Nullable Drawable divider) {
            this.divider = divider;
            return this;
        }

        @NotNull
        public Builder setSpacePx(int spacePx) {
            this.spacePx = spacePx;
            return this;
        }

        @NotNull
        public Builder setDividerMargins(int startMarginPx, int endMarginPx) {
            this.dividerStartMarginPx = startMarginPx;
            this.dividerEndMarginPx = endMarginPx;
            return this;
        }

        public DividerSpacingItemDecoration build() {
            return new DividerSpacingItemDecoration(this);
        }
    }
}