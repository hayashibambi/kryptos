package com.hayashibambi.kryptos

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.annotation.FloatRange
import androidx.core.math.MathUtils

class UpDownArrowDrawable(context: Context): Drawable() {

    companion object {
        private const val DEFAULT_ARROW_WIDTH = 48 // dip
        private const val DEFAULT_ARROW_HEIGHT = 28 // dip
        private const val DEFAULT_ARROW_THICKNESS = 8 // dip
        private const val DEFAULT_ARROW_COLOR = Color.BLACK
        private const val DEFAULT_FRACTION_BREAK_START = 0.45
        private const val DEFAULT_FRACTION_BREAK_END = 0.65
        private const val DEFAULT_PROGRESS = 0f
        private const val DEFAULT_IS_UP_TO_DOWN = true
        private const val DEFAULT_SKIP_BREAK_TIME = false
    }

    private val fraction = Fraction()
    private val paint = Paint()
    private val path = Path()
    private val drawArea = RectF()
    private val left = PointF()
    private val right = PointF()
    private val center = PointF()

    var progress = DEFAULT_PROGRESS
        set(value) {
            if (field != value) {
                field = value
                invalidateSelf()
            }
        }

    var arrowWidth = dpToPx(DEFAULT_ARROW_WIDTH, context)
        set(value) {
            if (field != value) {
                field = value
                invalidateDrawArea(bounds)
            }
        }

    var arrowHeight = dpToPx(DEFAULT_ARROW_HEIGHT, context)
        set(value) {
            if (field != value) {
                field = value
                invalidateDrawArea(bounds)
            }
        }

    var isUpToDown = DEFAULT_IS_UP_TO_DOWN
        set(value) {
            if (field != value) {
                field = value
                invalidateSelf()
            }
        }

    var arrowThickness
        get() = paint.strokeWidth
        set(value) {
            if (paint.strokeWidth != value) {
                paint.strokeWidth = value
                invalidateSelf()
            }
        }

    var arrowColor
        get() = paint.color
        set(value) {
            if (paint.color != value) {
                paint.color = value
                invalidateSelf()
            }
        }

    var breakStart
        get() = fraction.breakStart
        set(value) {
            if (fraction.breakStart != value) {
                fraction.breakStart = value
                invalidateSelf()
            }
        }

    var breakEnd
        get() = fraction.breakEnd
        set(value) {
            if (fraction.breakEnd != value) {
                fraction.breakEnd = value
                invalidateSelf()
            }
        }

    var skipBreakTime
        get() = fraction.skipBreakTime
        set(value) {
            if (fraction.skipBreakTime != value) {
                fraction.skipBreakTime = value
                invalidateSelf()
            }
        }

    init {
        breakStart = DEFAULT_FRACTION_BREAK_START
        breakEnd = DEFAULT_FRACTION_BREAK_END
        arrowColor = DEFAULT_ARROW_COLOR
        arrowThickness = dpToPx(DEFAULT_ARROW_THICKNESS, context).toFloat()
        skipBreakTime = DEFAULT_SKIP_BREAK_TIME
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        paint.isAntiAlias = true
    }

    override fun draw(canvas: Canvas) {
        canvas.drawColor(Color.GREEN)

        calculatePosition()
        path.reset()
        path.moveTo(left)
        path.lineTo(center)
        path.lineTo(right)
        canvas.drawPath(path, paint)
    }

    private fun calculatePosition() {
        var h = drawArea.height() / 2 * fraction.get(progress)
        if (!isUpToDown) h *= -1
        val centerY = drawArea.centerY()
        left.x = drawArea.left
        left.y = centerY + h
        center.x = drawArea.centerX()
        center.y = centerY - h
        right.x = drawArea.right
        right.y = left.y
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        invalidateDrawArea(bounds)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun getOpacity(): Int {
        return PixelFormat.OPAQUE
    }

    override fun setColorFilter(colorFilter: ColorFilter) {
        paint.colorFilter = colorFilter
    }

    private fun invalidateDrawArea(bounds: Rect) {
        drawArea.set(bounds)
        val verticalPadding = (drawArea.height() - arrowHeight) / 2
        val horizontalPadding = (drawArea.width() - arrowWidth) / 2
        drawArea.inset(horizontalPadding, verticalPadding)
    }

    private fun Path.moveTo(point: PointF) = this.moveTo(point.x, point.y)
    private fun Path.lineTo(point: PointF) = this.lineTo(point.x, point.y)

    private fun dpToPx(dp: Int, context: Context)
            = (context.resources.displayMetrics.density * dp).toInt()

    private class Fraction {

        companion object {
            private const val MIN_BREAK_POSITION = 0.1
            private const val MAX_BREAK_POSITION = 0.9
            private const val HALF_PI = Math.PI / 2
        }

        @FloatRange(
            from = MIN_BREAK_POSITION,
            to = MAX_BREAK_POSITION)
        var breakStart = DEFAULT_FRACTION_BREAK_START
            set(value) {
                field = Math.min(
                    MathUtils.clamp(value, MIN_BREAK_POSITION, MAX_BREAK_POSITION),
                    breakEnd)
            }

        @FloatRange(
            from = MIN_BREAK_POSITION
            , to = MAX_BREAK_POSITION)
        var breakEnd = DEFAULT_FRACTION_BREAK_END
            set(value) {
                field = Math.max(
                    breakStart,
                    MathUtils.clamp(value, MIN_BREAK_POSITION, MAX_BREAK_POSITION))
            }

        var skipBreakTime = DEFAULT_SKIP_BREAK_TIME

        /**
         * t: 0.0        -> breakStart / return: 1.0 -> 0.0
         * t: breakStart -> breakEnd   / return: 0.0 -> 0.0
         * t: breakEnd   -> 1.0        / return: 0.0 -> -1.0
         */
        @FloatRange(from = -1.0, to = 1.0)
        fun get(@FloatRange(from = 0.0, to = 1.0) t: Float): Float =
            if (skipBreakTime) Math.cos(Math.PI * t).toFloat()
            else when {
                t < breakStart -> Math.cos(HALF_PI * t / breakStart).toFloat()
                t < breakEnd -> 0f
                else -> Math.cos(HALF_PI * (1 + (t - breakEnd) / (1 - breakEnd))).toFloat()
            }


    }
}