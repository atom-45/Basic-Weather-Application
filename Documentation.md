# Project Documentation: Basic Weather App

## Overview
The Basic Weather App is a comprehensive weather monitoring and atmospheric analysis solution that integrates global weather forecasts from WeatherAPI.com with local sensor telemetry and offline circular buffer backfilling via Bluetooth Low Energy (BLE). The application uses a **Single Activity** architecture with **Jetpack Compose** and **Room Database**.

---

## System Architecture

```
+-----------------------------------------------------------------------------------+
|                                  UI Layer                                         |
|                                                                                   |
|   MainActivity.kt  <--->  MainScreen.kt  <--->  SensorDisplayScreen.kt            |
|        |                                              |                           |
|        v                                              v                           |
|  RainPredictionBottomSheet                 Sync Progress Banner (Compose)         |
+-----------------------------------------------------------------------------------+
                                        |
                                        v
+-----------------------------------------------------------------------------------+
|                             ViewModel & State Layer                               |
|                                                                                   |
|   WeatherViewModel     UserViewModel     PlaceViewModel     SensorDataViewModel   |
|         |                     |                |                      |           |
|         v                     v                v                      v           |
|  Retrofit (API)         Room DAO          Room DAO            Physics Engine      |
+-----------------------------------------------------------------------------------+
                                        |
                                        v
+-----------------------------------------------------------------------------------+
|                              BLE & Service Layer                                  |
|                                                                                   |
|                         WeatherBLEService.java                                    |
|   - Foreground Service Notification with Progress Indicator                       |
|   - 2-Stage Handshake Protocol (0x01 SYNC / 0x02 ACK)                            |
|   - Sequential CCCD Notification Chaining (0x2902)                                |
|   - Relative Tick Back-Projection & Local Timezone Conversion                     |
|   - Broadcast Actions (ACTION_SYNC_STARTED / ACTION_SYNC_COMPLETED)               |
+-----------------------------------------------------------------------------------+
                                        |
                                        v
+-----------------------------------------------------------------------------------+
|                              Database Layer (Room)                                |
|                                                                                   |
|   WeatherDatabase (Version 9)                                                     |
|   - users, locations, places, sensor_data, cached_forecast,                       |
|     prediction_verifications, thermodynamic_predictions                           |
+-----------------------------------------------------------------------------------+
```

---

## Database Schema & Migrations (Room Version 9)

Database: `WeatherDatabase.java` (`version = 9`, `exportSchema = false`)

### Entities
1.  **`users`**: `userID` (PK), `name_and_surname`.
2.  **`locations`**: `locationID` (PK), `name`, `region`, `country`, `lat`, `lon`, `localtime`, `tz_id`.
3.  **`places`**: `placeID` (PK), `name`, `region`, `country`, `lat`, `lon`, `url`.
4.  **`sensor_data`**: `id` (PK), `date` (Unique Index), `humidity`, `temperature`, `pressure`, `altitude`.
5.  **`cached_forecast`**: `id` (PK, default `"last_forecast"`), `json_data`.
6.  **`prediction_verifications`**: `id` (PK), `timestamp`, `temperature`, `humidity`, `pressure`, `prediction`, `actual_outcome`, `residual`, `source`.
7.  **`thermodynamic_predictions`**: `id` (PK), `original_timestamp`, `temp_snapshot`, `humidity_snapshot`, `pressure_snapshot`, `predicted_arrival`, `storm_speed`, `residual`, `window_start_millis`, `window_end_millis`, `verification_status`, `storm_event_id`, `actual_outcome`, `verified_timestamp`, `notified_45m`.

### Migration History
*   **`MIGRATION_1_2`**: Created `sensor_data` table.
*   **`MIGRATION_2_3`**: Created `cached_forecast` table.
*   **`MIGRATION_3_4`**: Created `prediction_verifications` table.
*   **`MIGRATION_4_5`**: Added `residual` and `source` columns to `prediction_verifications`.
*   **`MIGRATION_5_6`**: Created `thermodynamic_predictions` table.
*   **`MIGRATION_6_7`**: Added `verified_timestamp` column to `thermodynamic_predictions`.
*   **`MIGRATION_7_8`**: Added `notified_45m` column (`INTEGER NOT NULL DEFAULT 0`) to `thermodynamic_predictions`.
*   **`MIGRATION_8_9`**: Added unique index `index_sensor_data_date` on `sensor_data(date)`.
*   **Fallback**: Configured `.fallbackToDestructiveMigration()` as safety fallback.

---

## BLE Protocol & Offline History Sync (`WeatherBLEService.java`)

### 1. Arduino 20-Byte Payload Layout (Little Endian)
The Arduino RP2040 packs sensor telemetry into a 20-byte array:
*   **Bytes 0–3**: `uint32_t sampleMillis` (relative timer ticks in millis)
*   **Bytes 4–7**: `float humidity` (%)
*   **Bytes 8–11**: `float temperature` (°C)
*   **Bytes 12–15**: `float pressure` (hPa)
*   **Bytes 16–19**: `float altitude` (m)

`parseHistoricalRecord()` and `getArraySensorData()` unpack all 5 metrics at exact byte offsets using `ByteBuffer.order(ByteOrder.LITTLE_ENDIAN)`.

