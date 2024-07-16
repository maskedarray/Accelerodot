package com.example.accelerodot

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.util.Log
import android.widget.GridLayout
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import android.view.ViewGroup
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager


class OverlayService : Service(), SensorEventListener {

    private val TAG = "OverlayService"
    private var overlayView: View? = null
    private var dbg_counter = 0

    private var maxDisplacement = 2000

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null

    private var windowManager: WindowManager? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    private var gridLayout: GridLayout? = null

    private var filteredX = 0f
    private var filteredY = 0f
    private val filterFactor = 0.1f

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "OverlayService created")
        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val layoutInflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )

        overlayView = layoutInflater.inflate(R.layout.overlay_layout, null)
        val gridLayout = overlayView?.findViewById<GridLayout>(R.id.overlay_grid)

        if (gridLayout == null) {
            Log.e(TAG, "gridLayout is null")
        } else {
            Log.d(TAG, "gridLayout initialized")
        }

        addDotsToGridLayout(gridLayout)

        windowManager.addView(overlayView, params)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
    }

    private fun getScreenDimensions(): Pair<Int, Int> {
        val displayMetrics = resources.displayMetrics
        return Pair(displayMetrics.widthPixels, displayMetrics.heightPixels)
    }

    private fun addDotsToGridLayout(gridLayout: GridLayout?) {
        gridLayout?.let {
            val rowCount = it.rowCount
            val columnCount = it.columnCount

            for (i in 0 until rowCount) {
                for (j in 0 until columnCount) {
                    val dot = View(this)
                    dot.layoutParams = ViewGroup.LayoutParams(100, 100) // Adjust size as needed
                    dot.setBackgroundResource(R.drawable.circle)

                    val params = GridLayout.LayoutParams()
                    params.width = 20 // Adjust size as needed
                    params.height = 20 // Adjust size as needed
                    params.rowSpec = GridLayout.spec(i)
                    params.columnSpec = GridLayout.spec(j)
                    params.setMargins(20, 20, 20, 20) // Adjust margins as needed

                    dot.layoutParams = params

                    it.addView(dot)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        overlayView?.let {
            val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            windowManager.removeView(it)
        }
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val rawX = it.values[0]
            val rawY = it.values[1]

            // Apply low-pass filter
            filteredX = filteredX + filterFactor * (rawX - filteredX)
            filteredY = filteredY + filterFactor * (rawY - filteredY)

            val displacementX = (filteredX * maxDisplacement).toInt()
            val displacementY = (filteredY * maxDisplacement).toInt()

            updateGridPosition(displacementX, displacementY)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do nothing
    }

    private fun updateGridPosition(displacementX: Int, displacementY: Int) {
        overlayView?.findViewById<GridLayout>(R.id.overlay_grid)?.let {
            val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            val layoutParams = it.layoutParams as WindowManager.LayoutParams
            val screenDimensions = getScreenDimensions()
            val screenWidth = screenDimensions.first
            val screenHeight = screenDimensions.second

            // Calculate new position with wrap-around
            val newX = (displacementX + screenWidth) % screenWidth
            val newY = (displacementY + screenHeight) % screenHeight

            val params = layoutParams ?: return
            Log.d(TAG, "Screen width: "+ screenWidth + " screen height: " + screenHeight)
            layoutParams.x = newX
            layoutParams.y = newY
            windowManager?.updateViewLayout(overlayView, it.layoutParams)
        } ?: run {
            Log.e(TAG, "gridLayout is null in updateGridPosition")
        }
    }

}