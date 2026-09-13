package com.example.basicweatherapp.presentation.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rxjava3.subscribeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.basicweatherapp.R
import com.example.basicweatherapp.data.models.ForecastDay
import com.example.basicweatherapp.data.models.Hour
import com.example.basicweatherapp.data.models.Place
import com.example.basicweatherapp.data.models.User
import com.example.basicweatherapp.data.remote.responses.ForecastResponse
import com.example.basicweatherapp.presentation.theme.BasicWeatherAppTheme
import com.example.basicweatherapp.presentation.theme.LightBlue600
import com.example.basicweatherapp.presentation.theme.Muli
import com.example.basicweatherapp.presentation.theme.Orange
import com.example.basicweatherapp.presentation.viewmodels.PlaceViewModel
import com.example.basicweatherapp.presentation.viewmodels.UserViewModel
import com.example.basicweatherapp.presentation.viewmodels.WeatherViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    forecastResponse: ForecastResponse?,
    introText: String,
    locationName: String,
    isOfflineMode: Boolean,
    userViewModel: UserViewModel,
    placeViewModel: PlaceViewModel,
    weatherViewModel: WeatherViewModel,
    onSensorClick: () -> Unit,
    onLocationSelected: (Place) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf<List<Place>>(emptyList()) }
    
    var isEditingName by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf("") }
    val context = LocalContext.current
    
    val savedPlacesState = placeViewModel.allPlaces.subscribeAsState(initial = emptyList())
    val savedPlaces = savedPlacesState.value

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(LightBlue600, Orange)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Integrated Search Bar Header
            Box(modifier = Modifier.fillMaxWidth().zIndex(1f)) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    DockedSearchBar(
                        modifier = Modifier.fillMaxWidth(),
                        query = searchQuery,
                        onQueryChange = { 
                            searchQuery = it
                            if (it.isNotEmpty()) {
                                weatherViewModel.getPlaces(it)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe({ places ->
                                        searchResults = places ?: emptyList()
                                    }, {
                                        searchResults = emptyList()
                                    })
                            } else {
                                searchResults = emptyList()
                            }
                        },
                        onSearch = { isSearchActive = false },
                        active = isSearchActive,
                        onActiveChange = { isSearchActive = it },
                        placeholder = { Text(locationName, color = if (isSearchActive) Color.Gray else Color.White) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = if (isSearchActive) Color.Gray else Color.White) },
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isSearchActive) {
                                    IconButton(onClick = { 
                                        searchQuery = ""
                                        isSearchActive = false
                                    }) {
                                        Icon(Icons.Default.Close, contentDescription = null)
                                    }
                                }
                                IconButton(onClick = onSensorClick) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.outline_chart_data_24),
                                        contentDescription = "Sensor",
                                        tint = if (isSearchActive) Color.Gray else Color.White
                                    )
                                }
                            }
                        },
                        colors = SearchBarDefaults.colors(
                            containerColor = if (isSearchActive) Color.White else Color.White.copy(alpha = 0.2f),
                            dividerColor = Color.LightGray
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        val displayList = if (searchQuery.isEmpty()) savedPlaces else searchResults
                        
                        if (searchQuery.isEmpty() && savedPlaces.isNotEmpty()) {
                            Text(
                                text = "Saved Locations",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.Gray
                            )
                        }

                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)
                        ) {
                            items(
                                count = displayList.size,
                                key = { index -> 
                                    val p = displayList[index]
                                    p.name + p.lat + p.lon 
                                }
                            ) { index ->
                                val place = displayList[index]
                                // Only allow swipe to delete for saved places (when query is empty)
                                if (searchQuery.isEmpty()) {
                                    val dismissState = rememberSwipeToDismissBoxState(
                                        confirmValueChange = {
                                            if (it == SwipeToDismissBoxValue.EndToStart) {
                                                placeViewModel.deletePlace(place)
                                                    .subscribeOn(Schedulers.io())
                                                    .subscribe()
                                                true
                                            } else false
                                        }
                                    )

                                    SwipeToDismissBox(
                                        state = dismissState,
                                        enableDismissFromStartToEnd = false,
                                        backgroundContent = {
                                            Box(
                                                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                                                contentAlignment = Alignment.CenterEnd
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                                            }
                                        }
                                    ) {
                                        LocationListItem(place, onLocationSelected) {
                                            searchQuery = ""
                                            isSearchActive = false
                                        }
                                    }
                                } else {
                                    LocationListItem(place, onLocationSelected) {
                                        searchQuery = ""
                                        isSearchActive = false
                                        // Save new location
                                        placeViewModel.addPlace(place)
                                            .subscribeOn(Schedulers.io())
                                            .subscribe()
                                    }
                                }
                            }
                        }
                    }

                    // Subtle Offline Indicator
                    AnimatedVisibility(
                        visible = isOfflineMode,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Surface(
                            modifier = Modifier.padding(top = 8.dp).align(Alignment.CenterHorizontally),
                            color = Color.Black.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Offline: Showing cached weather", color = Color.White, fontSize = 11.sp, fontFamily = Muli)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Integrated User Tile
            UserWelcomeTile(
                introText = introText,
                isEditing = isEditingName,
                editedName = editedName,
                onEditClick = { 
                    isEditingName = true
                    editedName = ""
                },
                onNameChange = { editedName = it },
                onSaveName = {
                    if (editedName.isNotEmpty()) {
                        userViewModel.insertUser(User(editedName))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({
                                isEditingName = false
                                Toast.makeText(context, "Welcome, $editedName!", Toast.LENGTH_SHORT).show()
                            }, {})
                    }
                }
            )

            if (forecastResponse != null) {
                val current = forecastResponse.current
                val forecastDay = forecastResponse.forecast.forecastDays[0]
                val astro = forecastDay.astro

                Spacer(modifier = Modifier.height(30.dp))

                // Hero Temperature Section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = "https:" + current.condition.iconURL,
                        contentDescription = null,
                        modifier = Modifier.size(120.dp),
                        contentScale = ContentScale.Fit
                    )
                    Text(
                        text = "${current.temp_c}°",
                        fontSize = 100.sp,
                        fontWeight = FontWeight.ExtraLight,
                        fontFamily = Muli,
                        color = Color.White,
                        modifier = Modifier.offset(y = (-10).dp)
                    )
                    Text(
                        text = current.condition.text,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = Muli,
                        color = Color.White,
                        modifier = Modifier.offset(y = (-20).dp)
                    )
                    Text(
                        text = convertLocalDate(current.lastUpdated),
                        fontSize = 14.sp,
                        fontFamily = Muli,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.offset(y = (-15).dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Bento Weather Details Grid
                Row(modifier = Modifier.fillMaxWidth()) {
                    WeatherDetailTile(
                        label = "Feels Like",
                        value = "${current.feelslikeTempC}°",
                        iconId = R.drawable.sunny,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    WeatherDetailTile(
                        label = "Wind",
                        value = "${current.windSpeed} km/h ${current.windDirection}",
                        iconId = R.drawable.wind_2,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    WeatherDetailTile(
                        label = "Humidity",
                        value = "${current.humidity}%",
                        iconId = R.drawable.outline_humidity_percentage_24,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    WeatherDetailTile(
                        label = "Rain Chance",
                        value = "${forecastDay.day.rain_probability}%",
                        iconId = R.drawable.rain_2,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))

                // Astro Dashboard Card
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = painterResource(id = getMoonPhaseResource(astro.moonPhase)),
                                contentDescription = null,
                                modifier = Modifier.size(64.dp)
                            )
                            Text(text = astro.moonPhase, color = Color.White, fontSize = 12.sp, fontFamily = Muli)
                        }
                        VerticalDivider(modifier = Modifier.height(80.dp).padding(horizontal = 16.dp), color = Color.White.copy(alpha = 0.2f))
                        Column(modifier = Modifier.weight(1.5f)) {
                            AstroRow("Sunrise", astro.sunriseTime)
                            AstroRow("Sunset", astro.sunsetTime)
                            AstroRow("Moonrise", astro.moonriseTime)
                            AstroRow("Moonset", astro.moonsetTime)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                // Hourly Forecast
                SectionHeader(stringResource(id = R.string.hourly_weather_forecast))
                val filteredHours = forecastDay.hours.filter { hour ->
                    val hr = hour.time.split(" ")[1].substring(0, 2).toInt()
                    hr >= LocalTime.now().hour
                }
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredHours) { hour ->
                        ModernHourCard(hour)
                    }
                }

                // Daily Forecast
                SectionHeader(stringResource(id = R.string._3_day_weather_forecast))
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(forecastResponse.forecast.forecastDays) { day ->
                        ModernForecastCard(day)
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = stringResource(id = R.string.weatherapi_com),
                modifier = Modifier.fillMaxWidth().padding(bottom = 40.dp),
                fontFamily = Muli,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun LocationListItem(place: Place, onLocationSelected: (Place) -> Unit, onSelect: () -> Unit) {
    ListItem(
        headlineContent = { Text("${place.name}, ${place.region}", color = Color.Black, fontFamily = Muli) },
        supportingContent = { Text(place.country, color = Color.DarkGray, fontFamily = Muli) },
        leadingContent = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray) },
        modifier = Modifier.clickable {
            onSelect()
            onLocationSelected(place)
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
fun UserWelcomeTile(
    introText: String,
    isEditing: Boolean,
    editedName: String,
    onEditClick: () -> Unit,
    onNameChange: (String) -> Unit,
    onSaveName: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isEditing) {
                OutlinedTextField(
                    value = editedName,
                    onValueChange = onNameChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Enter your name", color = Color.White.copy(alpha = 0.5f)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f)
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onSaveName() })
                )
                IconButton(onClick = onSaveName) {
                    Icon(painter = painterResource(id = R.drawable.baseline_add_circle_outline_24), contentDescription = null, tint = Color.White)
                }
            } else {
                Text(
                    text = introText,
                    modifier = Modifier.weight(1f),
                    fontFamily = Muli,
                    fontSize = 18.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White.copy(alpha = 0.7f))
                }
            }
        }
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = if (onClick != null) modifier.clickable { onClick() } else modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
        content = { content() }
    )
}

