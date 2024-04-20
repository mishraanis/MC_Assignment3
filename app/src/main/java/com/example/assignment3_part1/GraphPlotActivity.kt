package com.example.assignment3_part1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.assignment3_part1.ui.theme.Assignment3_part1Theme
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.MediaStore
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.io.OutputStreamWriter
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineSpec
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.shader.color
import com.patrykandpatrick.vico.compose.common.shader.verticalGradient
import com.patrykandpatrick.vico.core.cartesian.Scroll
import com.patrykandpatrick.vico.core.cartesian.data.AxisValueOverrider
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.core.cartesian.data.LineCartesianLayerModel
import com.patrykandpatrick.vico.core.common.shader.DynamicShader
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope


class GraphPlotActivity : ComponentActivity() {
    private lateinit var sensorViewModel: SensorViewModel
    private var orientationData = mutableListOf<SensorData>()
    private var azimuthEntries = emptyList<Float>()
    private var pitchEntries = emptyList<Float>()
    private var rollEntries = emptyList<Float>()
    private var timeStampEntries = emptyList<Long>()
    private lateinit var model: CartesianChartModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorViewModel = ViewModelProvider(this)[SensorViewModel::class.java]



        println("datadb: ${orientationData.toList()}")
        setContent {
            println("${orientationData.toList()}, dbdata")
            Assignment3_part1Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(modifier = Modifier.fillMaxSize()){
                        PlotGraph(azimuthEntries, timeStampEntries, "azimuth")
                        PlotGraph(pitchEntries, timeStampEntries, "pitch")
                        PlotGraph(rollEntries, timeStampEntries, "roll")
                        SaveDataAsCsv()
                    }
                }
            }
        }
        fetchData()




