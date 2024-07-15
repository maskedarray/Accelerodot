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

    private var maxDisplacement = 200

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null

    private lateinit var windowManager: WindowManager
    private lateinit var layoutParams: WindowManager.LayoutParams
    private var gridLayout: GridLayout? = null

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
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        overlayView = layoutInflater.inflate(R.layout.overlay_layout, null)
        val gridLayout = overlayView?.findViewById<GridLayout>(R.id.overlay_grid)
        addDotsToGridLayout(gridLayout)

        windowManager.addView(overlayView, params)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
    }

    private fun addDotsToGridLayout(gridLayout: GridLayout?) {
        gridLayout?.let {
            val rowCount = it.rowCount
            val columnCount = it.columnCount

            for (i in 0 until rowCount) {
                for (j in 0 until columnCount) {
                    val dot = View(this)
                    dot.layoutParams = ViewGroup.LayoutParams(20, 20) // Adjust size as needed
                    dot.setBackgroundResource(R.drawable.circle)

                    val params = GridLayout.LayoutParams()
                    params.width = 50 // Adjust size as needed
                    params.height = 50 // Adjust size as needed
                    params.rowSpec = GridLayout.spec(i)
                    params.columnSpec = GridLayout.spec(j)
                    params.setMargins(10, 10, 10, 10) // Adjust margins as needed

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
            val x = it.values[0]
            val y = it.values[1]

            val displacementX = (x * maxDisplacement).toInt()
            val displacementY = (y * maxDisplacement).toInt()

            updateGridPosition(displacementX, displacementY)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do nothing
    }

    private fun updateGridPosition(displacementX: Int, displacementY: Int) {
        gridLayout?.let {
            val layoutParams = it.layoutParams as WindowManager.LayoutParams
            layoutParams.x = displacementX.coerceIn(-maxDisplacement, maxDisplacement)
            layoutParams.y = displacementY.coerceIn(-maxDisplacement, maxDisplacement)
            it.layoutParams = layoutParams
        }
    }
}