@Composable
fun WeatherDetailTile(label: String, value: String, iconId: Int, modifier: Modifier) {
    GlassCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(
                painter = painterResource(id = iconId),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = label, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, fontFamily = Muli)
            Text(text = value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = Muli)
        }
    }
}

@Composable
fun AstroRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, fontFamily = Muli)
        Text(text = value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium, fontFamily = Muli)
    }
}

@Composable
fun SectionHeader(text: String) {
    Text(
        text = text.uppercase(),
        modifier = Modifier.padding(vertical = 8.dp),
        fontFamily = Muli,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        color = Color.White.copy(alpha = 0.9f)
    )
}

@Composable
fun ModernHourCard(hour: Hour) {
    GlassCard(modifier = Modifier.width(100.dp)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = hour.time.split(" ")[1],
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.8f),
                fontFamily = Muli,
                textAlign = TextAlign.Center
            )
            AsyncImage(
                model = "https:" + hour.condition.iconURL,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                contentScale = ContentScale.Fit
            )
            Text(
                text = "${hour.temp_c}°",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontFamily = Muli,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(painter = painterResource(id = R.drawable.rain_2), contentDescription = null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = String.format(Locale.ENGLISH, "%.0f%%", hour.chanceOfRain),
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    fontFamily = Muli
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(painter = painterResource(id = R.drawable.wind_2), contentDescription = null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = String.format(Locale.ENGLISH, "%.0f km/h", hour.windKph),
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    fontFamily = Muli
                )
            }
        }
    }
}

