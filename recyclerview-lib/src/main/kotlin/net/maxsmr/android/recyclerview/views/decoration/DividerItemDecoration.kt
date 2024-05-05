package net.maxsmr.android.recyclerview.views.decoration

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.VERTICAL

/**
 * Реализация [RecyclerView.ItemDecoration] с настраиваемыми
 * отступами и Drawable-разделителями в различных ориентациях
 */
open class DividerItemDecoration private constructor(
    private val mode: Mode?,
    private val dividerPosition: DividerPosition,
    private val orientation: Int,
    private var divider: Divider?,
    private var dividerBlock: ((position: Int) -> Divider?)?,
    private val dividerSizeAtChild: Boolean = false
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
            VERTICAL -> {
                if (dividerPosition == DividerPosition.BEFORE_ITEM) {
                    outRect.set(0, height, 0, 0)
                } else {
                    outRect.set(0, 0, 0, height)
                }
            }
            RecyclerView.HORIZONTAL -> {
                if (dividerPosition == DividerPosition.BEFORE_ITEM) {
                    outRect.set(width, 0, 0, 0)
                } else {
                    outRect.set(0, 0, width, 0)
                }
            }
        }
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)
        when (orientation) {
            VERTICAL -> drawVertical(c, parent)
            RecyclerView.HORIZONTAL -> drawHorizontal(c, parent)
        }
    }

    private fun drawVertical(c: Canvas, parent: RecyclerView) {
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            val divider = divider(child, parent) ?: continue

            val left = (if (dividerSizeAtChild) child.left else parent.paddingLeft) + divider.marginStart(parent.context)
            val right = (if (dividerSizeAtChild) child.right else (parent.width - parent.paddingRight)) - divider.marginEnd(parent.context)
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

    private fun drawHorizontal(c: Canvas, parent: RecyclerView) {
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            val divider = divider(child, parent) ?: continue

            val top = (if (dividerSizeAtChild) child.top else parent.paddingTop) + divider.marginTop(parent.context)
            val bottom = (if (dividerSizeAtChild) child.bottom else (parent.height - parent.paddingBottom)) - divider.marginBottom(parent.context)
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

    private fun divider(view: View, parent: RecyclerView): Divider? {
        val adapter = parent.adapter ?: throw RuntimeException("Adapter not set")
        val childPos = parent.getChildAdapterPosition(view)
        return divider?.takeIf { mode?.isApplicable(childPos, adapter.itemCount) ?: false }
            ?: dividerBlock?.invoke(childPos)
    }


    /**
     * Режим отображения decorations в позициях
     */
    enum class Mode {

        ALL {
            override fun isApplicable(position: Int, count: Int): Boolean = true
        },
        ALL_EXCEPT_FIRST {
            override fun isApplicable(position: Int, count: Int): Boolean = position > 0
        },
        ALL_EXCEPT_LAST {
            override fun isApplicable(position: Int, count: Int): Boolean = position < count - 1
        },
        ALL_EXCEPT_FIRST_AND_LAST {
            override fun isApplicable(position: Int, count: Int): Boolean = position > 0 && position < count - 1
        },
        FIRST {
            override fun isApplicable(position: Int, count: Int): Boolean = position == 0
        },
        LAST {
            override fun isApplicable(position: Int, count: Int): Boolean = position == count - 1
        },
        FIRST_AND_LAST {
            override fun isApplicable(position: Int, count: Int): Boolean = position == 0 || position == count - 1
        };

        abstract fun isApplicable(position: Int, count: Int): Boolean
    }

    enum class DividerPosition {
        BEFORE_ITEM, AFTER_ITEM;
    }


    /**
     * Builder для создания инстанса [BaseItemDecorator]
     */
    class Builder(internal val context: Context) {

        /**
         * Режим проверки текущей позиции на то, должна ли она быть decorated
         */
        private var mode: Mode? = null
        private var orientation: Int = VERTICAL
        private var dividerPosition = DividerPosition.AFTER_ITEM
        private var divider: Divider? = null
        private var dividerBlock: ((position: Int) -> Divider?)? = null
        private var dividerSizeAtChild = false

        /**
         * Установка ориентации разделителей. В общем случае должен совпадать с направлением скролла списка.
         * По умолчанию [VERTICAL]
         */
        fun setOrientation(orientation: Int): Builder = apply {
            this.orientation = orientation
        }

        /**
         * Установка разделителей 1 типа. Удобно использовать, если позиции для разделителей подходят
         * под один из [mode]
         *
         * @param divider разделитель
         * @param mode описание позиции разделителей
         */
        fun setDivider(divider: Divider?, mode: Mode) = apply {
            this.divider = divider
            this.mode = mode
        }

        /**
         * Установка разделителей 1 типа. Удобно использовать, если в списке нужны одинаковые разделители,
         * но на произвольных позициях.
         *
         * @param divider разделитель
         * @param block лямбда принимает позицию элемента и возвращает флаг необходимости добавления разделителя
         */
        fun setDivider(divider: Divider?, block: (position: Int) -> Boolean) = apply {
            dividerBlock = { position -> divider.takeIf { block(position) } }
        }

        /**
         * Установка разделителей разных типов. Удобно использовать, если в списке нужны несколько
         * типов разделителей или разделители с различающимися параметрами
         *
         * @param block лямбда принимает позицию элемента и возвращает разделитель, либо null если не нужен
         */
        fun setDivider(block: (position: Int) -> Divider?) = apply {
            dividerBlock = block
        }

        /**
         * Устанавливает позицию разделителя до или после элемента. По умолчанию - после
         */
        fun setDividerPosition(dividerPosition: DividerPosition) = apply {
            this.dividerPosition = dividerPosition
        }

        /**
         * Установка режима расчета размера разделителя по дочернему элементу или родительскому списку.
         * для горизантального расположения: ширина по ширине дочернего элемента или родительского списка
         * для вертикального расположения: высота по высоте дочернего элемента или родительского списка
         */
        fun setDividerSizeAtChild(sizeAtChild: Boolean) = apply {
            this.dividerSizeAtChild = sizeAtChild
        }

        fun build(): DividerItemDecoration {
            return DividerItemDecoration(mode, dividerPosition, orientation, divider, dividerBlock, dividerSizeAtChild)
        }
    }

}