//        val layout = LinearLayout(this).apply {
//            orientation = LinearLayout.VERTICAL
//        }
//
//        val chartHeight = 500 // Set the height of the charts here
//        val chartLayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, chartHeight)
//
//        val azimuthChart = LineChart(this).apply { layoutParams = chartLayoutParams }
//        val pitchChart = LineChart(this).apply { layoutParams = chartLayoutParams }
//        val rollChart = LineChart(this).apply { layoutParams = chartLayoutParams }
//
//        layout.addView(azimuthChart)
//        layout.addView(pitchChart)
//        layout.addView(rollChart)
//
//        setContentView(layout)
//
//        lifecycleScope.launch {
//            val data = sensorViewModel.getAll().reversed()
//
//            val azimuthEntries = data.mapIndexed { index, sensorData -> Entry(index.toFloat(), sensorData.azimuth) }
//            val pitchEntries = data.mapIndexed { index, sensorData -> Entry(index.toFloat(), sensorData.pitch) }
//            val rollEntries = data.mapIndexed { index, sensorData -> Entry(index.toFloat(), sensorData.roll) }
//
////            println()
//            azimuthChart.data = LineData(LineDataSet(azimuthEntries, "Azimuth"))
//            pitchChart.data = LineData(LineDataSet(pitchEntries, "Pitch"))
//            rollChart.data = LineData(LineDataSet(rollEntries, "Roll"))
//
//            azimuthChart.apply {
//                xAxis.apply {
//                    axisMinimum = 0f // Your minimum value
//                    axisMaximum = 100f // Your maximum value
//                }
//                setHorizontalScrollBarEnabled(true)
//                moveViewToX(100f) // Move view to the maximum x value
//                invalidate() // Refresh the chart
//            }
//            pitchChart.apply {
//                xAxis.apply {
//                    axisMinimum = 0f // Your minimum value
//                    axisMaximum = 100f // Your maximum value
//                }
//                setHorizontalScrollBarEnabled(true)
//                moveViewToX(100f) // Move view to the maximum x value
//                invalidate() // Refresh the chart
//            }
//            rollChart.apply {
//                xAxis.apply {
//                    axisMinimum = 0f // Your minimum value
//                    axisMaximum = 100f // Your maximum value
//                }
//                setHorizontalScrollBarEnabled(true)
//                moveViewToX(100f) // Move view to the maximum x value
//                invalidate() // Refresh the chart
//            }
//        }
//        add a button to clear database
//        DeleteButton()

    }

    @Composable
    private fun SaveDataAsCsv() {
        val filename = "orientationData.csv"
        val context = LocalContext.current
        val successMessage = remember { mutableStateOf("") }
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
        }

        val contentUri = MediaStore.Files.getContentUri("external")
        val uri = context.contentResolver.insert(contentUri, contentValues)

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center,){
            Row(modifier = Modifier.fillMaxHeight(), horizontalArrangement = Arrangement.Center){
                Button(onClick = {
                    context.contentResolver.openOutputStream(uri!!)?.use { outputStream ->
                        val writer = OutputStreamWriter(outputStream)
                        writer.use { out ->
                            // Write the headers
                            out.write("Azimuth,Pitch,Roll,Timestamp\n")

                            // Write the data
                            orientationData.forEach { sensorData ->
                                out.write("${sensorData.azimuth},${sensorData.pitch},${sensorData.roll},${sensorData.timestamp}\n")
                            }
                        }
                        successMessage.value = "Orientation Data Saved Successfully"
//                        Toast.makeText(context, "Orientation Data Saved Successfully", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text(text = "Save Data as CSV", modifier = Modifier.padding(10.dp))
                }
                Button(onClick = {
                    context.startActivity(Intent(context, MainActivity::class.java))
                }) {
                    Text(text = "Go Back", modifier = Modifier.padding(10.dp))
                }
                if (successMessage.value.isNotEmpty()) {
                        Toast.makeText(context, "Orientation Data Saved", Toast.LENGTH_SHORT).show()
//                    Toa(text = successMessage.value, color = Color.Green)
                }
            }
        }
    }
    @OptIn(DelicateCoroutinesApi::class)
    private fun fetchData() {
        GlobalScope.launch(Dispatchers.IO) {
            val data = sensorViewModel.getAll().reversed()
            orientationData.addAll(data)
            azimuthEntries = data.map{ sensorData -> sensorData.azimuth}
            pitchEntries = data.map{ sensorData -> sensorData.pitch}
            rollEntries = data.map{ sensorData -> sensorData.roll}
            timeStampEntries = data.map{ sensorData -> sensorData.timestamp}
        }
    }
}


@Composable
fun PlotGraph(plottingEntries: List<Float>, timeStampEntries: List<Long>, label: String) {
    val context = LocalContext.current
    if(plottingEntries.isNotEmpty())
    {
        val model = CartesianChartModel(
            LineCartesianLayerModel.build {
                series(plottingEntries)
            },
        )
        val model2 = CartesianChartModel(
            LineCartesianLayerModel.build {
                series(timeStampEntries)
            },
        )
        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            CartesianChartHost(
                chart =
                rememberCartesianChart(
                    rememberLineCartesianLayer(),
                    rememberLineCartesianLayer(
                        listOf(
                            rememberLineSpec(
                                shader = DynamicShader.color(Color.Blue),
                                backgroundShader =
                                DynamicShader.verticalGradient(
                                    arrayOf(
                                        Color.Blue.copy(alpha = 0.4f),
                                        Color.Blue.copy(alpha = 0f)
                                    ),
                                ),
                            ),
                        ),
                    ),
                    startAxis = rememberStartAxis(
                    ),
                    bottomAxis = rememberBottomAxis(

                    ),
                ),
                model = model,
                scrollState = rememberVicoScrollState(true, Scroll.Absolute.Start),
                modifier = Modifier.height(150.dp)
            )
            Text(text = "$label entries")
        }
    }else{
        Text(text = "No $label entries")
    }

//    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//        Text("x: $rollEntries, y: $pitchEntries, z: $azimuthEntries")
//    }

}