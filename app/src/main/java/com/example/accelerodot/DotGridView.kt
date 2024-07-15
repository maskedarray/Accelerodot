package com.example.accelerodot

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class DotGridView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val paint = Paint().apply {
        color = 0xFFFFFFFF.toInt()
        style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width
        val height = height
        val spacing = 50

        for (x in 0 until width step spacing) {
            for (y in 0 until height step spacing) {
                canvas.drawCircle(x.toFloat(), y.toFloat(), 5f, paint)
            }
        }
    }
}