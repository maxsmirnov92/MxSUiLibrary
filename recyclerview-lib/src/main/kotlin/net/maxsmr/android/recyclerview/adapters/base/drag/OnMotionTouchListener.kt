package net.maxsmr.android.recyclerview.adapters.base.drag

import android.content.Context
import android.util.Log
import android.util.TypedValue
import android.util.TypedValue.COMPLEX_UNIT_DIP
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import androidx.recyclerview.widget.RecyclerView

private const val MOTION_THRESHOLD_DP_DEFAULT = 2.0f

/**
 * OnTouchListener с распознаванием направления движения
 */
class OnMotionTouchListener(
        private val holder: RecyclerView.ViewHolder,
        private val dragListener: OnStartDragListener,
        context: Context
) : OnTouchListener {

    /**
     * Чувствительность к движению
     */
    private val motionThreshold: Float = TypedValue.applyDimension(COMPLEX_UNIT_DIP, MOTION_THRESHOLD_DP_DEFAULT, context.resources.displayMetrics)

    private var initX = 0f
    private var initY = 0f

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when(event.action) {
            MotionEvent.ACTION_DOWN -> {
                //сохраняется начальные координаты касания
                initX = event.x
                initY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                // вычисляется направление движения пальца
                val deltaX = Math.abs(event.x - initX)
                val deltaY = Math.abs(event.y - initY)
                if (deltaX < motionThreshold && deltaY < motionThreshold) {
                    return false
                }
                if (deltaX > deltaY) {
                    //горизонтальное направление
                    dragListener.onStartSwipe(holder)
                } else {
                    //вертикальное направление
                    dragListener.onStartDrag(holder)
                }
            }
            MotionEvent.ACTION_UP -> {
                v.performClick() // TODO long click will not work
                return false
            }
        }
        return true
    }
}
