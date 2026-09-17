# Basic Weather Application

A modernized, high-performance Android weather dashboard that blends global meteorological data from [WeatherAPI.com](https://www.weatherapi.com/) with real-time local sensor data via Bluetooth Low Energy (BLE).

## 🌟 Modern UI/UX

The application has undergone a complete visual transformation using **Jetpack Compose** and **Material 3**, featuring:
*   **Glassmorphism Design:** Sophisticated semi-transparent surfaces with subtle borders and dynamic gradients.
*   **Hero Dashboard:** A dramatic focal point for temperature and primary weather conditions.
*   **Bento Box Layouts:** Clean, organized grids for weather details and sensor readings.
*   **Safety & Analytics:** Integrated thermodynamic safety banners, "Live Model Audit" cards, and horizontally scrollable verification chips.
*   **Single-Dashboard Flow:** Integrated search and user profile management directly on the main screen.

## 🚀 Thermodynamic Analysis Engine (NEW)

The core of the application now features a custom physics engine that applies the **First Law of Thermodynamics** to your local environment.
*   **Scientific Differential Equation:** The engine solves for the internal energy balance:
    $$dT/dt = \frac{1}{\rho c_p} \frac{dp}{dt} + \frac{J}{c_p}$$
*   **Real-Time Storm Tracking:** By monitoring the "Thermodynamic Residual" (Actual vs. Predicted cooling), the app detects atmospheric instability before rain even starts.
*   **Dynamic Density:** Calculates air density ($\rho$) in real-time using Virtual Temperature to account for humidity variations.
*   **Forward Projection:** Uses the Forward Euler method to predict the exact timestamp of rain arrival by projecting when the air parcel will hit the Dew Point.
*   **Black Box Audit System:** A high-fidelity logging system that records every prediction snapshot (Temp, Humidity, Pressure, Residual) and asks for user verification within a 10-minute arrival window.
*   **Event Grouping & Back-Propagation:** Automatically groups sequential storm build-up predictions into "Events." Verifying one prediction applies the ground truth to the entire sequence for superior Python trend analysis.
*   **Stationary Precision:** Optimized for stationary BLE sensors, calculating storm approach speed based on localized pressure tendencies ($dp/dt$).

## 🛠 Features

*   **Global Forecasts:** Real-time, hourly, and 3-day forecasts for any city worldwide.
*   **Precision BLE Integration:** Optimized for **Arduino Nano RP2040 Connect** (BME680). The app features an intelligent target-device filter to automatically connect to your local station.
*   **Proactive Weather Alerts:** A dedicated high-priority notification channel that combines thermodynamic energy balance with dew point analysis to warn you of approaching storms even when the app is in the background.
*   **Expanded Verification Loop:** A multi-category ground-truth system (**Heavy Rain, Light Rain, Cloudy, Windy, Lightning, Clear**). Verifications now capture the **Thermodynamic Residual** and Phase Error data.
*   **Advanced Dataset Exporting:** Professional data extraction tools using **Android WorkManager**. Export a compressed ZIP containing `raw_sensor_data.csv`, `labeled_verifications.csv`, and the new `thermodynamic_blackbox.csv` for direct Python research.
*   **Integrated Search & Recents:** Instant location switching with a modern search bar and swipe-to-delete management for saved places.
*   **Offline Support:** Automatic caching of the last known weather data for seamless offline use.

## 🏗 Architecture & Tech Stack

The project follows modern Android development practices with a clean separation of concerns:
*   **UI Layer:** 100% Jetpack Compose with a Single-Activity architecture.
*   **Reactive Lifecycle Management:** Migrated from manual disposables to idiomatic Compose `subscribeAsState()` and `LaunchedEffect` patterns, ensuring leak-proof data streaming.
*   **Logic Layer:** MVVM architecture with specialized **Physics Utility Packages** for atmospheric calculations.
*   **Background Processing:** **WorkManager** for reliable, memory-safe historical data exports.
*   **Concurrency:** RxJava 3 for core business logic and BLE streams, bridged to Compose.
*   **Networking:** Retrofit 2 & OkHttp for resilient API communication.
*   **Persistence:** Room Database (Version 6) storing user data, saved locations, weather caching, and the comprehensive **Thermodynamic Black Box** history.
*   **Dependency Injection:** Dagger 2 for robust component management.

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
*Developed with a focus on precision, performance, and modern design.*
