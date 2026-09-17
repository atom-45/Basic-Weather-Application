package com.example.basicweatherapp.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.basicweatherapp.R
import com.example.basicweatherapp.data.models.PredictionVerification
import com.example.basicweatherapp.data.models.SensorData
import com.example.basicweatherapp.data.models.ThermodynamicPrediction
import com.example.basicweatherapp.physics.StormReport
import androidx.compose.runtime.rxjava3.subscribeAsState
import com.example.basicweatherapp.presentation.theme.BasicWeatherAppTheme
import com.example.basicweatherapp.presentation.theme.Muli
import com.example.basicweatherapp.presentation.viewmodels.SensorDataViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun SensorDisplayScreen(
    sensorDataViewModel: SensorDataViewModel,
    onBackClick: () -> Unit,
    onRainPredictionClick: () -> Unit
) {
    val sensorDataList by sensorDataViewModel.allSensorData.subscribeAsState(initial = emptyList())
    val stormReport by sensorDataViewModel.stormAnalysis.subscribeAsState(initial = null)
    val unverifiedPredictions by sensorDataViewModel.getUnverifiedPredictions().subscribeAsState(initial = emptyList())
    
    val compositeDisposable = remember { CompositeDisposable() }
    DisposableEffect(Unit) {
        onDispose {
            compositeDisposable.clear()
        }
    }

    var selectedDataType by remember { mutableStateOf("Temperature") }
    var selectedPeriod by remember { mutableStateOf("10 minutes") }
    var chartData by remember { mutableStateOf<LineData?>(null) }
    
    val context = LocalContext.current

    // Auto-update graph when new data arrives or selections change
    LaunchedEffect(sensorDataList, selectedDataType, selectedPeriod) {
        if (chartData != null) {
            chartData = generateChartData(sensorDataList, selectedDataType, selectedPeriod, context)
        }
    }

    SensorDisplayContent(
        sensorDataList = sensorDataList,
        stormReport = stormReport,
        unverifiedPredictions = unverifiedPredictions,
        selectedDataType = selectedDataType,
        onDataTypeChange = { selectedDataType = it },
        selectedPeriod = selectedPeriod,
        onPeriodChange = { selectedPeriod = it },
        chartData = chartData,
        onPlotClick = {
            chartData = generateChartData(sensorDataList, selectedDataType, selectedPeriod, context)
        },
        onClearClick = { chartData = null },
        onExportClick = { start, end -> sensorDataViewModel.exportData(start, end) },
        onBackClick = onBackClick,
        onRainPredictionClick = onRainPredictionClick,
        onVerificationSubmit = { verification ->
            compositeDisposable.add(
                sensorDataViewModel.insertVerification(verification)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe()
            )
        },
        onAuditSubmit = { eventId, outcome ->
            compositeDisposable.add(
                sensorDataViewModel.verifyPrediction(eventId, outcome)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe()
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorDisplayContent(
    sensorDataList: List<SensorData>,
    stormReport: StormReport?,
    unverifiedPredictions: List<ThermodynamicPrediction>,
    selectedDataType: String,
    onDataTypeChange: (String) -> Unit,
    selectedPeriod: String,
    onPeriodChange: (String) -> Unit,
    chartData: LineData?,
    onPlotClick: () -> Unit,
    onClearClick: () -> Unit,
    onExportClick: (String, String) -> Unit,
    onBackClick: () -> Unit,
    onRainPredictionClick: () -> Unit,
    onVerificationSubmit: (PredictionVerification) -> Unit,
    onAuditSubmit: (String, String) -> Unit
) {
    val periods = listOf("10 minutes", "30 minutes", "60 minutes", "120 minutes", "150 minutes", "180 minutes", "240 minutes", "300 minutes")
    val dataTypes = listOf("Temperature", "Humidity", "Pressure")
    val lastData = sensorDataList.lastOrNull()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        colorResource(id = R.color.orange),
                        colorResource(id = R.color.yellow)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
                
                if (lastData != null) {
                    val displayDate = try {
                        lastData.date.replace("T", " ").substring(0, 16)
                    } catch (_: Exception) {
                        lastData.date
                    }
                     Text(
                        text = "Updated: $displayDate",
                        fontSize = 12.sp,
                        fontFamily = Muli,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bento Box Layout for Sensors
            Column(modifier = Modifier.fillMaxWidth()) {
                // Hero Temperature Card
                ModernSensorCard(
                    label = "Temperature",
                    value = lastData?.let { String.format(Locale.ENGLISH, "%.2f ℃", it.temperature) } ?: "-- ℃",
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    isHero = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    ModernSensorCard(
                        label = "Humidity",
                        value = lastData?.let { String.format(Locale.ENGLISH, "%.2f %%", it.humidity) } ?: "-- %",
                        modifier = Modifier.weight(1f).height(120.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    ModernSensorCard(
                        label = "Pressure",
                        value = lastData?.let { String.format(Locale.ENGLISH, "%.2f hPa", it.pressure) } ?: "-- hPa",
                        modifier = Modifier.weight(1f).height(120.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ModernSensorCard(
                        label = "Altitude",
                        value = lastData?.let { String.format(Locale.ENGLISH, "%.2f m", it.altitude) } ?: "-- m",
                        modifier = Modifier.weight(1.5f).height(120.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    // Rain Prediction Trigger Card
                    Card(
                        modifier = Modifier.weight(1f).height(120.dp).clickable { onRainPredictionClick() },
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.outline_humidity_percentage_24),
                                contentDescription = "Rain Prediction",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Predict", color = Color.White, fontSize = 14.sp, fontFamily = Muli)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Thermodynamic Analysis",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontFamily = Muli
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                ModernSensorCard(
                    label = "Storm Speed",
                    value = stormReport?.let { String.format(Locale.ENGLISH, "%.1f km/h", it.stormSpeedKmh) } ?: "-- km/h",
                    modifier = Modifier.weight(1f).height(120.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                ModernSensorCard(
                    label = "Arrival",
                    value = stormReport?.let {
                        if (it.arrivalTimeMinutes > 0) {
                            String.format(Locale.ENGLISH, "%s (%.0f min)", it.arrivalTimestamp, it.arrivalTimeMinutes)
                        } else "N/A"
                    } ?: "--:--",
                    modifier = Modifier.weight(1f).height(120.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Safety Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (stormReport?.isSafeToWalk == false)
                        Color(0xFFFF5252).copy(alpha = 0.8f)
                    else Color.White.copy(alpha = 0.15f)
                ),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground), // Placeholder icon
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stormReport?.statusMessage ?: "Awaiting atmospheric analysis...",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontFamily = Muli,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Black Box Audit Card - Appears during active window
            val activePrediction = unverifiedPredictions.firstOrNull { 
                val now = System.currentTimeMillis()
                now in it.windowStartMillis..it.windowEndMillis
            }
            
            activePrediction?.let { prediction ->
                AuditCard(prediction, onAuditSubmit)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Thermodynamic Verification Section
            if (stormReport != null && !stormReport.isSafeToWalk) {
                var feedbackSubmitted by remember { mutableStateOf(false) }
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (feedbackSubmitted) "Feedback received! Calibration data saved." 
                                   else "Verify this Thermodynamic Analysis:",
                            color = if (feedbackSubmitted) Color(0xFF4CAF50) else Color.White,
                            fontSize = 14.sp,
                            fontFamily = Muli,
                            fontWeight = FontWeight.Bold
                        )
                        
                        if (!feedbackSubmitted) {
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            val outcomes = listOf("Heavy Rain", "Light Rain", "Cloudy", "Lightning", "Windy", "Clear")
                            
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(outcomes) { outcome ->
                                    ThermoFeedbackChip(outcome, stormReport, lastData, onVerificationSubmit) {
                                        feedbackSubmitted = true
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "Trends",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontFamily = Muli
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Modern Trend Control Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    // Segmented Button for Data Type
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        dataTypes.forEachIndexed { index, type ->
                            SegmentedButton(
                                selected = selectedDataType == type,
                                onClick = { onDataTypeChange(type) },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = dataTypes.size),
                                label = { Text(type, fontSize = 11.sp, fontFamily = Muli) },
                                colors = SegmentedButtonDefaults.colors(
                                    activeContainerColor = Color.White.copy(alpha = 0.3f),
                                    activeContentColor = Color.White,
                                    inactiveContainerColor = Color.Transparent,
                                    inactiveContentColor = Color.White.copy(alpha = 0.7f),
                                    activeBorderColor = Color.White.copy(alpha = 0.5f),
                                    inactiveBorderColor = Color.White.copy(alpha = 0.2f)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    var expanded by remember { mutableStateOf(false) }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded },
                            modifier = Modifier.weight(0.8f)
                        ) {
                            OutlinedTextField(
                                value = selectedPeriod,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Period", color = Color.White.copy(alpha = 0.7f)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color.White.copy(alpha = 0.5f),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                periods.forEach { period ->
                                    DropdownMenuItem(
                                        text = { Text(period) },
                                        onClick = {
                                            onPeriodChange(period)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = onPlotClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.8f), contentColor = Color.Black),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Plot")
                        }
                        TextButton(
                            onClick = onClearClick,
                            colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                        ) {
                            Text("Clear")
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    AndroidView(
                        factory = { ctx ->
                            LineChart(ctx).apply {
                                setDrawGridBackground(false)
                                description.isEnabled = false
                                xAxis.setDrawGridLines(false)
                                xAxis.textColor = android.graphics.Color.WHITE
                                axisLeft.textColor = android.graphics.Color.WHITE
                                axisRight.isEnabled = false
                                legend.textColor = android.graphics.Color.WHITE
                            }
                        },
                        update = { chart ->
                            chart.data = chartData
                            chart.invalidate()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Export Dataset Section
                    var showDatePicker by remember { mutableStateOf(false) }
                    val dateRangePickerState = rememberDateRangePickerState()

                    Button(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.2f),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                    ) {
                        Icon(painterResource(id = R.drawable.ic_launcher_foreground), // Placeholder icon
                            contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export Historical Dataset", fontFamily = Muli)
                    }

                    if (showDatePicker) {
                        Dialog(
                            onDismissRequest = { showDatePicker = false },
                            properties = DialogProperties(usePlatformDefaultWidth = false)
                        ) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(vertical = 40.dp, horizontal = 16.dp),
                                shape = RoundedCornerShape(28.dp),
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 6.dp
                            ) {
                                Column {
                                    DateRangePicker(
                                        state = dateRangePickerState,
                                        modifier = Modifier.weight(1f),
                                        title = { 
                                            Text(
                                                text = "Select Date Range", 
                                                modifier = Modifier.padding(start = 24.dp, top = 24.dp),
                                                style = MaterialTheme.typography.labelMedium
                                            ) 
                                        },
                                        headline = {
                                            DateRangePickerDefaults.DateRangePickerHeadline(
                                                selectedStartDateMillis = dateRangePickerState.selectedStartDateMillis,
                                                selectedEndDateMillis = dateRangePickerState.selectedEndDateMillis,
                                                displayMode = dateRangePickerState.displayMode,
                                                dateFormatter = DatePickerDefaults.dateFormatter(),
                                                modifier = Modifier.padding(start = 24.dp, bottom = 12.dp)
                                            )
                                        }
                                    )
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(end = 16.dp, bottom = 16.dp),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        TextButton(onClick = { showDatePicker = false }) {
                                            Text("Cancel")
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = {
                                                val startMs = dateRangePickerState.selectedStartDateMillis
                                                val endMs = dateRangePickerState.selectedEndDateMillis
                                                if (startMs != null && endMs != null) {
                                                    val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
                                                    val start = Instant.ofEpochMilli(startMs).atZone(ZoneId.systemDefault()).toLocalDateTime().format(formatter)
                                                    val end = Instant.ofEpochMilli(endMs).atZone(ZoneId.systemDefault()).toLocalDateTime().withHour(23).withMinute(59).format(formatter)
                                                    onExportClick(start, end)
                                                }
                                                showDatePicker = false
                                            }
                                        ) {
                                            Text("Export")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun AuditCard(
    prediction: ThermodynamicPrediction,
    onVerify: (String, String) -> Unit
) {
    var feedbackSubmitted by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.25f)),
        border = BorderStroke(2.dp, Color.White.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_add_circle_outline_24), // Needs appropriate icon
                    contentDescription = null,
                    tint = Color.Yellow,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Live Model Audit",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontFamily = Muli
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = if (feedbackSubmitted) "Audit complete. Energy model calibrated." 
                       else "At ${prediction.originalTimestamp.substring(11, 16)}, physics predicted arrival at ${prediction.predictedArrival}. What is the actual status?",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 14.sp,
                fontFamily = Muli
            )

            if (!feedbackSubmitted) {
                Spacer(modifier = Modifier.height(16.dp))
                val outcomes = listOf("Heavy Rain", "Light Rain", "Cloudy", "Windy", "Lightning", "Clear")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(outcomes) { outcome ->
                        AssistChip(
                            onClick = {
                                onVerify(prediction.stormEventId, outcome)
                                feedbackSubmitted = true
                            },
                            label = { Text(outcome, fontSize = 11.sp, color = Color.White) },
                            colors = AssistChipDefaults.assistChipColors(containerColor = Color.White.copy(alpha = 0.1f))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ThermoFeedbackChip(
    outcome: String,
    report: StormReport,
    lastData: SensorData?,
    onVerify: (PredictionVerification) -> Unit,
    onSuccess: () -> Unit
) {
    AssistChip(
        onClick = {
            if (lastData != null) {
                val verification = PredictionVerification(
                    lastData.date,
                    lastData.temperature,
                    lastData.humidity,
                    lastData.pressure,
                    report.statusMessage,
                    outcome,
                    report.thermodynamicResidual,
                    "THERMODYNAMIC_DE"
                )
                onVerify(verification)
                onSuccess()
            }
        },
        label = { Text(outcome, fontSize = 10.sp, color = Color.White) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = Color.White.copy(alpha = 0.1f),
            labelColor = Color.White
        ),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
    )
}

@Composable
fun ModernSensorCard(label: String, value: String, modifier: Modifier, isHero: Boolean = false) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f),
                fontFamily = Muli,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                color = Color.White,
                fontSize = if (isHero) 48.sp else 24.sp,
                fontFamily = Muli,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

private fun generateChartData(dataList: List<SensorData>, type: String, period: String, context: android.content.Context): LineData? {
    val samplingFrequency = 10
    val parts = period.split(" ")
    if (parts.isEmpty()) return null
    val minutes = parts[0].toIntOrNull() ?: 0
    val points = minutes / samplingFrequency
    
    val entries = mutableListOf<Entry>()
    
    dataList.forEach { data ->
        val value = when(type) {
            "Temperature" -> data.temperature
            "Humidity" -> data.humidity
            "Pressure" -> data.pressure
            else -> 0f
        }
        entries.add(Entry(data.id.toFloat(), value))
    }
    
    val filteredEntries = if (entries.size > points) {
        entries.takeLast(points)
    } else {
        if (entries.isEmpty()) {
            Toast.makeText(context, "No sensor data available", Toast.LENGTH_SHORT).show()
        }
        entries
    }
    
    if (filteredEntries.isEmpty()) return null
    
    val dataSet = LineDataSet(filteredEntries, type).apply {
        mode = LineDataSet.Mode.CUBIC_BEZIER
        setDrawValues(false)
        lineWidth = 3f
        setDrawCircles(false)
        axisDependency = YAxis.AxisDependency.LEFT
        color = android.graphics.Color.WHITE
        setDrawFilled(true)
        fillDrawable = android.graphics.drawable.GradientDrawable(
            android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(android.graphics.Color.argb(100, 255, 255, 255), android.graphics.Color.TRANSPARENT)
        )
    }
    
    return LineData(dataSet)
}

@Preview(showBackground = true)
@Composable
fun SensorDisplayScreenPreview() {
    BasicWeatherAppTheme {
        SensorDisplayContent(
            sensorDataList = emptyList(),
            stormReport = null,
            unverifiedPredictions = emptyList(),
            selectedDataType = "Temperature",
            onDataTypeChange = {},
            selectedPeriod = "10 minutes",
            onPeriodChange = {},
            chartData = null,
            onPlotClick = {},
            onClearClick = {},
            onExportClick = { _, _ -> },
            onBackClick = {},
            onRainPredictionClick = {},
            onVerificationSubmit = {},
            onAuditSubmit = { _, _ -> }
        )
    }
}
