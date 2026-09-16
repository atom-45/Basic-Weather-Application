# Project Documentation: Basic Weather App

## Overview
The Basic Weather App is a comprehensive weather monitoring solution that integrates global weather forecasts with local sensor data via Bluetooth Low Energy (BLE). The application has been fully migrated from a legacy Fragment-based XML architecture to a modern **Jetpack Compose** implementation with a **Single Activity** design.

## Architecture

### UI Layer (Jetpack Compose)
- **Single Activity Architecture:** `MainActivity.kt` serves as the entry point and host for the navigation graph.
- **Reactive Lifecycle Management:** Utilizes `subscribeAsState()` for leak-proof RxJava integration, tying data streams directly to Composable lifecycles.
- **Navigation:** Uses `androidx.navigation.compose` for seamless transitions between the dashboard and detailed sensor views.
- **Theme:** A custom "Glassmorphism" theme (`presentation/theme/`) utilizing semi-transparent surfaces, dynamic gradients, and the Muli font.

### ViewModel & State Management
- **MVVM Pattern:** ViewModels manage UI state and interact with repositories.
- **Physics Package Integration:** ViewModels utilize a dedicated `physics` package to perform atmospheric energy balance calculations.
- **Dagger 2:** Handles dependency injection for ViewModels and Repositories.

### Data Layer
- **Local Persistence (Room - Version 5):** Stores user profiles, saved locations, sensor data history, cached weather forecasts, and **expanded prediction verifications** (including thermodynamic residuals).
- **Remote API (Retrofit):** Fetches global weather data from WeatherAPI.com.
- **BLE Service:** `WeatherBLEService` handles real-time data acquisition. Optimized for **Arduino Nano RP2040 Connect**.

### Thermodynamic Analysis Engine (physics/)
- **Theoretical Basis:** Implements the First Law of Thermodynamics ($dT/dt = \frac{1}{\rho c_p} \frac{dp}{dt} + \frac{J}{c_p}$) to analyze local air parcels.
- **Numerical Methods:** Uses **Backward Difference** for tendency calculation and **Forward Euler** for rain arrival time projection.
- **Core Components:**
    - `DensityCalculator`: Dynamic $\rho$ calculation with virtual temperature correction.
    - `StormThermodynamicsEngine`: Solves the DE to detect atmospheric energy anomalies (residuals).
    - `DewPointCalculator`: Magnus-Tetens approximation for saturation thresholds.

## Core Screens

### 1. Main Dashboard (`MainScreen.kt`)
- **Integrated Search:** A `DockedSearchBar` featuring a lifecycle-aware `LaunchedEffect` for reactive, disposable-free location searching.
- **User Welcome Tile:** Personalized greeting with inline editing and persistence.
- **Bento Weather Grid:** Modern layout for primary weather conditions and astronomical data.

### 2. Sensor Display Screen (`SensorDisplayScreen.kt`)
- **Bento Grid:** Displays real-time Temperature, Humidity, Pressure, and Altitude from the BLE sensor.
- **Thermodynamic Analysis Section:** Displays storm approach speed, exact arrival timestamps (e.g., "16:45"), and safety verdicts.
- **Verification Loop:** Integrated horizontal chip row for ground-truth reporting (Heavy Rain, Clear, etc.).
- **Interactive Trends:** MPAndroidChart auto-updates with new sensor data. Supports historical data viewing up to 5 hours.

### 3. Rain Analysis Bottom Sheet
- **Static vs. Dynamic Analysis:** Compares static dew point spread against the dynamic thermodynamic engine.
- **Metrics:** Displays Dew Point, Temperature Spread, and multi-stage Pressure Trends (30m to 3h).

## Security & Configuration
- **API Key Handling:** The WeatherAPI key is stored in `local.properties` and accessed via `BuildConfig`.
- **Permissions:** Dynamically handles Bluetooth (Scan/Connect) and Notification permissions (required for WorkManager feedback on API 33+).
- **Offline Mode:** The app automatically caches the last successful weather fetch in the Room database.

## Build Requirements
- **Android Studio:** Ladybug or newer recommended.
- **Min SDK:** 26 (Android 8.0).
- **Target SDK:** 34.
- **Compose BOM:** 2025.02.00 (Stable).
