# Basic Weather Application

A modernized, high-performance Android weather dashboard that blends global meteorological data from [WeatherAPI.com](https://www.weatherapi.com/) with real-time local sensor data via Bluetooth Low Energy (BLE).

## 🌟 Modern UI/UX

The application has undergone a complete visual transformation using **Jetpack Compose** and **Material 3**, featuring:
*   **Glassmorphism Design:** Sophisticated semi-transparent surfaces with subtle borders and dynamic gradients.
*   **Hero Dashboard:** A dramatic focal point for temperature and primary weather conditions.
*   **Bento Box Layouts:** Clean, organized grids for weather details and sensor readings.
*   **Refined Sensor Cards:** Improved vertical alignment and dynamic font scaling for precise readability across different card sizes.
*   **Single-Dashboard Flow:** Integrated search and user profile management directly on the main screen.

## 🛠 Features

*   **Global Forecasts:** Real-time, hourly, and 3-day forecasts for any city worldwide.
*   **Precision BLE Integration:** Optimized for **Arduino Nano RP2040 Connect** (BME680). The app features an intelligent target-device filter to automatically connect to your local station while ignoring interference from other Bluetooth devices.
*   **Rain Analysis & Ground Truth:** Localized rain probability forecasting based on sensor trends and dew point calculation. Now includes a **Verification Loop** allowing you to confirm actual outcomes, building a labeled dataset for future model refinement.
*   **Dataset Exporting:** Professional data extraction tools using **Android WorkManager**. Export historical sensor data and manual verifications as a compressed ZIP containing formatted CSV files directly to your Downloads folder.
*   **Integrated Search & Recents:** Instant location switching with a modern search bar and swipe-to-delete management for saved places.
*   **Offline Support:** Automatic caching of the last known weather data for seamless offline use.

## 🏗 Architecture & Tech Stack

The project follows modern Android development practices with a clean separation of concerns:
*   **UI Layer:** 100% Jetpack Compose with a Single-Activity architecture.
*   **Logic Layer:** MVVM (Model-View-ViewModel) architecture.
*   **Background Processing:** **WorkManager** for reliable, memory-safe historical data exports.
*   **Concurrency:** RxJava 3 for core business logic and BLE streams, bridged to Compose via `runtime-rxjava3`.
*   **Networking:** Retrofit 2 & OkHttp for resilient API communication.
*   **Persistence:** Room Database (Version 4) for local user data, saved locations, weather caching, and labeled prediction verifications.
*   **Dependency Injection:** Dagger 2 for robust component management.
*   **Image Loading:** Coil for efficient weather icon processing.

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
