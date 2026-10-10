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
- **Local Persistence (Room - Version 8):** Stores user profiles, saved locations, sensor data history, cached weather forecasts, real-time verification logs, and the comprehensive **Thermodynamic Black Box** prediction history.
  - **Migration 6 to 7 (`MIGRATION_6_7`):** Added `verified_timestamp` column to record the exact human verification timestamp ($T_{\text{verify}}$) for phase error calculations.
  - **Migration 7 to 8 (`MIGRATION_7_8`):** Added `notified_45m` column (`INTEGER NOT NULL DEFAULT 0`) to track one-time background 45-minute countdown alert dispatches.
- **Remote API (Retrofit):** Fetches global weather data from WeatherAPI.com.
- **BLE Service:** `WeatherBLEService` handles real-time data acquisition, **Background Atmospheric Analysis**, periodic 45-minute countdown monitoring, and high-priority heads-up notifications.

### Thermodynamic Analysis Engine (physics/)
- **Theoretical Basis:** Implements the First Law of Thermodynamics ($dT/dt = \frac{1}{\rho c_p} \frac{dp}{dt} + \frac{J}{c_p}$) to analyze local air parcels.
- **Numerical Methods:** Uses **Backward Difference** for derivative calculations ($dp/dt$, $dT/dt$) and **Forward Euler** for rain arrival time extrapolation.
- **Dynamic Density:** Calculates air density ($\rho$) in real time using Virtual Temperature correction for relative humidity.
- **Black Box Audit System:** Automatically records the engine's prediction snapshots alongside exact sensor parameters.
- **Merged Hybrid Verification Model:**
  - **Targeted Arrival-Window Back-Propagation:** When a user verifies a Live Audit card, back-propagation updates pending predictions matching the specific target arrival window with a **5-minute tolerance window** ($\pm 5\text{ mins}$ around native 10-minute window = 20-minute total span).
  - **Isolation of Long-Horizon Forecasts:** Long-horizon forecasts (3–4+ hours out) are completely isolated and remain `PENDING` until their specific target arrival window arrives.
  - **Immutability Protection:** Once a prediction snapshot is marked `VERIFIED` (`verification_status = 'VERIFIED'`), it can NEVER be overwritten or re-verified by subsequent audits.
  - **Phase Error Tracking ($\Delta t$):** Captures `$T_{\text{verify}}$` (ISO timestamp when user tapped verification) alongside `$T_{\text{predicted\_arrival}}$`, allowing exact phase error ($\Delta t = T_{\text{verify}} - T_{\text{predicted}}$) computation in exported datasets.
  - **Human Catchment Window:** Accommodates 10-minute BLE sensor update cycles and real-world human response times without requiring exact-second button presses.
- **Core Components:**
    - `DensityCalculator`: Dynamic $\rho$ calculation with virtual temperature correction.
    - `StormThermodynamicsEngine`: Solves the DE to detect atmospheric energy anomalies (residuals).
    - `RainAnalysisEngine`: Centralized logic for static dew-point-based saturation thresholds.
    - `DewPointCalculator`: Magnus-Tetens approximation for saturation thresholds.

## Background Notification Systems (`WeatherBLEService.java`)

The background service implements a multi-stage alert pipeline using high-priority heads-up notification channels (`WeatherAlertChannel`):

1. **Long-Horizon Scheduled 45-Minute Countdown Alert**:
   - Continuously monitors pending predictions in Room (`getPredictionsNeeding45mAlert`).
   - When a forecast created hours in advance reaches **45 minutes before its target arrival time** ($T_{\text{now}} \ge T_{\text{arrival}} - 45\text{m}$), the service fires a high-priority heads-up alert:
     * **Title**: `"Thermodynamic Alert: Upcoming Storm"`
     * **Content**: `"Storm predicted at 19:45 is expected in ~45 minutes. Prepare for rain."`
   - Sets `notified_45m = 1` to guarantee the 45-minute countdown alert fires **exactly once** per forecast.

2. **Imminent Sensor Reading Alert**:
   - Fires during live BLE sensor processing if current atmospheric analysis detects rain arriving within 45 minutes or an immediate unstable energy shift ($dT/dt$ residual anomaly or rapid pressure drop $> 1\text{ hPa/hr}$).

3. **Notification Banner De-duplication**:
   - All alerts share `ALERT_NOTIFICATION_ID = 204` on `WeatherAlertChannel`, smoothly updating the existing heads-up banner on Android instead of cluttering the status bar with duplicate cards.

## Core Screens

### 1. Main Dashboard (`MainScreen.kt`)
- **Integrated Search:** A `DockedSearchBar` featuring a lifecycle-aware `LaunchedEffect` for reactive, disposable-free location searching.
- **User Welcome Tile:** Personalized greeting with inline editing and persistence.
- **Bento Weather Grid:** Modern layout for primary weather conditions and astronomical data.

### 2. Sensor Display Screen (`SensorDisplayScreen.kt`)
- **Bento Grid:** Displays real-time Temperature, Humidity, Pressure, and Altitude from the BLE sensor.
- **Thermodynamic Analysis Section:** Displays storm approach speed, exact arrival timestamps, and safety verdicts.
- **Live Model Audit Card:** A dynamic component that appears strictly during a prediction's active arrival window ($T_{\text{now}} \in [\text{windowStart}, \text{windowEnd}]$) to request ground-truth verification.
- **De-duplicated Safety Verification Row:** 
  - The secondary "Verify this Thermodynamic Analysis" card is filtered to only appear for imminent events ($\le 30$ mins) AND when no active Live Model Audit Card is displaying (`activePrediction == null`), eliminating double prompts and premature distant forecast feedback.
- **Interactive Trends:** MPAndroidChart auto-updates with new sensor data. Supports historical data viewing up to 5 hours.

### 3. Dataset Export & WorkManager (`ExportWorker.kt`)
- Generates a compressed ZIP file (`weather_export_<date>.zip`) saved to the device `Downloads` directory containing three CSV datasets for offline research and Python model calibration:
  1. `thermodynamic_blackbox.csv`: Contains `Timestamp`, `Temp`, `Humidity`, `Pressure`, `PredictedArrival`, `VerifiedTimestamp`, `Speed`, `Residual`, `EventID`, `Status`, `ActualOutcome`.
  2. `labeled_verifications.csv`: Contains real-time Tier 1 ground-truth feedback logs.
  3. `raw_sensor_data.csv`: High-frequency sensor telemetry history.

## Security & Configuration
- **API Key Handling:** The WeatherAPI key is stored in `local.properties` and accessed via `BuildConfig`.
- **Permissions:** Dynamically handles Bluetooth (Scan/Connect) and Notification permissions (required for WorkManager feedback and BLE alerts on API 33+).
- **Offline Mode:** The app automatically caches the last successful weather fetch in the Room database.

## Build Requirements
- **Android Studio:** Ladybug / Quail or newer recommended.
- **Min SDK:** 26 (Android 8.0).
- **Target SDK:** 34.
- **Compose BOM:** 2025.02.00 (Stable).
