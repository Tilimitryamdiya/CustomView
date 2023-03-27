package ru.netology.customview.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.withStyledAttributes
import ru.netology.customview.R
import ru.netology.customview.util.AndroidUtils
import kotlin.math.min
import kotlin.random.Random

class StatsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private var radius = 0F
    private var center = PointF(0F, 0F)
    private var oval = RectF(0F, 0F, 0F, 0F)

    private var lineWidth = AndroidUtils.dp(context, 5F).toFloat()
    private var fontSize = AndroidUtils.dp(context, 40F).toFloat()
    private var colors = emptyList<Int>()
    private var notFilledColor = 0
    private var animationType = 0

    private var progress = 0F
    private var valueAnimator: ValueAnimator? = null

    init {
        context.withStyledAttributes(attrs, R.styleable.StatsView) {
            lineWidth = getDimension(R.styleable.StatsView_lineWidth, lineWidth)
            fontSize = getDimension(R.styleable.StatsView_fontSize, fontSize)
            val resId = getResourceId(R.styleable.StatsView_colors, 0)
            colors = resources.getIntArray(resId).toList()
            notFilledColor = getInt(R.styleable.StatsView_notFilledColor, Color.GRAY)
            animationType = getInt(R.styleable.StatsView_animationType, 0)
        }
    }

    var totalData: Float = 0F
        set(value) {
            field = value
            invalidate()
        }

    var currentDataList: List<Float> = emptyList()
        set(value) {
            field = value
            update()
        }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = lineWidth
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = fontSize
    }

    private val notFilledPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = lineWidth
        color = notFilledColor
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        radius = min(w, h) / 2F - lineWidth
        center = PointF(w / 2F, h / 2F)
        oval = RectF(
            center.x - radius, center.y - radius,
            center.x + radius, center.y + radius,
        )
    }

    override fun onDraw(canvas: Canvas) {
        if (currentDataList.isEmpty()) {
            return
        }

        canvas.drawCircle(center.x, center.y, radius, notFilledPaint)

        val percentData = currentDataList.sum() / totalData

        canvas.drawText(
            "%.2f%%".format(percentData * 100),
            center.x,
            center.y + textPaint.textSize / 4,
            textPaint,
        )

        var startFrom = -90F

        for ((index, datum) in currentDataList.withIndex()) {
            val percentDatumInData = datum / totalData
            val angle = 360F * percentDatumInData
            paint.color = colors.getOrNull(index) ?: randomColor()

            when (animationType) {

                AnimationType.ROTATION.type -> {
                    canvas.drawArc(
                        oval,
                        startFrom + progress * 360,
                        angle * progress,
                        false,
                        paint
                    )
                    if (progress == 1F) {
                        paint.color = colors[0]
                        canvas.drawPoint(center.x, center.y - radius, paint)
                    }
                }

                AnimationType.SEQUENTIAL.type -> {
                    if (progress < 0) return
                    canvas.drawArc(
                        oval,
                        startFrom,
                        angle * if (progress < percentDatumInData) progress / percentDatumInData else 1F,
                        false,
                        paint
                    )
                    progress -= percentDatumInData
                    paint.color = colors[0]
                    canvas.drawPoint(center.x, center.y - radius, paint)
                }

                AnimationType.BIDIRECTIONAL.type -> {
                    canvas.drawArc(
                        oval,
                        startFrom + (angle - angle * progress) / 2,
                        angle * progress,
                        false,
                        paint
                    )
                    if (progress == 1F) {
                        paint.color = colors[0]
                        canvas.drawPoint(center.x, center.y - radius, paint)
                    }
                }
            }

            startFrom += angle
        }
    }

    private fun update() {
        valueAnimator?.let {
            it.removeAllListeners()
            it.cancel()
        }
        valueAnimator = ValueAnimator.ofFloat(0F, 1F).apply {
            addUpdateListener { anim ->
                progress = anim.animatedValue as Float
                invalidate()
            }
            duration = 2000
            interpolator = LinearInterpolator()
        }.also {
            it.start()
        }
    }

    private enum class AnimationType(val type: Int) {
        ROTATION(0),
        SEQUENTIAL(1),
        BIDIRECTIONAL(2)
    }

    private fun randomColor() = Random.nextInt(0xFF000000.toInt(), 0xFFFFFFFF.toInt())
}