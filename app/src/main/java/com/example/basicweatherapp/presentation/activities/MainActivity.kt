package com.example.basicweatherapp.presentation.activities

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.*
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.rxjava3.subscribeAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.basicweatherapp.R
import com.example.basicweatherapp.data.models.Place
import com.example.basicweatherapp.data.models.SensorData
import com.example.basicweatherapp.data.models.PredictionVerification
import com.example.basicweatherapp.data.models.User
import com.example.basicweatherapp.data.remote.responses.ForecastResponse
import com.example.basicweatherapp.di.application.WeatherApplication
import com.example.basicweatherapp.factories.ViewModelFactory
import com.example.basicweatherapp.presentation.screens.*
import com.example.basicweatherapp.presentation.theme.BasicWeatherAppTheme
import com.example.basicweatherapp.presentation.theme.Muli
import com.example.basicweatherapp.presentation.viewmodels.PlaceViewModel
import com.example.basicweatherapp.presentation.viewmodels.SensorDataViewModel
import com.example.basicweatherapp.presentation.viewmodels.UserViewModel
import com.example.basicweatherapp.presentation.viewmodels.WeatherViewModel
import com.example.basicweatherapp.services.WeatherBLEService
import com.example.basicweatherapp.utilities.Constants
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

class MainActivity : ComponentActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private var bluetoothManager: BluetoothManager? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var scanning = false
    private val handler = Handler(Looper.getMainLooper())
    private var deviceAddress: String? = null
    private var weatherBLEService: WeatherBLEService? = null
    private var connected = false

    private val compositeDisposable = CompositeDisposable()
    private val TAG = "MainActivity"

    private val SCAN_PERIOD: Long = 10000
    private val REQUEST_ENABLE_BT = 200

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as? WeatherBLEService.LocalBinder
            weatherBLEService = binder?.service
            weatherBLEService?.let {
                if (!it.initialize()) {
                    Log.e(TAG, "onServiceConnected: Unable to initialize Bluetooth")
                }
                deviceAddress?.let { addr ->
                    val result = it.connect(addr)
                    Log.d(TAG, "onServiceConnected connect request result $result")
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            weatherBLEService = null
        }
    }

    private val bleScanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            val device = result.device
            val deviceName = device.name
            
            // Check for the specific Arduino Nano RP2040 Connect device
            val hasTargetName = deviceName != null && (deviceName.contains(Constants.ARDUINO_DEVICE_NAME, ignoreCase = true))

            if (hasTargetName) {
                deviceAddress = device.address
                Log.d(TAG, "onScanResult Found Target Device: $deviceName ($deviceAddress)")
                connectToBLEDevice(deviceAddress!!)

                // Stop scanning once the target device is found
                if (scanning) {
                    try {
                        bluetoothAdapter?.bluetoothLeScanner?.stopScan(this)
                        scanning = false
                    } catch (e: SecurityException) {
                        Log.e(TAG, "SecurityException while stopping scan: ${e.message}")
                    }
                }
            } else {
                Log.d(TAG, "onScanResult Ignored Device: ${deviceName ?: "Unknown"} (${device.address})")
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.e(TAG, "onScanFailed: $errorCode")
        }
    }

    private val gattUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                WeatherBLEService.ACTION_GATT_CONNECTED -> {
                    connected = true
                    Toast.makeText(context, "Bluetooth Device connected", Toast.LENGTH_SHORT).show()
                }
                WeatherBLEService.ACTION_GATT_DISCONNECTED -> {
                    connected = false
                    Toast.makeText(context, "Bluetooth Device not connected", Toast.LENGTH_SHORT).show()
                }
                WeatherBLEService.ACTION_DATA_AVAILABLE -> {
                    val sensorData = intent.getFloatArrayExtra(WeatherBLEService.EXTRA_SENSOR_DATA)
                    sensorData?.let {
                        Log.d(TAG, "onReceive Sensor Data: ${it.contentToString()}")
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        
        (application as WeatherApplication).applicationComponent.inject(this)

        val weatherViewModel = ViewModelProvider(this, viewModelFactory)[WeatherViewModel::class.java]
        val userViewModel = ViewModelProvider(this, viewModelFactory)[UserViewModel::class.java]
        val placeViewModel = ViewModelProvider(this, viewModelFactory)[PlaceViewModel::class.java]
        val sensorDataViewModel = ViewModelProvider(this, viewModelFactory)[SensorDataViewModel::class.java]

        setContent {
            BasicWeatherAppTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
                    val navController = rememberNavController()
                    var showRainPrediction by remember { mutableStateOf(false) }
                    var isOfflineMode by remember { mutableStateOf(false) }

                    NavHost(navController = navController, startDestination = "main") {
                        composable(
                            route = "main?placeName={placeName}&region={region}&country={country}",
                            arguments = listOf(
                                navArgument("placeName") { defaultValue = ""; type = NavType.StringType },
                                navArgument("region") { defaultValue = ""; type = NavType.StringType },
                                navArgument("country") { defaultValue = ""; type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val pName = backStackEntry.arguments?.getString("placeName") ?: ""
                            val pRegion = backStackEntry.arguments?.getString("region") ?: ""
                            val pCountry = backStackEntry.arguments?.getString("country") ?: ""
                            
                            var forecastResponse by remember { mutableStateOf<ForecastResponse?>(null) }
                            var introText by remember { mutableStateOf("Welcome!") }
                            var locationName by remember { mutableStateOf("Vosloorus") }

                            LaunchedEffect(pName, pRegion, pCountry) {
                                loadWeatherData(
                                    pName, pRegion, pCountry, 
                                    userViewModel, weatherViewModel, placeViewModel,
                                    onDataLoaded = { response, intro, loc, isOffline ->
                                        forecastResponse = response
                                        introText = intro
                                        locationName = loc
                                        isOfflineMode = isOffline
                                    }
                                )
                            }

                            MainScreen(
                                forecastResponse = forecastResponse,
                                introText = introText,
                                locationName = locationName,
                                isOfflineMode = isOfflineMode,
                                userViewModel = userViewModel,
                                placeViewModel = placeViewModel,
                                weatherViewModel = weatherViewModel,
                                onSensorClick = { navController.navigate("sensor") },
                                onLocationSelected = { place ->
                                    navController.navigate("main?placeName=${place.name}&region=${place.region}&country=${place.country}") {
                                        popUpTo("main") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("sensor") {
                            SensorDisplayScreen(
                                sensorDataViewModel = sensorDataViewModel,
                                onBackClick = { navController.popBackStack() },
                                onRainPredictionClick = { showRainPrediction = true }
                            )
                        }
                        composable(
                            route = "search_results/{locations}",
                            arguments = listOf(navArgument("locations") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val locationsStr = backStackEntry.arguments?.getString("locations") ?: ""
                            val locations = locationsStr.split(",").toTypedArray()
                            SearchResultsScreen(
                                locations = locations,
                                weatherViewModel = weatherViewModel,
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                    }
                    
                    if (showRainPrediction) {
                        RainPredictionBottomSheet(
                            sensorDataViewModel = sensorDataViewModel,
                            onDismiss = { showRainPrediction = false }
                        )
                    }
                }
            }
        }

        checkPermissionsAndStartBT()
    }

    private fun checkPermissionsAndStartBT() {
        val permissions = mutableListOf<String>()

        // Bluetooth Permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            permissions.add(Manifest.permission.BLUETOOTH)
            permissions.add(Manifest.permission.BLUETOOTH_ADMIN)
        }

        // Location Permissions (Required for BLE scanning on older versions)
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)

        // Notification Permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val missingPermissions = permissions.filter {
            ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), REQUEST_ENABLE_BT)
        } else {
            startBluetoothOperations()
        }
    }

    private fun startBluetoothOperations() {
        bluetoothManager = getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager?.adapter

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not supported", Toast.LENGTH_SHORT).show()
        } else {
            if (!bluetoothAdapter!!.isEnabled) {
                try {
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    startActivity(enableBtIntent)
                } catch (e: SecurityException) {
                    Log.e(TAG, "SecurityException while enabling BT: ${e.message}")
                }
            } else {
                scanBLEDevice()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_ENABLE_BT) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                startBluetoothOperations()
            } else {
                Toast.makeText(this, "Permissions required for weather sensor features", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun scanBLEDevice() {
        bluetoothAdapter?.bluetoothLeScanner?.let { scanner ->
            if (!scanning) {
                handler.postDelayed({
                    scanning = false
                    try {
                        scanner.stopScan(bleScanCallback)
                    } catch (e: SecurityException) {
                        Log.e(TAG, "SecurityException: ${e.message}")
                    }
                }, SCAN_PERIOD)
                scanning = true
                try {
                    scanner.startScan(bleScanCallback)
                } catch (e: SecurityException) {
                    Log.e(TAG, "SecurityException: ${e.message}")
                }
            }
        }
    }

    private fun connectToBLEDevice(address: String) {
        deviceAddress = address
        val gattServiceIntent = Intent(this, WeatherBLEService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(gattServiceIntent)
        } else {
            startService(gattServiceIntent)
        }
        bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun loadWeatherData(
        placeName: String?,
        region: String?,
        country: String?,
        userViewModel: UserViewModel,
        weatherViewModel: WeatherViewModel,
        placeViewModel: PlaceViewModel,
        onDataLoaded: (ForecastResponse, String, String, Boolean) -> Unit
    ) {
        // Step 1: Load cached data immediately
        val cacheDisposable = Observable.zip(
            weatherViewModel.cachedForecast,
            userViewModel.allUsers
        ) { cachedResponse, users -> Pair(cachedResponse, users) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ (cachedResponse, users) ->
                val intro = if (users.isNotEmpty()) {
                    "Have a look at today's weather ${users.last().name_and_surname}"
                } else "Have a look at today's weather"
                
                val location = "${cachedResponse.location.name}, ${cachedResponse.location.region}, ${cachedResponse.location.country}"
                onDataLoaded(cachedResponse, intro, location, true)
            }, {
                Log.d(TAG, "No cached forecast found")
            })
        compositeDisposable.add(cacheDisposable)

        // Step 2: Attempt to load fresh data from network
        if (!placeName.isNullOrEmpty() && !region.isNullOrEmpty() && !country.isNullOrEmpty()) {
            val weatherDisposable = userViewModel.allUsers
                .subscribeOn(Schedulers.io())
                .flatMap { users ->
                    val intro = if (users.isNotEmpty()) {
                        "Have a look at today's weather ${users.last().name_and_surname}"
                    } else "Have a look at today's weather"
                    
                    val location = "$placeName, $region, $country"
                    
                    weatherViewModel.getForecasts(3, placeName)
                        .doOnNext { response ->
                            weatherViewModel.cacheForecast(response)
                                .subscribeOn(Schedulers.io())
                                .subscribe()
                        }
                        .map { response -> Triple(response, intro, location) }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ (response, intro, location) ->
                    onDataLoaded(response, intro, location, false)
                }, { error ->
                    Log.e(TAG, "loadWeatherData network error: ", error)
                })
            compositeDisposable.add(weatherDisposable)
        } else {
             val disposable = Observable.zip(
                placeViewModel.allPlaces,
                userViewModel.allUsers
             ) { places, users -> Pair(places, users) }
                .subscribeOn(Schedulers.io())
                .flatMap { (places, users) ->
                    var location = "Vosloorus"
                    if (places.isNotEmpty()) {
                        val place = places.first()
                        location = "${place.name}, ${place.region}, ${place.country}"
                    }
                    
                    val intro = if (users.isNotEmpty()) {
                        "Have a look at today's weather ${users.last().name_and_surname}"
                    } else "Have a look at today's weather"
                    
                    weatherViewModel.getForecasts(3, location)
                        .doOnNext { response ->
                            weatherViewModel.cacheForecast(response)
                                .subscribeOn(Schedulers.io())
                                .subscribe()
                        }
                        .map { response -> Triple(response, intro, location) }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ (response, intro, location) ->
                    onDataLoaded(response, intro, location, false)
                }, { error ->
                    Log.e(TAG, "loadWeatherData fallback error: ", error)
                })
            compositeDisposable.add(disposable)
        }
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter().apply {
            addAction(WeatherBLEService.ACTION_GATT_CONNECTED)
            addAction(WeatherBLEService.ACTION_GATT_DISCONNECTED)
            addAction(WeatherBLEService.ACTION_GATT_SERVICES_DISCOVERED)
            addAction(WeatherBLEService.ACTION_DATA_AVAILABLE)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(gattUpdateReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(gattUpdateReceiver, filter)
        }
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(gattUpdateReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (weatherBLEService != null) {
            unbindService(serviceConnection)
        }
        compositeDisposable.clear()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RainPredictionBottomSheet(
    sensorDataViewModel: SensorDataViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val sensorDataList by sensorDataViewModel.allSensorData.subscribeAsState(initial = emptyList())
    
    val compositeDisposable = remember { CompositeDisposable() }
    DisposableEffect(Unit) {
        onDispose {
            compositeDisposable.clear()
        }
    }

    val analysis = remember(sensorDataList) {
        if (sensorDataList.isNotEmpty()) {
            val current = sensorDataList.last()
            val lastRecordDate = current.date.substring(0, 10)
            val relevantData = sensorDataList.filter { it.date.startsWith(lastRecordDate) }
            
            val pressureTrends = calculatePressureTrends(relevantData)
            val dewPoint = calculateDewPoint(current.temperature, current.humidity)
            val prediction = predictRain(current.temperature, dewPoint)
            
            RainAnalysis(
                prediction = prediction,
                spread = current.temperature - dewPoint,
                dewPoint = dewPoint,
                trends = pressureTrends,
                lastUpdated = current.date,
                temp = current.temperature,
                humidity = current.humidity,
                pressure = current.pressure
            )
        } else null
    }

    var feedbackSubmitted by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White.copy(alpha = 0.95f),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray.copy(alpha = 0.5f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 40.dp, start = 20.dp, end = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Rain Analysis",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Muli,
                color = Color.Black
            )
            
            if (analysis != null) {
                Text(
                    text = "Based on data from ${analysis!!.lastUpdated.replace("T", " ").substring(0, 16)}",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontFamily = Muli,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            if (analysis != null) {
                // Prediction Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = getPredictionColor(analysis!!.prediction).copy(alpha = 0.1f)
                    ),
                    border = BorderStroke(1.dp, getPredictionColor(analysis!!.prediction).copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = analysis!!.prediction,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = getPredictionColor(analysis!!.prediction),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            fontFamily = Muli
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Verification Section
                if (!feedbackSubmitted && (analysis!!.spread < 10)) {
                    Text(
                        text = "Is it actually raining now?",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = Muli,
                        color = Color.Black.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val outcomes = listOf("Heavy Rain", "Light Rain", "Cloudy", "Windy", "Lightning", "Clear")
                        items(outcomes) { outcome ->
                            PredictionChip(outcome, analysis!!, sensorDataViewModel, compositeDisposable) { feedbackSubmitted = true }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                } else if (feedbackSubmitted) {
                    Text(
                        text = "Thank you for your feedback!",
                        fontSize = 14.sp,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Medium,
                        fontFamily = Muli
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Stats Grid
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AnalysisStatCard("Spread", String.format(Locale.ENGLISH, "%.2f ℃", analysis!!.spread), Modifier.weight(1f))
                    AnalysisStatCard("Dew Point", String.format(Locale.ENGLISH, "%.2f ℃", analysis!!.dewPoint), Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Trends
                Text(
                    text = "Pressure Trends",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start),
                    fontFamily = Muli
                )
                
                Spacer(modifier = Modifier.height(10.dp))
                
                TrendRow("30 min", analysis!!.trends[0])
                TrendRow("1 hour", analysis!!.trends[1])
                TrendRow("2 hours", analysis!!.trends[2])
                TrendRow("3 hours", analysis!!.trends[3])

            } else {
                CircularProgressIndicator(modifier = Modifier.padding(40.dp))
            }
        }
    }
}

@Composable
fun PredictionChip(
    outcome: String,
    analysis: RainAnalysis,
    sensorDataViewModel: SensorDataViewModel,
    compositeDisposable: CompositeDisposable,
    onSuccess: () -> Unit
) {
    AssistChip(
        onClick = {
            val verification = PredictionVerification(
                analysis.lastUpdated,
                analysis.temp,
                analysis.humidity,
                analysis.pressure,
                analysis.prediction,
                outcome,
                0.0, // No residual for static rain prediction
                "STATIC_THRESHOLD"
            )
            compositeDisposable.add(
                sensorDataViewModel.insertVerification(verification)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        onSuccess()
                    }, {
                        Log.e("RainPrediction", "Error saving feedback", it)
                    })
            )
        },
        label = { Text(outcome, fontFamily = Muli, fontSize = 12.sp) },
        modifier = Modifier.width(100.dp),
        colors = AssistChipDefaults.assistChipColors(
            labelColor = when(outcome) {
                "Heavy Rain" -> Color(0xFFD32F2F)
                "Clear" -> Color(0xFF388E3C)
                else -> Color.DarkGray
            }
        )
    )
}

@Composable
fun AnalysisStatCard(label: String, value: String, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = label, fontSize = 12.sp, color = Color.Gray, fontFamily = Muli)
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black, fontFamily = Muli)
        }
    }
}

@Composable
fun TrendRow(label: String, value: Float) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, modifier = Modifier.width(80.dp), fontSize = 14.sp, fontFamily = Muli)
        LinearProgressIndicator(
            progress = { (value + 5f) / 10f }, // Mapping trend range to 0..1
            modifier = Modifier.weight(1f).height(6.dp),
            color = if (value >= 0) Color(0xFF4CAF50) else Color(0xFFF44336),
            trackColor = Color.Black.copy(alpha = 0.05f),
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        Text(
            text = String.format(Locale.ENGLISH, "%+.2f", value),
            modifier = Modifier.width(60.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = Muli
        )
    }
}

private fun getPredictionColor(prediction: String): Color {
    return when {
        prediction.contains("Heavy") -> Color(0xFFD32F2F)
        prediction.contains("Light") -> Color(0xFF1976D2)
        prediction.contains("Unlikely") -> Color(0xFF388E3C)
        else -> Color(0xFF7B1FA2)
    }
}

data class RainAnalysis(
    val prediction: String,
    val spread: Float,
    val dewPoint: Float,
    val trends: FloatArray,
    val lastUpdated: String,
    val temp: Float,
    val humidity: Float,
    val pressure: Float
)

private fun calculateDewPoint(temp: Float, humidity: Float): Float {
    val a = 17.625f
    val b = 243.04f
    val alpha = (Math.log(humidity.toDouble() / 100.0) + (a * temp) / (b + temp)).toFloat()
    return (b * alpha) / (a - alpha)
}

private fun calculatePressureTrends(sensorData: List<SensorData>): FloatArray {
    val listSize = sensorData.size
    val currentPosition = listSize - 1
    
    val t30 = 30 / 10
    val t1h = 60 / 10
    val t2h = 120 / 10
    val t3h = 180 / 10
    
    val trends = FloatArray(4)
    if (listSize > t30) trends[0] = sensorData[currentPosition].pressure - sensorData[listSize - t30].pressure
    if (listSize > t1h) trends[1] = sensorData[currentPosition].pressure - sensorData[listSize - t1h].pressure
    if (listSize > t2h) trends[2] = sensorData[currentPosition].pressure - sensorData[listSize - t2h].pressure
    if (listSize > t3h) trends[3] = sensorData[currentPosition].pressure - sensorData[listSize - t3h].pressure
    
    return trends
}

private fun predictRain(temp: Float, dewPoint: Float): String {
    val spread = temp - dewPoint
    return when {
        spread < 4 -> "Heavy Rain currently or Rain is Imminent"
        spread < 5.5 -> "Rain, Light or Normal is expected soon / currently raining"
        spread < 10 -> "Unlikely To Rain"
        else -> "Very Dry - No Rain"
    }
}