### 2. Sequential CCCD Notification Enablement
To avoid Android BLE GATT operation collisions, notification enabling is chained sequentially:
1.  In `onServicesDiscovered()`, notifications are enabled for `liveChar` (`RP2040_CHARACTERISTICS_UUID`).
2.  `setCharacteristicNotification()` writes `ENABLE_NOTIFICATION_VALUE` (`0x0001`) to descriptor `0x2902`.
3.  When `onDescriptorWrite()` confirms success for `liveChar`, it triggers notification enablement for `historyChar` (`RP2040_HISTORY_CHARACTERISTIC_UUID`).
4.  Once `onDescriptorWrite()` confirms success for `historyChar`, `checkAndTriggerHistoricalSync()` executes.

### 3. Handshake Protocol (`0x01` SYNC / `0x02` ACK)
1.  **Sync Request (`0x01`)**: App writes `0x01` to `syncControlChar` via `WRITE_TYPE_NO_RESPONSE`. App broadcasts `ACTION_SYNC_STARTED` and updates foreground notification to *"Syncing Sensor History - Downloading offline readings..."* with a progress bar.
2.  **Stream & Accumulation**: Arduino dumps its 144-record circular buffer over `historyChar`. App collects packets in `historyAccumulator`.
3.  **Relative Tick Back-Projection & Local Timezone Conversion**:
    - Calculates `maxTick = max(r.sampleMillis())` across the batch.
    - Calculates `deltaMillis = maxTick - record.sampleMillis()`.
    - Calculates `sampleUtcMillis = phoneUtcMillis - deltaMillis`.
    - Converts `sampleUtcMillis` to **Local Timezone** (`ZoneId.systemDefault()`) preserving millisecond precision:
      ```java
      String isoDate = LocalDateTime.ofInstant(
          Instant.ofEpochMilli(sampleUtcMillis),
          ZoneId.systemDefault()
      ).toString();
      ```
4.  **Chronological Sorting & Batch Insertion**:
    - Sorts `batchToInsert` in strict ascending chronological order (`oldest -> newest`).
    - Backfills all new records into Room DB in a single `insertAll()` transaction.
5.  **ACK (`0x02`)**: App transmits `0x02` ACK to Arduino via `WRITE_TYPE_NO_RESPONSE`, broadcasts `ACTION_SYNC_COMPLETED`, and restores the foreground notification to *"Receiving live data..."*. Arduino receives `0x02` and resets `totalStored = 0; head = 0;`.

---

## Thermodynamic Physics Engine (`physics/`)

### Core Formula
Solves the First Law of Thermodynamics for open air parcels:
$$\frac{dT}{dt} = \frac{1}{\rho c_p} \frac{dp}{dt} + \frac{J}{c_p}$$

### Components
1.  **`DensityCalculator`**: Computes air density $\rho$ in real time using Virtual Temperature correction ($T_v = T \cdot (1 + 0.378 \cdot e/p)$) for relative humidity.
2.  **`DewPointCalculator`**: Calculates saturation threshold using Magnus-Tetens approximation ($a = 17.27, b = 237.7$).
3.  **`RainAnalysisEngine`**: Centralized thresholding for temperature-dewpoint spread ($T - T_d$):
    *   $\text{Spread} < 4.0^\circ\text{C}$: Heavy Rain Imminent / Currently Raining.
    *   $\text{Spread} < 5.5^\circ\text{C}$: Rain Expected Soon.
    *   $\text{Spread} < 10.0^\circ\text{C}$: Unlikely To Rain.
    *   $\text{Else}$: Very Dry - No Rain.
4.  **`StormThermodynamicsEngine`**:
    *   Calculates numerical derivatives using exact timestamp deltas:
        $$dt = \text{ChronoUnit.SECONDS.between}(t_1, t_2)$$
    *   Calculates Thermodynamic Residual ($\text{residual} = \text{dT/dt}_{\text{actual}} - \text{dT/dt}_{\text{theoretical}}$).
    *   Computes storm speed and projects rain arrival timestamps via Forward Euler extrapolation to the Dew Point.

---

## UI Components & Sync Indicators

### 1. Sensor Display Screen (`SensorDisplayScreen.kt`)
*   **Bento Grid**: Real-time display for Temperature, Humidity, Pressure, and Altitude.
*   **Sync Progress Banner**: Animated `AnimatedVisibility` surface containing `CircularProgressIndicator` and `LinearProgressIndicator` (*"Syncing sensor history..."*) displayed dynamically during BLE history transfer.
*   **Interactive MPAndroidChart**: Dynamically renders trend lines for Temperature, Humidity, or Pressure over 10m to 300m time windows.
*   **Live Model Audit Card**: Prompts for user verification during a prediction's active arrival window.

### 2. Main Dashboard (`MainScreen.kt`)
*   **Search Bar**: Docked search with swipe-to-delete location history.
*   **Weather Grid**: Global forecast metrics from Retrofit 2.
*   **Offline Indicator**: Subtle banner displayed when operating on cached data.

### 3. Rain Prediction Bottom Sheet (`MainActivity.kt`)
*   **Unified Saturation Analysis**: Utilizes `DewPointCalculator` and `RainAnalysisEngine` for real-time spread analysis and user feedback submission.

---

## Dataset Exporting (`ExportWorker.kt`)

Powered by **Android WorkManager**. Generates a ZIP file in `Downloads` containing:
1.  `thermodynamic_blackbox.csv`: Prediction snapshots, residual values, arrival windows, verification statuses, and phase errors ($\Delta t$).
2.  `labeled_verifications.csv`: Tier 1 real-time user feedback entries.
3.  `raw_sensor_data.csv`: High-frequency sensor telemetry log.

---

## Build & Test Commands
*   **Build Assembly**: `./gradlew :app:assembleDebug`
*   **Unit Tests**: `./gradlew :app:testDebugUnitTest`
