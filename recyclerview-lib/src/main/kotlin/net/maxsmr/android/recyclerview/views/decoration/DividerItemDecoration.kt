package net.maxsmr.android.recyclerview.views.decoration

import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Реализация [RecyclerView.ItemDecoration] с настраиваемыми
 * отступами и Drawable-разделителями в различных ориентациях
 * @param orientation ориентации, для которых применять разделитель/отступы
 * @param itemDecoratedValidator дополнительное условие для decoration для этой позиции
 * (при наличии divider); основное проверяется при null или true этого
 * @param dividerBlock возврат [Divider] в этом блоке при отсутствии основного [divider]
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
open class DividerItemDecoration(
        private val mode: Mode?,
        private val dividerPosition: DividerPosition,
        private val orientation: Int,
        private var divider: Divider?,
        private val itemDecoratedValidator: ((Int) -> Boolean)? = null,
        private var dividerBlock: ((position: Int) -> Divider?)?
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        val divider = divider(view, parent) ?: return
        var width = divider.width(view.context, true)
        var height = divider.height(view.context, true)
        if (width < 0) {
            width = 0
        }
        if (height < 0) {
            height = 0
        }
        when (orientation) {
            RecyclerView.HORIZONTAL -> {
                if (dividerPosition == DividerPosition.BEFORE_ITEM) {
                    outRect.set(width, 0, 0, 0)
                } else {
                    outRect.set(0, 0, width, 0)
                }
            }
            RecyclerView.VERTICAL -> {
                if (dividerPosition == DividerPosition.BEFORE_ITEM) {
                    outRect.set(0, height, 0, 0)
                } else {
                    outRect.set(0, 0, 0, height)
                }
            }
        }
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)
        when (orientation) {
            RecyclerView.HORIZONTAL -> drawHorizontal(c, parent)
            RecyclerView.VERTICAL -> drawVertical(c, parent)
        }
    }

    private fun isDecorated(view: View, parent: RecyclerView): Boolean {
        val adapter = parent.adapter ?: throw RuntimeException("Adapter not set")
        val childPos = parent.getChildAdapterPosition(view)
        return itemDecoratedValidator?.invoke(childPos) != false
                && mode?.isApplicable(childPos, adapter.itemCount) != false
    }

    private fun drawHorizontal(c: Canvas, parent: RecyclerView) {
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            val divider = divider(child, parent) ?: continue

            val top = parent.paddingTop + divider.marginTop(parent.context)
            val bottom = parent.height - parent.paddingBottom - divider.marginBottom(parent.context)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val left: Int
            val right: Int
            var width = divider.width(parent.context, false)
            if (width < 0) {
                width = 0
            }
            if (dividerPosition == DividerPosition.BEFORE_ITEM) {
                right = child.left - params.leftMargin - divider.marginEnd(parent.context)
                left = right - width
            } else {
                left = child.right + params.rightMargin + divider.marginStart(parent.context)
                right = left + width
            }
            divider.draw(c, left, top, right, bottom)
        }
    }

    private fun drawVertical(c: Canvas, parent: RecyclerView) {
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            val divider = divider(child, parent) ?: continue

            val left = parent.paddingLeft + divider.marginStart(parent.context)
            val right = parent.width - parent.paddingRight - divider.marginEnd(parent.context)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val top: Int
            val bottom: Int
            var height = divider.height(parent.context, false)
            if (height < 0) {
                height = 0
            }
            if (dividerPosition == DividerPosition.BEFORE_ITEM) {
                bottom = child.top - params.topMargin - divider.marginBottom(parent.context)
                top = bottom - height
            } else {
                top = child.bottom + params.bottomMargin + divider.marginTop(parent.context)
                bottom = top + height
            }
            divider.draw(c, left, top, right, bottom)
        }
    }

    private fun divider(view: View, parent: RecyclerView): Divider? {
        val childPos = parent.getChildAdapterPosition(view)
        return divider?.takeIf { isDecorated(view, parent) } ?: dividerBlock?.invoke(childPos)
    }

    /**
     * Режим отображения decorations в позициях
     */
    enum class Mode {

        /**
         * Все
         */
        ALL {
            override fun isApplicable(childPos: Int, itemCount: Int): Boolean = true
        },

        /**
         * Все, кроме первого
         */
        ALL_EXCEPT_FIRST {
            override fun isApplicable(childPos: Int, itemCount: Int): Boolean = childPos > 0
        },

        /**
         * Все, кроме последнего
         */
        ALL_EXCEPT_LAST {
            override fun isApplicable(childPos: Int, itemCount: Int): Boolean = childPos < itemCount - 1
        },

        /**
         * Все, кроме первого и последнего
         */
        ALL_EXCEPT_FIRST_AND_LAST {
            override fun isApplicable(childPos: Int, itemCount: Int): Boolean = childPos > 0 && childPos < itemCount - 1
        },

        /**
         * Только первый
         */
        FIRST {
            override fun isApplicable(childPos: Int, itemCount: Int): Boolean = childPos == 0
        },

        /**
         * Только последний
         */
        LAST {
            override fun isApplicable(childPos: Int, itemCount: Int): Boolean = childPos == itemCount - 1
        },

        /**
         * Первый и последний
         */
        FIRST_AND_LAST {
            override fun isApplicable(childPos: Int, itemCount: Int): Boolean = childPos == 0 || childPos == itemCount - 1
        };

        abstract fun isApplicable(childPos: Int, itemCount: Int): Boolean
    }

    enum class DividerPosition {
        BEFORE_ITEM, AFTER_ITEM;
    }
}