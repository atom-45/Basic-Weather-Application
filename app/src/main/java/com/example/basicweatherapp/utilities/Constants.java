package com.example.basicweatherapp.utilities;

import com.example.basicweatherapp.BuildConfig;

import java.util.List;

public class Constants {

    public static String API_KEY = BuildConfig.WEATHER_API_KEY;
    public static String PLACES_LIST = "PLACES_LIST";
    public static String LOCATION = "LOCATION";

    public final static String RP2040_SERVICE_UUID = BuildConfig.BLE_SERVICE_UUID;
    public final static String RP2040_CHARACTERISTICS_UUID = BuildConfig.BLE_LIVE_CHAR_UUID;
    public final static String RP2040_HISTORY_CHARACTERISTIC_UUID = BuildConfig.BLE_HISTORY_CHAR_UUID;
    public final static String RP2040_SYNC_CONTROL_CHARACTERISTIC_UUID = BuildConfig.BLE_SYNC_CHAR_UUID;

    public final static byte SYNC_COMMAND_REQUEST = (byte) 0x01;
    public final static byte SYNC_COMMAND_ACK = (byte) 0x02;

    public final static String DESCRIPTOR_GEN_UUID = "00002902-0000-1000-8000-00805f9b34fb";
    public final static String ARDUINO_DEVICE_NAME = "RP2040 BME 680 Sensor";
}
