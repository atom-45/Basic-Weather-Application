# Basic Weather Application

A modernized, high-performance Android weather dashboard that blends global meteorological data from [WeatherAPI.com](https://www.weatherapi.com/) with real-time local sensor data via Bluetooth Low Energy (BLE).

## 🌟 Modern UI/UX

The application has undergone a complete visual transformation using **Jetpack Compose** and **Material 3**, featuring:
*   **Glassmorphism Design:** Sophisticated semi-transparent surfaces with subtle borders and dynamic gradients.
*   **Hero Dashboard:** A dramatic focal point for temperature and primary weather conditions.
*   **Bento Box Layouts:** Clean, organized grids for weather details and sensor readings.
*   **Single-Dashboard Flow:** Integrated search and user profile management directly on the main screen, eliminating unnecessary navigation.

## 🛠 Features

*   **Global Forecasts:** Real-time, hourly, and 3-day forecasts for any city worldwide.
*   **BLE Sensor Integration:** Direct communication with local sensors (e.g., RP2040/BME680) to display precise Temperature, Humidity, Pressure, and Altitude.
*   **Rain Analysis Dashboard:** Localized rain probability forecasting based on sensor trends, dew point calculation, and pressure shifts.
*   **Integrated Search & Recents:** Instant location switching with a modern search bar and swipe-to-delete management for saved places.
*   **Offline Support:** Automatic caching of the last known weather data for seamless offline use.
*   **Dynamic Previews:** Full support for Compose Previews across all screens for rapid UI development.

## 🏗 Architecture & Tech Stack

The project follows modern Android development practices with a clean separation of concerns:
*   **UI Layer:** 100% Jetpack Compose with a Single-Activity architecture.
*   **Logic Layer:** MVVM (Model-View-ViewModel) architecture.
*   **Concurrency:** RxJava 3 for core business logic and BLE streams, bridged to Compose via `runtime-rxjava3`.
*   **Networking:** Retrofit 2 & OkHttp for resilient API communication.
*   **Persistence:** Room Database for local user data, saved locations, and weather caching.
*   **Dependency Injection:** Dagger 2 for robust component management.
*   **Image Loading:** Coil for efficient weather icon processing.

## 🚀 Getting Started

### Prerequisites
1.  Obtain a free API Key from [WeatherAPI.com](https://www.weatherapi.com/signup.aspx).
2.  Add your key to the `local.properties` file in the root directory:
    ```properties
    WEATHER_API_KEY="your_api_key_here"
    ```

### Building
Clone the repository and import it into the latest version of **Android Studio (Quail or newer)**.

---
*Developed with a focus on precision, performance, and modern design.*
