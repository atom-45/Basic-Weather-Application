# Basic Weather Application

A modernized, high-performance Android weather dashboard that blends global meteorological data from [WeatherAPI.com](https://www.weatherapi.com/) with real-time local sensor data via Bluetooth Low Energy (BLE).

## 🌟 Modern UI/UX

The application features a modern visual design using **Jetpack Compose** and **Material 3**:
*   **Glassmorphism Design:** Semi-transparent surfaces with subtle borders and dynamic gradients.
*   **Hero Dashboard:** Focal point for live local temperature and global weather conditions.
*   **Bento Box Layouts:** Clean, organized grids for sensor telemetry ($T, H, P, \text{Altitude}$) and meteorological metrics.
*   **Safety & Analytics:** Integrated thermodynamic safety banners, dynamic "Live Model Audit" cards, and de-duplicated verification options.
*   **Single-Dashboard Flow:** Integrated search and user profile management directly on the main screen.

## 🚀 Thermodynamic Analysis Engine

The core of the application features a custom physics engine that applies the **First Law of Thermodynamics** to local air parcels:
*   **Differential Equation Solver:** Solves the internal energy balance equation:
    $$dT/dt = \frac{1}{\rho c_p} \frac{dp}{dt} + \frac{J}{c_p}$$
*   **Real-Time Storm Tracking:** Monitors the "Thermodynamic Residual" (Actual vs. Theoretical cooling) to detect atmospheric instability before rain even starts.
*   **Dynamic Density Calculation:** Computes air density ($\rho$) in real time using Virtual Temperature to account for humidity variations.
*   **Forward Euler Projection:** Projects the exact arrival timestamp of rain by extrapolating when the cooling parcel will intersect the Dew Point.
*   **Black Box Audit System:** Logs prediction snapshots ($T, H, P$, Residual, Predicted Arrival) into Room database table `thermodynamic_predictions`.
*   **Merged Hybrid Verification Model:**
    *   **Targeted Window Back-Propagation:** Verifying a Live Audit card updates pending buildup snapshots matching the active arrival window with a **5-minute tolerance window** ($\pm 5\text{ mins}$ around native 10-minute window = 20-minute total span).
    *   **Long-Horizon Forecast Isolation:** Predictions targeting 3–4+ hours in the future are completely isolated and remain `PENDING` until their target arrival window occurs.
    *   **Immutable Verifications:** Once a prediction snapshot is marked `VERIFIED`, it can **never** be overwritten or altered by subsequent audits.
    *   **Phase Error Tracking ($\Delta t$):** Captures $T_{\text{verify}}$ (exact ISO timestamp when user tapped verification) alongside $T_{\text{predicted\_arrival}}$, enabling exact phase error ($\Delta t = T_{\text{verify}} - T_{\text{predicted}}$) analysis in Python.
    *   **Human Catchment Window:** Accommodates 10-minute BLE sensor update cycles and real-world human response times without requiring exact-second button presses.

## 🛠 Features

*   **Global Forecasts:** Real-time, hourly, and 3-day forecasts for any city worldwide via Retrofit 2.
*   **Precision BLE Integration:** Optimized for **Arduino Nano RP2040 Connect** (BME680). The app features an intelligent target-device filter to automatically connect to your local station.
*   **Multi-Stage Proactive Notifications:**
    *   **45-Minute Scheduled Countdown Alert:** Background service monitors pending forecasts created hours ago and dispatches a high-priority heads-up warning when a storm is **45 minutes away** (*"Storm predicted at 19:45 is expected in ~45 minutes. Prepare for rain."*). Dispatches **exactly once** using a `notified_45m` database flag.
    *   **Imminent Sensor Warning:** Triggers immediate alerts when live sensor readings detect rain arriving within 45 minutes or rapid pressure drops ($> 1\text{ hPa/hr}$).
*   **Multi-Category Ground Truth System:** User feedback options (**Heavy Rain, Light Rain, Cloudy, Windy, Lightning, Clear**).
*   **Advanced Dataset Exporting:** Powered by **Android WorkManager**. Exports a compressed ZIP containing:
    1.  `thermodynamic_blackbox.csv`: Contains `Timestamp`, `Temp`, `Humidity`, `Pressure`, `PredictedArrival`, `VerifiedTimestamp`, `Speed`, `Residual`, `EventID`, `Status`, `ActualOutcome`.
    2.  `labeled_verifications.csv`: Tier 1 real-time feedback logs.
    3.  `raw_sensor_data.csv`: High-frequency sensor telemetry.
*   **Integrated Search & Recents:** Instant location switching with a modern search bar and swipe-to-delete management.
*   **Offline Support:** Automatic Room caching of global weather and sensor history.

## 🏗 Architecture & Tech Stack

*   **UI Layer:** 100% Jetpack Compose with a Single-Activity architecture (`MainActivity.kt`).
*   **Reactive Lifecycle Management:** RxJava 3 bridged to Compose via `subscribeAsState()` and lifecycle-aware `LaunchedEffect` routines.
*   **Persistence:** Room Database (**Version 8**) storing user data, saved locations, weather caching, and the comprehensive Thermodynamic Black Box.
    *   `MIGRATION_6_7`: Added `verified_timestamp` column.
    *   `MIGRATION_7_8`: Added `notified_45m` column (`INTEGER NOT NULL DEFAULT 0`).
*   **Background Processing:** **WorkManager** for memory-safe dataset exports and `WeatherBLEService` for continuous BLE telemetry and notifications.
*   **Dependency Injection:** Dagger 2 for component management.

## 🚀 Getting Started

### Prerequisites
1.  Obtain a free API Key from [WeatherAPI.com](https://www.weatherapi.com/signup.aspx).
2.  Add your key to the `local.properties` file in the root directory:
    ```properties
    WEATHER_API_KEY="your_api_key_here"
    ```
3.  Ensure your Arduino Nano RP2040 Connect is advertising the name `"RP2040 Connect"`.

### Building
Clone the repository and import it into the latest version of **Android Studio (Quail or newer)**.

---
*Developed with a focus on atmospheric physics, performance, and modern design.*
