package com.example.customgridview

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import androidx.annotation.RequiresApi

/**
 * Algorithm
 * Total Grid Space = Total Canvas Width - (Trailing spaces*2) - ((total columns -1)*internal space)
 * Total Grid Space = 400 - 50*2 - (2-1)*50 = 250
 * Each Grid Space = 250/2 = 125
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class SquareGridCustomView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attributeSet, defStyleAttr, defStyleRes) {

    private var totalLeftPadding = 0
    private var totalRightPadding = 0
    private var totalTopPadding = 0
    private var totalBottomPadding = 0

    private var totalColumns: Int = 2 // default
    private var totalRows: Int = 2 // default
    private var space: Int = 10 //default

    private var squareShape1: Rect? = null
    private var squareShape2: Rect? = null
    private var squareShape3: Rect? = null
    private var squareWidth = 0
    private var squareHeight = 0


    private var horizontalGridWidth = 0
    private var verticalGridHeight = 0
    private var extraPadding = dptoDisplayPixels(10)

    private var paint = Paint()?.also {
        it.isAntiAlias = true // Smoothing Surface
    }

    init {
        val typedArray =
            context.obtainStyledAttributes(attributeSet, R.styleable.SquareGridCustomView)
        try {
            totalColumns = typedArray.getInt(R.styleable.SquareGridCustomView_grids, 2)
            space = typedArray.getInt(R.styleable.SquareGridCustomView_col_space, 10)
            paint.color = typedArray.getColor(
                R.styleable.SquareGridCustomViewColor_color,
                Color.RED
            ) // Default color is RED
        } finally {
            typedArray.recycle()
        }
        isClickable = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)


        refreshValues(canvas)

        var shape = Rect()
        for (row in 0 until totalRows) {
            var localTopPadding = totalTopPadding
            if(row != 0) {
                localTopPadding = shape.bottom + space
            }
            for (column in 0 until totalColumns) {
                when (column) {
                    0 -> {
                        shape = drawSquare(
                            canvas,
                            totalLeftPadding,
                            localTopPadding,
                            totalLeftPadding + horizontalGridWidth,
                            localTopPadding + verticalGridHeight
                        )
                        Log.d("column 1", shape.width().toString())
                    }
                    else -> {
                        shape = drawSquare(
                            canvas,
                            shape.right + (space),
                            localTopPadding,
                            shape.right + (space) + horizontalGridWidth,
                            localTopPadding + verticalGridHeight
                        )
                        Log.d("column 2", shape.width().toString())
                    }
                }
            }
        }

        /*for (row in 0 until totalRows) {
            for (column in 0 until totalColumns) {
                if (column == 0) {
                    shape = drawSquare(
                        canvas,
                        totalLeftPadding,
                        totalTopPadding,
                        totalLeftPadding + horizontalGridWidth - (innerSpace),
                        verticalGridHeight - (space / 2)
                    )
                    Log.d("column 1", shape.width().toString())
                } else if (column == totalColumns - 1) {
                    shape = drawSquare(
                        canvas,
                        shape.right + (space),
                        totalTopPadding,
                        shape.right + (space) + horizontalGridWidth,
                        verticalGridHeight - (space / 2)
                    )
                    Log.d("column 3", shape.width().toString())
                } else {
                    shape = drawSquare(
                        canvas,
                        shape.right + (space),
                        totalTopPadding,
                        shape.right + (space) + horizontalGridWidth - (innerSpace),
                        verticalGridHeight - (space / 2)
                    )
                    Log.d("column 2", shape.width().toString())
                }
            }
        }*/
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        squareWidth = widthMeasureSpec
        squareHeight = heightMeasureSpec
//        squareWidth = widthMeasureSpec
//        squareHeight = heightMeasureSpec

        totalLeftPadding = space
        totalRightPadding = totalLeftPadding
        totalTopPadding = totalLeftPadding
        totalBottomPadding = totalLeftPadding

        totalRows = totalColumns

//        this.setMeasuredDimension(squareWidth, squareHeight)
        this.setMeasuredDimension(squareWidth, squareHeight)
    }

    private fun drawSquare(
        canvas: Canvas,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ): Rect {
        val shape = Rect(left, top, right, bottom)
        canvas.drawRect(shape, paint)
        return shape
    }

//    private fun drawSquare(
//        canvas: Canvas
//    ){
//        squareShape1 = Rect(totalLeftPadding,totalTopPadding,(horizontalGridWidth - totalRightPadding/2).toInt(), (verticalGridHeight-totalBottomPadding).toInt())
//        squareShape1?.let { canvas.drawRect(it,paint) }
//        invalidate()
//        squareShape2 = Rect(squareShape1!!.right+totalLeftPadding,totalTopPadding,(squareShape1!!.right+totalLeftPadding/2+horizontalGridWidth-totalRightPadding/2).toInt(), (verticalGridHeight-totalBottomPadding).toInt())
//        squareShape2?.let { canvas.drawRect(it,paint) }
//        invalidate()
//        squareShape3 = Rect(squareShape2!!.right+totalLeftPadding,totalTopPadding,(squareShape2!!.right+ totalLeftPadding/2+horizontalGridWidth-totalRightPadding).toInt(), (verticalGridHeight-totalBottomPadding).toInt())
//        squareShape3?.let { canvas.drawRect(it,paint) }
//
//        invalidate()
//    }

    private fun refreshValues(canvas: Canvas) {
        horizontalGridWidth = getSquareDimension(canvas)
        verticalGridHeight = horizontalGridWidth
    }

    private fun getSquareDimension(canvas: Canvas): Int {
        // Algorithm
        // Total Grid Space = Total Canvas Width - (Trailing spaces*2) - ((total columns -1)*internal space)
        // Total Grid Space = 400 - 50*2 - (2-1)*50 = 250
        // Each Grid Space = 250/2 = 125
        return (canvas.width - (space * 2) - ((totalColumns - 1) * space)) / totalColumns
    }

    private fun dptoDisplayPixels(value: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }
}
