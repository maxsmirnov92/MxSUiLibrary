package net.maxsmr.android.recyclerview.views.decoration

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.Pair
import android.view.View
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.util.*

/**
 * Реализация [RecyclerView.ItemDecoration] с настраиваемыми
 * отступами и Drawable-разделителями в различных ориентациях
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
open class DividerSpacingItemDecoration private constructor(builder: Builder) : RecyclerView.ItemDecoration() {

    val dividerMargins: Pair<Int, Int>
        get() = Pair(dividerStartMarginPx, dividerEndMarginPx)

    /**
     * Ориентации для которых применять разделитель/отступы
     */
    private val orientations: MutableSet<Int> = mutableSetOf()

    /**
     * Дополнительное условие для decoration для этой позиции - основное проверяется при null или true этого
     */
    var additionalItemDecoratedValidator: ((Int) -> Boolean)? = null

    var settings: DecorationSettings = DecorationSettings.defaultDecorationSettings

    var isReverse = false

    var divider: Drawable? = null

    /**
     * Отступ между элементами;
     * при ненулевом значении будет использовано оно;
     * если задан разделитель, то его intrinsic значения
     */
    var spacePx = 0
        set(value) {
            require(value >= 0) { "incorrect space: $spacePx" }
            field = value
        }

    /**
     * Отступ от левого края для разделителя
     * при ориентации [RecyclerView.VERTICAL]
     * и от верха при [RecyclerView.HORIZONTAL]
     * в абсолютных пикселях
     */
    private var dividerStartMarginPx = 0

    /**
     * Отступ от правого края для разделителя
     * при ориентации [RecyclerView.VERTICAL]
     * и от низа при [RecyclerView.HORIZONTAL]
     * в абсолютных пикселях
     */
    private var dividerEndMarginPx = 0

    init {
        settings = builder.settings
        isReverse = builder.isReverse
        divider = builder.divider
        spacePx = builder.spacePx
        setOrientations(builder.orientations)
        setDividerMargins(builder.dividerStartMarginPx, builder.dividerEndMarginPx)
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)
        if (hasOrientation(RecyclerView.VERTICAL)) {
            drawVertical(c, parent)
        }
        if (hasOrientation(RecyclerView.HORIZONTAL)) {
            drawHorizontal(c, parent)
        }
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        if (isDecorated(view, parent, false)) {
            var width: Int
            var height: Int
            divider.let { divider ->
                if (divider != null && spacePx == 0) {
                    width = divider.intrinsicWidth
                    height = divider.intrinsicHeight
                } else {
                    width = spacePx
                    height = spacePx
                }
            }
            if (width < 0) {
                width = 0
            }
            if (height < 0) {
                height = 0
            }
            if (hasOrientation(RecyclerView.VERTICAL)) {
                if (!isReverse) {
                    outRect[0, 0, 0] = height
                } else {
                    outRect[0, height, 0] = 0
                }
            }
            if (hasOrientation(RecyclerView.HORIZONTAL)) {
                if (!isReverse) {
                    outRect[0, 0, width] = 0
                } else {
                    outRect[width, 0, 0] = 0
                }
            }
        }
    }

    fun getOrientations() = orientations.toList()

    fun setOrientations(orientations: Collection<Int>) {
        require(orientations.all { it == RecyclerView.HORIZONTAL || it == RecyclerView.VERTICAL }) { "Incorrect orientation" }
        this.orientations.clear()
        this.orientations.addAll(orientations)
        require(this.orientations.isNotEmpty()) { "No orientations specified" }
    }

    fun setDividerMargins(startMarginPx: Int, endMarginPx: Int) {
        require(startMarginPx >= 0) { "Incorrect dividerStartMarginPx: $startMarginPx" }
        require(endMarginPx >= 0) { "Incorrect dividerEndMarginPx: $startMarginPx" }
        dividerStartMarginPx = startMarginPx
        dividerEndMarginPx = endMarginPx
    }

    private fun isDecorated(view: View, parent: RecyclerView, dividerOrSpacing: Boolean): Boolean {
        val adapter = parent.adapter ?: throw RuntimeException("Adapter not set")
        val childPos = parent.getChildAdapterPosition(view)
        var result: Boolean = additionalItemDecoratedValidator?.invoke(childPos) != false
        if (result) {
            result = when (settings.mode) {
                DecorationSettings.Mode.ALL -> true
                DecorationSettings.Mode.ALL_EXCEPT_FIRST -> childPos > 0
                DecorationSettings.Mode.ALL_EXCEPT_LAST -> childPos < adapter.itemCount - 1
                DecorationSettings.Mode.ALL_EXCEPT_FIRST_AND_LAST -> childPos > 0 && childPos < adapter.itemCount - 1
                DecorationSettings.Mode.FIRST -> childPos == 0
                DecorationSettings.Mode.LAST -> childPos == adapter.itemCount - 1
                DecorationSettings.Mode.FIRST_AND_LAST -> childPos == 0 || childPos == adapter.itemCount - 1
                DecorationSettings.Mode.CUSTOM -> if (dividerOrSpacing) settings.getDividerPositions().contains(childPos) else settings.getDividerPositions().contains(childPos) || settings.getSpacingPositions().contains(childPos)
            }
        }
        return result
    }

    private fun drawVertical(c: Canvas, parent: RecyclerView) {
        divider?.let { divider ->
            val left = parent.paddingLeft + dividerStartMarginPx
            val right = parent.width - parent.paddingRight - dividerEndMarginPx
            val childCount = parent.childCount
            for (i in 0 until childCount) {
                val child = parent.getChildAt(i)
                if (isDecorated(child, parent, true)) {
                    val params = child.layoutParams as RecyclerView.LayoutParams
                    val top: Int
                    val bottom: Int
                    var height = divider.intrinsicHeight
                    if (height < 0) {
                        height = 0
                    }
                    if (!isReverse) {
                        bottom = child.top + params.topMargin
                        top = bottom + height
                    } else {
                        top = child.bottom + params.bottomMargin
                        bottom = top + height
                    }
                    divider.setBounds(left, top, right, bottom)
                    divider.draw(c)
                }
            }
        }
    }

    private fun drawHorizontal(c: Canvas, parent: RecyclerView) {
        divider?.let { divider ->
            val top = parent.paddingTop + dividerStartMarginPx
            val bottom = parent.height - parent.paddingBottom - dividerEndMarginPx
            val childCount = parent.childCount
            for (i in 0 until childCount) {
                val child = parent.getChildAt(i)
                if (isDecorated(child, parent, true)) {
                    val params = child.layoutParams as RecyclerView.LayoutParams
                    val left: Int
                    val right: Int
                    var width = divider.intrinsicWidth
                    if (width < 0) {
                        width = 0
                    }
                    if (!isReverse) {
                        left = child.right + params.rightMargin
                        right = left + width
                    } else {
                        right = child.left + params.leftMargin
                        left = right + width
                    }
                    divider.setBounds(left, top, right, bottom)
                    divider.draw(c)
                }
            }
        }
    }

    private fun hasOrientation(@RecyclerView.Orientation orientation: Int): Boolean {
        return orientations.contains(orientation)
    }

    /**
     * Опции для отображения decorations
     */
    class DecorationSettings @JvmOverloads constructor(val mode: Mode = Mode.ALL_EXCEPT_LAST, dividerPositions: Collection<Int>? = null, spacingPositions: Collection<Int>? = null) {

        /**
         * Позиции для отображения разделителей (для режима [Mode.CUSTOM])
         */
        private val dividerPositions: MutableSet<Int> = LinkedHashSet()

        /**
         * Позиции для отображения отступов (для режима [Mode.CUSTOM])
         */
        private val spacingPositions: MutableSet<Int> = LinkedHashSet()

        fun getDividerPositions(): Set<Int> {
            return Collections.unmodifiableSet(dividerPositions)
        }

        fun getSpacingPositions(): Set<Int> {
            return Collections.unmodifiableSet(spacingPositions)
        }

        /**
         * Режим отображения decorations в позициях
         */
        enum class Mode {
            /**
             * Все
             */
            ALL,

            /**
             * Все, кроме первого
             */
            ALL_EXCEPT_FIRST,

            /**
             * Все, кроме последнего
             */
            ALL_EXCEPT_LAST,

            /**
             * Все, кроме первого и последнего
             */
            ALL_EXCEPT_FIRST_AND_LAST,

            /**
             * Только первый
             */
            FIRST,

            /**
             * Только последний
             */
            LAST,

            /**
             * Первый и последний
             */
            FIRST_AND_LAST,  /*
             * В указанных позициях
             */
            CUSTOM
        }

        companion object {
            val defaultDecorationSettings: DecorationSettings
                get() = DecorationSettings()
        }

        init {
            if (dividerPositions != null) {
                this.dividerPositions.addAll(dividerPositions)
            }
            if (spacingPositions != null) {
                this.spacingPositions.addAll(spacingPositions)
            }
        }
    }

    /**
     * Builder для создания инстанса [DividerSpacingItemDecoration]
     */
    class Builder {

        var settings = DecorationSettings.defaultDecorationSettings
            private set
        val orientations: MutableSet<Int> = HashSet()
        var isReverse = false
            private set
        var divider: Drawable? = null
            private set
        var spacePx = 0
            private set
        var dividerStartMarginPx = 0
            private set
        var dividerEndMarginPx = 0
            private set

        fun setSettings(settings: DecorationSettings): Builder {
            this.settings = settings
            return this
        }

        fun addOrientations(orientations: Set<Int>): Builder {
            this.orientations.addAll(orientations)
            return this
        }

        fun addOrientation(@RecyclerView.Orientation orientation: Int): Builder {
            orientations.add(orientation)
            return this
        }

        fun setReverse(reverse: Boolean): Builder {
            isReverse = reverse
            return this
        }

        fun setDivider(context: Context?, @DrawableRes dividerResId: Int): Builder {
            setDivider(ContextCompat.getDrawable(context!!, dividerResId))
            return this
        }

        fun setDividerFromAttrs(context: Context): Builder {
            val styledAttributes = context.obtainStyledAttributes(ATTRS)
            setDivider(styledAttributes.getDrawable(0))
            styledAttributes.recycle()
            return this
        }

        fun setDivider(divider: Drawable?): Builder {
            this.divider = divider
            return this
        }

        fun setSpacePx(spacePx: Int): Builder {
            this.spacePx = spacePx
            return this
        }

        fun setDividerMargins(startMarginPx: Int, endMarginPx: Int): Builder {
            dividerStartMarginPx = startMarginPx
            dividerEndMarginPx = endMarginPx
            return this
        }

        fun build(): DividerSpacingItemDecoration {
            return DividerSpacingItemDecoration(this)
        }

        companion object {
            private val ATTRS = intArrayOf(
                    android.R.attr.listDivider
            )
        }
    }
}