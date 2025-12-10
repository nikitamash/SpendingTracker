package com.nikita.app.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.nikita.app.R

data class BarData(
    val label: String,
    val value: Float,
    val iconDrawable: Int? = null,
    val isSelected: Boolean = false
)

class BarChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var data: List<BarData> = emptyList()
    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val selectedBarColor = ContextCompat.getColor(context, R.color.yellow_primary)
    private val unselectedBarColor = ContextCompat.getColor(context, R.color.yellow_dark) // Or a dimmer yellow
    private val textColor = ContextCompat.getColor(context, R.color.light_gray)
    
    private val barWidth = 28f * resources.displayMetrics.density
    private val barSpacing = 16f * resources.displayMetrics.density
    private val cornerRadius = 8f * resources.displayMetrics.density

    init {
        barPaint.style = Paint.Style.FILL
        textPaint.color = textColor
        // Set text size to be proportional to bar width (80% of bar width)
        textPaint.textSize = barWidth * 0.8f
        textPaint.textAlign = Paint.Align.CENTER
    }

    fun setData(newData: List<BarData>) {
        data = newData
        requestLayout() // Trigger measurement for new width
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        if (data.isEmpty()) return

        val availableHeight = height - paddingBottom - paddingTop - textPaint.textSize - 20f
        val maxValue = data.maxOfOrNull { it.value } ?: 1f
        val safeMaxValue = if (maxValue == 0f) 1f else maxValue

        // Calculate total content width
        val totalChartWidth = (data.size * barWidth) + ((data.size - 1) * barSpacing) + paddingLeft + paddingRight
        
        // Start drawing from padding left by default
        var startX = paddingLeft.toFloat()
        
        // If content fits within the view, center it
        if (totalChartWidth < width) {
            startX = (width - totalChartWidth) / 2f + paddingLeft
        }
        
        data.forEach { item ->
            // Calculate bar height relative to max value
            val barHeight = (item.value / safeMaxValue) * availableHeight
            
            val left = startX
            val right = left + barWidth
            val bottom = height - paddingBottom - textPaint.textSize - 10f
            val top = bottom - barHeight.coerceAtLeast(10f) // Minimum height for visibility

            // Draw Bar
            barPaint.color = if (item.isSelected) selectedBarColor else unselectedBarColor
            // Draw rounded rect
            canvas.drawRoundRect(left, top, right, bottom, cornerRadius, cornerRadius, barPaint)

            // Draw Label (text or drawable icon)
            if (item.iconDrawable != null) {
                // Draw drawable icon
                val drawable = ContextCompat.getDrawable(context, item.iconDrawable)
                drawable?.let {
                    val iconSize = (barWidth * 0.8f).toInt()
                    val iconLeft = (left + barWidth / 2f - iconSize / 2f).toInt()
                    val iconTop = (height - paddingBottom - iconSize).toInt()
                    it.setBounds(iconLeft, iconTop, iconLeft + iconSize, iconTop + iconSize)
                    it.draw(canvas)
                }
            } else {
                // Draw text label (emoji)
                canvas.drawText(
                    item.label,
                    left + barWidth / 2f,
                    height - paddingBottom.toFloat(),
                    textPaint
                )
            }

            startX += barWidth + barSpacing
        }
    }
    
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredHeight = (200 * resources.displayMetrics.density).toInt()
        
        // Calculate required width based on data
        val contentWidth = if (data.isNotEmpty()) {
            (data.size * barWidth) + ((data.size - 1) * barSpacing) + paddingLeft + paddingRight
        } else {
            0f
        }
        
        val desiredWidth = contentWidth.toInt().coerceAtLeast(suggestedMinimumWidth)

        val width = resolveSize(desiredWidth, widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        
        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> heightSize.coerceAtMost(desiredHeight)
            else -> desiredHeight
        }
        
        setMeasuredDimension(width, height)
    }
}
