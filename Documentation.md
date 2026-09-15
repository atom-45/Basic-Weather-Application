# Project Documentation: Basic Weather App

## Overview
The Basic Weather App is a comprehensive weather monitoring solution that integrates global weather forecasts with local sensor data via Bluetooth Low Energy (BLE). The application has been fully migrated from a legacy Fragment-based XML architecture to a modern **Jetpack Compose** implementation with a **Single Activity** design.

## Architecture

### UI Layer (Jetpack Compose)
- **Single Activity Architecture:** `MainActivity.kt` serves as the entry point and host for the navigation graph.
- **Navigation:** Uses `androidx.navigation.compose` for seamless transitions between the dashboard and detailed sensor views.
- **Theme:** A custom "Glassmorphism" theme (`presentation/theme/`) utilizing semi-transparent surfaces, dynamic gradients, and the Muli font.

### ViewModel & State Management
- **MVVM Pattern:** ViewModels manage UI state and interact with repositories.
- **RxJava 3 Integration:** ViewModels (written in Java) emit data as `Observable` streams, which are converted to Compose State using `subscribeAsState()`.
- **Dagger 2:** Handles dependency injection for ViewModels and Repositories.

### Data Layer
- **Local Persistence (Room - Version 4):** Stores user profiles, saved locations, sensor data history, cached weather forecasts, and **labeled prediction verifications**.
- **Remote API (Retrofit):** Fetches global weather data from WeatherAPI.com.
- **BLE Service:** `WeatherBLEService` handles real-time data acquisition. Optimized for **Arduino Nano RP2040 Connect** with intelligent target-device filtering.

### Background Processing
- **WorkManager:** Manages heavy, long-running tasks like historical dataset exports. The `ExportWorker` handles database queries, CSV generation, and ZIP compression in the background to ensure reliability and memory safety.

## Core Screens

### 1. Main Dashboard (`MainScreen.kt`)
- **Integrated Search:** A `DockedSearchBar` that allows real-time searching and management of saved locations with swipe-to-delete functionality.
- **User Welcome Tile:** Personalized greeting with inline editing capabilities.
- **Weather Overview:** Displays "Hero" temperature, current conditions, and essential metrics (Wind, Humidity, Feels Like).
- **Astronomy Card:** Shows moon phase visuals and solar/lunar times.
- **Forecast Rows:** Horizontal lists for hourly and 3-day forecasts with centralized alignment.

### 2. Sensor Display Screen (`SensorDisplayScreen.kt`)
- **Bento Grid:** Displays real-time Temperature, Humidity, Pressure, and Altitude from the BLE sensor. Cards feature refined vertical centering and hero-state font scaling.
- **Interactive Trends:** A configurable graph (MPAndroidChart) that auto-updates with new sensor data. Supports historical data viewing up to 5 hours.
- **Dataset Export:** A dedicated section utilizing the Material 3 `DateRangePicker` to trigger background exports of historical sensor readings and verifications.

### 3. Rain Analysis Bottom Sheet
- **Scientific Forecasting:** Calculates local rain probability based on live or last-available sensor data.
- **Ground Truth Verification:** Includes a feedback loop where users can confirm the actual weather outcome ("Yes", "No", "Cloudy"). This data is saved to a dedicated table to build a labeled dataset for future model refinement.
- **Metrics:** Displays Dew Point, Temperature Spread, and multi-stage Pressure Trends (30m to 3h).

## Security & Configuration
- **API Key Handling:** The WeatherAPI key is stored in `local.properties` and accessed via `BuildConfig`.
- **Permissions:** Dynamically handles Bluetooth (Scan/Connect) and Notification permissions (required for WorkManager feedback on API 33+).
- **Offline Mode:** The app automatically caches the last successful weather fetch in the Room database.

## Build Requirements
- **Android Studio:** Quail (2024.1.1) or newer recommended.
- **Min SDK:** 26 (Android 8.0).
- **Target SDK:** 34.
- **Compose BOM:** 2025.02.00 (Stable).
