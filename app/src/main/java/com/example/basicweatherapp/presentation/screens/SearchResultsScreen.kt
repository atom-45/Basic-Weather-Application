package com.example.basicweatherapp.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.basicweatherapp.data.models.Place
import com.example.basicweatherapp.presentation.theme.BasicWeatherAppTheme
import com.example.basicweatherapp.presentation.theme.Muli
import com.example.basicweatherapp.presentation.viewmodels.WeatherViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultsScreen(
    locations: Array<String>,
    weatherViewModel: WeatherViewModel,
    onBackClick: () -> Unit
) {
    var places by remember { mutableStateOf<List<Place>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var headerText by remember { mutableStateOf("") }

    LaunchedEffect(locations) {
        isLoading = true
        if (locations.size == 1) {
            headerText = "Nearby places at ${locations[0]}"
            weatherViewModel.getPlaces(locations[0])
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ results ->
                    places = results ?: emptyList()
                    isLoading = false
                }, {
                    isLoading = false
                })
        } else if (locations.size >= 2) {
            headerText = "Nearby places at ${locations[0]} and ${locations[1]}"
            weatherViewModel.getCombinedPlaces(
                weatherViewModel.getPlaces(locations[0]),
                weatherViewModel.getPlaces(locations[1])
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ results ->
                    places = results ?: emptyList()
                    isLoading = false
                }, {
                    isLoading = false
                })
        }
    }

    SearchResultsContent(
        headerText = headerText,
        places = places,
        isLoading = isLoading,
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultsContent(
    headerText: String,
    places: List<Place>,
    isLoading: Boolean,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(headerText, fontSize = 18.sp, fontFamily = Muli) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                    items(places) { place ->
                        SimplePlaceCard(place = place)
                    }
                }
            }
        }
    }
}

@Composable
fun SimplePlaceCard(place: Place) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${place.name}, ${place.region}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Muli,
                color = Color.Black
            )
            Text(
                text = place.country,
                fontSize = 14.sp,
                fontFamily = Muli,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = String.format(Locale.ENGLISH, "Lat: %.2f, Lon: %.2f", place.lat, place.lon),
                fontSize = 12.sp,
                fontFamily = Muli,
                color = Color.DarkGray
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchResultsScreenPreview() {
    BasicWeatherAppTheme {
        SearchResultsContent(
            headerText = "Nearby places at New York",
            places = listOf(
                Place("Brooklyn", "New York", "USA", 40.6, -73.9, ""),
                Place("Queens", "New York", "USA", 40.7, -73.8, "")
            ),
            isLoading = false,
            onBackClick = {}
        )
    }
}