@Composable
fun ModernForecastCard(forecastDay: ForecastDay) {
    GlassCard(modifier = Modifier.width(120.dp)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = formatDate(forecastDay.date),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.8f),
                fontFamily = Muli,
                textAlign = TextAlign.Center
            )
            AsyncImage(
                model = "https:" + forecastDay.day.condition.iconURL,
                contentDescription = null,
                modifier = Modifier.size(45.dp),
                contentScale = ContentScale.Fit
            )
            Text(
                text = "${forecastDay.day.maxTemperature}°",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontFamily = Muli,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(painter = painterResource(id = R.drawable.rain_2), contentDescription = null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${forecastDay.day.rain_probability}%",
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    fontFamily = Muli
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(painter = painterResource(id = R.drawable.wind_2), contentDescription = null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = String.format(Locale.ENGLISH, "%.0f km/h", forecastDay.day.maxWind),
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    fontFamily = Muli
                )
            }
        }
    }
}

private fun convertLocalDate(date: String): String {
    val dateSplit = date.split(" ")
    val localDate = LocalDate.parse(dateSplit[0])
    val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.ENGLISH)
    return localDate.format(formatter)
}

private fun formatDate(date: String): String {
    val localDate = LocalDate.parse(date)
    val formatter = DateTimeFormatter.ofPattern("EEE, d MMM", Locale.ENGLISH)
    return localDate.format(formatter)
}

private fun getMoonPhaseResource(moonPhase: String): Int {
    return when (moonPhase) {
        "New Moon" -> R.drawable.new_moon
        "Waxing Crescent" -> R.drawable.waxing_crescent
        "First Quarter" -> R.drawable.first_quarter
        "Waxing Gibbous" -> R.drawable.waxing_gibbous
        "Full Moon" -> R.drawable.full_moon
        "Waning Moon" -> R.drawable.waning_gibbous
        "Last Quarter" -> R.drawable.last_quarter
        else -> R.drawable.waning_crescent
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    BasicWeatherAppTheme {
        Text("Main Screen Preview (ViewModels required)", modifier = Modifier.padding(20.dp), color = Color.White)
    }
}
