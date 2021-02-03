package net.maxsmr.android.recyclerview.views.decoration

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat

sealed class Divider {

    abstract fun width(context: Context, withMargins: Boolean): Int
    abstract fun height(context: Context, withMargins: Boolean): Int

    abstract fun marginStart(context: Context): Int
    abstract fun marginEnd(context: Context): Int
    abstract fun marginTop(context: Context): Int
    abstract fun marginBottom(context: Context): Int

    abstract fun draw(canvas: Canvas, left: Int, top: Int, right: Int, bottom: Int)

    protected fun Context.toPx(srcUnit: Int, value: Int): Int = TypedValue.applyDimension(srcUnit, value.toFloat(), resources.displayMetrics).toInt()

    /**
     * Пустое пространство в качестве разделителя
     */
    class Space @JvmOverloads constructor(val value: Int, val unit: Int = TypedValue.COMPLEX_UNIT_DIP) : Divider() {

        constructor(context: Context, @DimenRes dimen: Int) : this(context.resources.getDimensionPixelSize(dimen), TypedValue.COMPLEX_UNIT_PX)

        override fun width(context: Context, withMargins: Boolean): Int {
            return height(context, withMargins)
        }

        override fun height(context: Context, withMargins: Boolean): Int {
            return context.toPx(unit, value)
        }

        override fun marginStart(context: Context): Int = 0
        override fun marginEnd(context: Context): Int = 0
        override fun marginTop(context: Context): Int = 0
        override fun marginBottom(context: Context): Int = 0

        override fun draw(canvas: Canvas, left: Int, top: Int, right: Int, bottom: Int) {

        }
    }

    /**
     * Изображение в качестве разделителя
     *
     * @param marginStart доп отступ слева от изображения
     * @param marginTop доп отступ сверху от изображения
     * @param marginEnd доп отступ справа от изображения
     * @param marginBottom доп отступ снизу от изображения
     * @param marginUnit единицы измерения отступа
     */
    class Image @JvmOverloads constructor(
            val src: Drawable,
            private val marginStart: Int = 0,
            private val marginTop: Int = 0,
            private val marginEnd: Int = 0,
            private val marginBottom: Int = 0,
            private val marginUnit: Int = TypedValue.COMPLEX_UNIT_DIP
    ) : Divider() {

        override fun width(context: Context, withMargins: Boolean): Int {
            return src.intrinsicWidth + if (withMargins) context.toPx(marginUnit, marginStart + marginEnd) else 0
        }

        override fun height(context: Context, withMargins: Boolean): Int {
            return src.intrinsicHeight + if (withMargins) context.toPx(marginUnit, marginTop + marginBottom) else 0
        }

        override fun marginStart(context: Context): Int {
            return context.toPx(marginUnit, marginStart)
        }

        override fun marginEnd(context: Context): Int {
            return context.toPx(marginUnit, marginEnd)
        }

        override fun marginTop(context: Context): Int {
            return context.toPx(marginUnit, marginTop)
        }

        override fun marginBottom(context: Context): Int {
            return context.toPx(marginUnit, marginBottom)
        }

        override fun draw(canvas: Canvas, left: Int, top: Int, right: Int, bottom: Int) {
            src.setBounds(left, top, right, bottom)
            src.draw(canvas)
        }

        companion object {

            /**
             * Создает разделитель с заданным цветом
             *
             * @param size размер разделителя в [unit]. Для горизонтального списка - ширина разделителя, для вертикального - высота
             * @param marginStart дополнительный отступ от разделителя слева
             * @param marginTop дополнительный отступ от разделителя сверху
             * @param marginEnd дополнительный отступ от разделителя справа
             * @param marginBottom дополнительный отступ от разделителя снизу
             */
            @JvmStatic
            @JvmOverloads
            fun createColor(context: Context?,
                            @ColorRes colorRes: Int,
                            size: Int,
                            marginStart: Int = 0,
                            marginTop: Int = 0,
                            marginEnd: Int = 0,
                            marginBottom: Int = 0,
                            unit: Int = TypedValue.COMPLEX_UNIT_DIP): Image? {
                context ?: return null
                val s = TypedValue.applyDimension(unit, size.toFloat(), context.resources.displayMetrics).toInt()
                if (s <= 0) return null
                val image = Bitmap.createBitmap(s, s, Bitmap.Config.ARGB_8888)
                image.eraseColor(ContextCompat.getColor(context, colorRes))
                return Image(BitmapDrawable(context.resources, image), marginStart, marginTop, marginEnd, marginBottom, unit)
            }

            /**
             * Создает разделитель с заданным [drawableRes]
             *
             * @param marginStart дополнительный отступ от разделителя слева
             * @param marginTop дополнительный отступ от разделителя сверху
             * @param marginEnd дополнительный отступ от разделителя справа
             * @param marginBottom дополнительный отступ от разделителя снизу
             */
            @JvmStatic
            @JvmOverloads
            fun create(context: Context?,
                       @DrawableRes drawableRes: Int,
                       marginStart: Int = 0,
                       marginTop: Int = 0,
                       marginEnd: Int = 0,
                       marginBottom: Int = 0,
                       marginUnit: Int = TypedValue.COMPLEX_UNIT_DIP): Image? {
                context ?: return null
                val drawable = ContextCompat.getDrawable(context, drawableRes)
                        ?: return null
                return Image(drawable, marginStart, marginTop, marginEnd, marginBottom, marginUnit)
            }

            /**
             * Создает стандартный разделитель с ресурсом [android.R.attr.listDivider]
             *
             * @param marginStart дополнительный отступ от разделителя слева
             * @param marginTop дополнительный отступ от разделителя сверху
             * @param marginEnd дополнительный отступ от разделителя справа
             * @param marginBottom дополнительный отступ от разделителя снизу
             */
            @JvmStatic
            @JvmOverloads
            fun createDefault(context: Context?,
                              marginStart: Int = 0,
                              marginTop: Int = 0,
                              marginEnd: Int = 0,
                              marginBottom: Int = 0,
                              marginUnit: Int = TypedValue.COMPLEX_UNIT_DIP): Image? {
                context ?: return null
                val styledAttributes = context.obtainStyledAttributes(intArrayOf(android.R.attr.listDivider))
                val drawable = styledAttributes.getDrawable(0)
                styledAttributes?.recycle()
                drawable ?: return null
                return Image(drawable, marginStart, marginTop, marginEnd, marginBottom, marginUnit)
            }
        }
    }
}