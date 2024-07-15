package com.example.accelerodot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.accelerodot.ui.theme.AccelerodotTheme

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.provider.Settings // Make sure this import is present
import android.util.Log


class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Log.d(TAG, "Requesting overlay permission")
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                startActivityForResult(intent, 0)
            } else {
                Log.d(TAG, "Overlay permission granted, starting overlay service")
                startOverlayService()
            }
        } else {
            Log.d(TAG, "Starting overlay service for SDK < M")
            startOverlayService()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    Log.d(TAG, "Overlay permission granted, starting overlay service after result")
                    startOverlayService()
                } else {
                    Log.d(TAG, "Overlay permission not granted")
                }
            }
        }
    }

    private fun startOverlayService() {
        Log.d(TAG, "Starting overlay service")
        val intent = Intent(this, OverlayService::class.java)
        startService(intent)
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AccelerodotTheme {
        Greeting("Android")
    }
}