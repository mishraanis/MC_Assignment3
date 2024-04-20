package com.example.assignment3_part1

import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.assignment3_part1.ui.theme.Assignment3_part1Theme
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private lateinit var sensorViewModel: SensorViewModel
    private val handler = Handler(Looper.getMainLooper())
    private var azimuth by mutableFloatStateOf(0f)
    private var pitch by mutableFloatStateOf(0f)
    private var roll by mutableFloatStateOf(0f)

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        sensorViewModel = ViewModelProvider(this)[SensorViewModel::class.java]

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        setContent {
            Assignment3_part1Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainDisplay(azimuth, pitch, roll, sensorViewModel)
                }
            }
        }
        handler.post(object : Runnable {
            override fun run() {
                GlobalScope.launch {
                    sensorViewModel.insert(SensorData(azimuth = azimuth, pitch = pitch, roll = roll, timestamp = System.currentTimeMillis()))
                }
                handler.postDelayed(this, 1000)
            }
        })
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do something here if sensor accuracy changes.
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val mySensor = event?.sensor

        if (mySensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
            val rotationMatrix = FloatArray(9)
            print("Event: ${event.values}")
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

            val orientationAngles = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientationAngles)

            roll = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
            pitch = Math.toDegrees(orientationAngles[1].toDouble()).toFloat()
            azimuth = Math.toDegrees(orientationAngles[2].toDouble()).toFloat()
        }
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }
}

@OptIn(DelicateCoroutinesApi::class)
fun clearAllData(sensorViewModel: SensorViewModel) {
    GlobalScope.launch {
        sensorViewModel.deleteAll()
    }
}

@Composable
fun MainDisplay(azimuth: Float, pitch: Float, roll: Float, sensorViewModel: SensorViewModel) {
    val context = LocalContext.current
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center,) {
        Column(horizontalAlignment = Alignment.CenterHorizontally){
            Column(horizontalAlignment = Alignment.CenterHorizontally){
                Text("x: $roll", modifier = Modifier.padding(16.dp))
                Text("y: $pitch", modifier = Modifier.padding(16.dp))
                Text("z: $azimuth", modifier = Modifier.padding(16.dp))
            }
            Box(contentAlignment = Alignment.Center){
                Row(modifier = Modifier.padding(16.dp)){
                    Button(onClick = {
                        context.startActivity(Intent(context, GraphPlotActivity::class.java))
                    }, modifier = Modifier.padding(10.dp)) {
                        Text(text = "View Graph", modifier = Modifier.padding(10.dp))
                    }
                    Button(onClick = {
                        clearAllData(sensorViewModel)
                    }, modifier = Modifier.padding(16.dp)) {
                        Text(text = "Clear Database", modifier = Modifier.padding(0.dp))
                    }
                }
            }
        }

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
    Assignment3_part1Theme {
        Greeting("Android")
    }
}