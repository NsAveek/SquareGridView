package com.example.customgridview

import android.R.attr.maxHeight
import android.R.attr.maxWidth
import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible


/**
 * Algorithm
 * Total Grid Space = Total Canvas Width - (Trailing spaces*2) - ((total columns -1)*internal space)
 * Total Grid Space = 400 - 50*2 - (2-1)*50 = 250
 * Each Grid Space = 250/2 = 125
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class SquareGridCustomViewConstraintLayout @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attributeSet, defStyleAttr, defStyleRes) {

    private lateinit var btnLoadImage: Button
    private lateinit var imageGridCustomView: ImageView

    private lateinit var imageBitmapCanvas: Bitmap

    private var totalLeftPadding = 0
    private var totalRightPadding = 0
    private var totalTopPadding = 0
    private var totalBottomPadding = 0

    private var totalColumns: Int = 2 // default
    private var totalRows: Int = 2 // default
    private var space: Int = 10 //default
    private var squareWidth = 0
    private var squareHeight = 0

    private var canvasWidth = 0
    private var canvasHeight = 0

    private var horizontalGridWidth = 0
    private var verticalGridHeight = 0
    private var shape = Rect()

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
            )
        } finally {
            typedArray.recycle()
        }
        isClickable = true
        isVisible = true

        imageBitmapCanvas = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.nature
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        refreshValues(canvas)


        for (row in 0 until totalRows) {

            var localTopPadding = when (row) {
                0 -> totalTopPadding
                else -> shape.bottom + space
            }
            for (column in 0 until totalColumns) {
                when (column) {
                    0 -> {
                        if (row == 0) {
                            shape = drawSquareWithBitmap(
                                canvas,
                                totalLeftPadding,
                                localTopPadding,
                                totalLeftPadding + horizontalGridWidth,
                                localTopPadding + verticalGridHeight, imageBitmapCanvas
                            )
                        } else {
                            shape = drawSquare(
                                canvas,
                                totalLeftPadding,
                                localTopPadding,
                                totalLeftPadding + horizontalGridWidth,
                                localTopPadding + verticalGridHeight
                            )
                            Log.d("row $row column $column", shape.width().toString())
                        }
                    }
                    else -> {
                        shape = drawSquare(
                            canvas,
                            shape.right + (space),
                            localTopPadding,
                            shape.right + (space) + horizontalGridWidth,
                            localTopPadding + verticalGridHeight
                        )
                        Log.d("row $row column $column", shape.width().toString())
                    }
                }
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        squareWidth = widthMeasureSpec
        squareHeight = heightMeasureSpec

        totalLeftPadding = space
        totalRightPadding = totalLeftPadding
        totalTopPadding = totalLeftPadding
        totalBottomPadding = totalLeftPadding

        totalRows = totalColumns

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
        canvas.save()
        canvas.drawRect(shape, paint.apply { color = Color.WHITE })
        canvas.restore()
        return shape
    }

    private fun drawSquareWithBitmap(
        canvas: Canvas,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        bitmap: Bitmap
    ): Rect {
        val shape = Rect(left, top, right, bottom)
        canvas.save()
        canvas.drawRect(shape, paint)
        canvas.save()
        canvas.restore()
        Log.d("shape width", shape.width().toString())
        Log.d("shape height", shape.height().toString())
//        canvas.drawBitmap(resize(bitmap,canvas.width,canvas.height), shape, shape, null)
//        canvas.drawBitmap(Bitmap.createScaledBitmap(bitmap, shape.width(), shape.height(), false), shape, shape, null)
        canvas.drawBitmap(bitmap, null, shape, null)
        canvas.restore()
        bitmap.recycle()
        return shape
    }


    private fun resize(image: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        var image = image
        return if (maxHeight > 0 && maxWidth > 0) {
            val width = image.width
            val height = image.height
            val ratioBitmap = width.toFloat() / height.toFloat()
            val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()
            var finalWidth = maxWidth
            var finalHeight = maxHeight
            if (ratioMax > 1) {
                finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
            } else {
                finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
            }
            image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true)
            image
        } else {
            image
        }
    }
//    public fun transform(source : Bitmap) : Bitmap  {
//        val scale : Float
//        val newSize : Int
//        val scaleBitmap : Bitmap
//        if (isHeightScale) {
//            scale = (float) mSize / source.getHeight();
//            newSize = Math.round(source.getWidth() * scale);
//            scaleBitmap = Bitmap.createScaledBitmap(source, newSize, mSize, true);
//        } else {
//            scale = (float) mSize / source.getWidth();
//            newSize = Math.round(source.getHeight() * scale);
//            scaleBitmap = Bitmap.createScaledBitmap(source, mSize, newSize, true);
//        }
//        if (scaleBitmap != source) {
//            source.recycle();
//        }
//
//        return scaledBitmap
//    }
//    private fun resize(image: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
//        var image = image
//        return if (maxHeight > 0 && maxWidth > 0) {
//            val width = image.width
//            val height = image.height
//            val ratioBitmap = width.toFloat() / height.toFloat()
//            val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()
//            var finalWidth = maxWidth
//            var finalHeight = maxHeight
//            if (ratioMax > ratioBitmap) {
//                finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
//            } else {
//                finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
//            }
//            image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true)
//            image
//        } else {
//            image
//        }
//    }

    private fun getSquaredCroppedBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {

        var finalBitmap: Bitmap = if (bitmap.width != width || bitmap.height != height) {
            Bitmap.createScaledBitmap(bitmap, width, height, false)
        } else {
            bitmap
        }
        val output = Bitmap.createBitmap(
            finalBitmap.width,
            finalBitmap.height, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(output)
        val paint = Paint()
        val rect = Rect(
            0, 0, finalBitmap.width,
            finalBitmap.height
        )

        paint.isAntiAlias = true
        paint.isFilterBitmap = true
        paint.isDither = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = Color.parseColor("#BAB399")
        canvas.drawCircle(
            finalBitmap.width / 2 + 0.7f,
            finalBitmap.height / 2 + 0.7f,
            finalBitmap.width / 2 + 0.1f, paint
        )
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(finalBitmap, rect, rect, paint)

        return output

    }

    private fun refreshValues(canvas: Canvas) {
        horizontalGridWidth = getSquareDimension(canvas)
        verticalGridHeight = horizontalGridWidth
    }

    private fun getSquareDimension(canvas: Canvas): Int {
        // Algorithm
        // Total Grid Space = Total Canvas Width - (Trailing spaces*2) - ((total columns -1)*internal space)
        // Total Grid Space = 400 - 50*2 - (2-1)*50 = 250
        // Each Grid Space = 250/2 = 125
        return (canvasWidth - (space * 2) - ((totalColumns - 1) * space)) / totalColumns
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        canvasWidth = w
        canvasHeight = h
        super.onSizeChanged(w, h, oldw, oldh)
    }

    private fun dptoDisplayPixels(value: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }
}
