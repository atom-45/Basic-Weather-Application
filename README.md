# Basic Weather Application

A modernized, high-performance Android weather dashboard that blends global meteorological data from [WeatherAPI.com](https://www.weatherapi.com/) with real-time local sensor data and offline circular buffer backfilling via Bluetooth Low Energy (BLE).

## 🌟 Modern UI/UX

The application features a modern visual design using **Jetpack Compose** and **Material 3**:
*   **Glassmorphism Design:** Semi-transparent surfaces with subtle borders and dynamic gradients.
*   **Hero Dashboard:** Focal point for live local temperature and global weather conditions.
*   **Bento Box Layouts:** Clean, organized grids for sensor telemetry ($T, H, P, \text{Altitude}$) and meteorological metrics.
*   **Safety & Analytics:** Integrated thermodynamic safety banners, dynamic "Live Model Audit" cards, and de-duplicated verification options.
*   **Sync Progress Banner:** Real-time animated Jetpack Compose progress indicator (`LinearProgressIndicator` and `CircularProgressIndicator`) that slides into view during offline sensor synchronization.
*   **Single-Dashboard Flow:** Integrated search and user profile management directly on the main screen.

## 🚀 Thermodynamic Analysis Engine & Physics Package

The core of the application features a custom physics engine that applies the **First Law of Thermodynamics** to local air parcels:
*   **Differential Equation Solver:** Solves the internal energy balance equation:
    $$dT/dt = \frac{1}{\rho c_p} \frac{dp}{dt} + \frac{J}{c_p}$$
*   **Real-Time Storm Tracking:** Monitors the "Thermodynamic Residual" (Actual vs. Theoretical cooling) to detect atmospheric instability before rain even starts.
*   **Dynamic Density Calculation:** Computes air density ($\rho$) in real time using Virtual Temperature correction (`DensityCalculator`) to account for humidity variations.
*   **Centralized Saturation Analysis:** Uses `DewPointCalculator` (Magnus-Tetens approximation) and `RainAnalysisEngine` for unified temperature-dewpoint spread thresholding ($T - T_d$) across the app.
*   **Variable-Interval Differentiation:** Uses exact timestamp deltas ($dt = \text{SECONDS.between}(t_1, t_2)$) to ensure thermodynamic equations remain 100% mathematically accurate regardless of 5-minute or 10-minute sampling variations.
*   **Forward Euler Projection:** Projects the exact arrival timestamp of rain by extrapolating when the cooling parcel will intersect the Dew Point.
*   **Black Box Audit System:** Logs prediction snapshots ($T, H, P$, Residual, Predicted Arrival) into Room database table `thermodynamic_predictions`.
*   **Merged Hybrid Verification Model:**
    *   **Targeted Window Back-Propagation:** Verifying a Live Audit card updates pending buildup snapshots matching the active arrival window with a **5-minute tolerance window** ($\pm 5\text{ mins}$ around native 10-minute window = 20-minute total span).
    *   **Long-Horizon Forecast Isolation:** Predictions targeting 3–4+ hours in the future are completely isolated and remain `PENDING` until their target arrival window occurs.
    *   **Immutable Verifications:** Once a prediction snapshot is marked `VERIFIED`, it can **never** be overwritten or altered by subsequent audits.
    *   **Phase Error Tracking ($\Delta t$):** Captures $T_{\text{verify}}$ (exact ISO timestamp when user tapped verification) alongside $T_{\text{predicted\_arrival}}$, enabling exact phase error ($\Delta t = T_{\text{verify}} - T_{\text{predicted}}$) analysis in Python.

## 📡 Precision BLE & History Sync Protocol

Optimized for **Arduino Nano RP2040 Connect** with a BME680 sensor:
*   **20-Byte Payload Structure (Little Endian):**
    *   **Bytes 0–3:** `uint32_t sampleMillis` (relative time in milliseconds)
    *   **Bytes 4–7:** `float humidity` (%)
    *   **Bytes 8–11:** `float temperature` (°C)
    *   **Bytes 12–15:** `float pressure` (hPa)
    *   **Bytes 16–19:** `float altitude` (m)
*   **2-Stage Handshake Protocol (`0x01` SYNC / `0x02` ACK):**
    1.  App triggers sync by writing `0x01` (SYNC) to `syncControlChar` via `WRITE_TYPE_NO_RESPONSE`.
    2.  Arduino streams its 144-sample circular buffer (`2.88 KB`) over `historyCharacteristic`.
    3.  App accumulates packets, applies **Relative Tick Back-Projection** ($\text{delta} = \text{maxTick} - t_{\text{sample}}$), converts timestamps to local timezone (`ZoneId.systemDefault()`) with millisecond precision, sorts them chronologically, and backfills into Room DB in a single transaction.
    4.  App transmits `0x02` (ACK) to Arduino via `WRITE_TYPE_NO_RESPONSE`, prompting the Arduino to clear its circular buffer (`totalStored = 0; head = 0;`).
*   **Sequential CCCD Notification Chaining:** Enables notifications on `liveChar` first, and uses `onDescriptorWrite()` to sequentially enable notifications on `historyChar` without GATT write collisions.
*   **Multi-Stage System Notifications:**
    *   **Sync Progress Indicator:** Updates ongoing foreground notification to *"Syncing Sensor History - Downloading offline readings..."* with a progress bar during history backfill.
    *   **45-Minute Scheduled Countdown Alert:** Background service monitors pending forecasts and dispatches a high-priority heads-up warning when a storm is **45 minutes away**.
    *   **Imminent Sensor Warning:** Triggers immediate alerts when live sensor readings detect rain arriving within 45 minutes or rapid pressure drops ($> 1\text{ hPa/hr}$).

## 🏗 Architecture & Tech Stack

*   **UI Layer:** 100% Jetpack Compose with a Single-Activity architecture (`MainActivity.kt`).
*   **Reactive Lifecycle Management:** RxJava 3 bridged to Compose via `subscribeAsState()` and lifecycle-aware `LaunchedEffect` routines with complete error callbacks (`onError`).
*   **Persistence:** Room Database (**Version 9**) storing user profiles, saved locations, weather caching, sensor data, and the Thermodynamic Black Box.
    *   `MIGRATION_6_7`: Added `verified_timestamp` column.
    *   `MIGRATION_7_8`: Added `notified_45m` column (`INTEGER NOT NULL DEFAULT 0`).
    *   `MIGRATION_8_9`: Added unique index `index_sensor_data_date` on `sensor_data(date)`.
*   **Background Processing:** **WorkManager** for dataset exports and `WeatherBLEService` for continuous BLE telemetry and notifications.
*   **Dependency Injection:** Dagger 2 for component management.

## 🛠 Features & Dataset Exporting

*   **Global Forecasts:** Real-time, hourly, and 3-day forecasts for any city worldwide via Retrofit 2.
*   **Multi-Category Ground Truth System:** User feedback options (**Heavy Rain, Light Rain, Cloudy, Windy, Lightning, Clear**).
*   **Advanced Dataset Exporting:** Powered by **Android WorkManager**. Exports a compressed ZIP containing:
    1.  `thermodynamic_blackbox.csv`: Contains `Timestamp`, `Temp`, `Humidity`, `Pressure`, `PredictedArrival`, `VerifiedTimestamp`, `Speed`, `Residual`, `EventID`, `Status`, `ActualOutcome`.
    2.  `labeled_verifications.csv`: Tier 1 real-time feedback logs.
    3.  `raw_sensor_data.csv`: High-frequency sensor telemetry.

## 🚀 Getting Started

### Prerequisites
1.  Obtain a free API Key from [WeatherAPI.com](https://www.weatherapi.com/signup.aspx).
2.  Add your key to `local.properties` in the root directory:
    ```properties
    WEATHER_API_KEY="your_api_key_here"
    ```
3.  Ensure your Arduino Nano RP2040 Connect is flashed with the BME680 BLE sketch.

### Building
Clone the repository and import it into the latest version of **Android Studio (Quail or newer)**. Run `:app:assembleDebug` or `:app:testDebugUnitTest`.

---
*Developed with a focus on atmospheric physics, performance, and modern design.*
