package com.example.basicweatherapp.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.basicweatherapp.R;
import com.example.basicweatherapp.data.models.HistoricalSensorRecord;
import com.example.basicweatherapp.data.models.SensorData;
import com.example.basicweatherapp.data.models.ThermodynamicPrediction;
import com.example.basicweatherapp.data.repositories.SensorDataRepository;
import com.example.basicweatherapp.data.repositories.ThermodynamicPredictionRepository;
import com.example.basicweatherapp.di.application.WeatherApplication;
import com.example.basicweatherapp.physics.DewPointCalculator;
import com.example.basicweatherapp.physics.RainAnalysisEngine;
import com.example.basicweatherapp.physics.StormReport;
import com.example.basicweatherapp.physics.StormThermodynamicsEngine;
import com.example.basicweatherapp.presentation.activities.MainActivity;
import com.example.basicweatherapp.utilities.Constants;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.inject.Inject;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@SuppressLint("MissingPermission")
public class WeatherBLEService extends Service {

    public static final String TAG = "WeatherBLEService";

    private static final int NOTIFICATION_ID = 203;
    private static final String CHANNEL_ID = "WeatherBLEServiceChannel";
    private static final String ALERT_CHANNEL_ID = "WeatherAlertChannel";
    private static final int ALERT_NOTIFICATION_ID = 204;
    
    public final static String ACTION_GATT_CONNECTED = "com.atom.bluetoothfitnessapplication.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.atom.bluetoothfitnessapplication.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.atom.bluetoothfitnessapplication.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.atom.bluetoothfitnessapplication.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String ACTION_SYNC_STARTED = "com.atom.bluetoothfitnessapplication.bluetooth.le.ACTION_SYNC_STARTED";
    public final static String ACTION_SYNC_COMPLETED = "com.atom.bluetoothfitnessapplication.bluetooth.le.ACTION_SYNC_COMPLETED";
    public final static String EXTRA_SENSOR_DATA = "com.atom.bluetoothfitnessapplication.bluetooth.le.EXTRA_DATA";

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTED = 2;

    private int connectionState;

    private final Binder binder = new LocalBinder();
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Inject
    SensorDataRepository sensorDataRepository;

    @Inject
    ThermodynamicPredictionRepository thermodynamicPredictionRepository;

    private final List<HistoricalSensorRecord> historyAccumulator = new ArrayList<>();
    private final android.os.Handler historyHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private final Runnable historyFlushRunnable = new Runnable() {
        @Override
        public void run() {
            List<HistoricalSensorRecord> batch;
            synchronized (historyAccumulator) {
                if (historyAccumulator.isEmpty()) return;
                batch = new ArrayList<>(historyAccumulator);
                historyAccumulator.clear();
            }
            processHistoricalSyncBatch(batch);
        }
    };

    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback()
    {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
        {

            if(newState == BluetoothGatt.STATE_CONNECTED)
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(NOTIFICATION_ID, createNotification("Weather Sensor Connected", "Receiving live data from BME680 sensor..."), ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE);
                } else {
                    startForeground(NOTIFICATION_ID, createNotification("Weather Sensor Connected", "Receiving live data from BME680 sensor..."));
                }

                connectionState = STATE_CONNECTED;
                broadcastUpdate(ACTION_GATT_CONNECTED);
                bluetoothGatt.discoverServices();

            } else if(newState == BluetoothGatt.STATE_DISCONNECTED) {
                connectionState = STATE_DISCONNECTED;
                broadcastUpdate(ACTION_GATT_DISCONNECTED);
                stopForeground(true);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status)
        {
            if(status == BluetoothGatt.GATT_SUCCESS)
            {
                BluetoothGattService service = bluetoothGatt
                        .getService(UUID.fromString(Constants.RP2040_SERVICE_UUID));

                if (service != null) {
                    BluetoothGattCharacteristic liveChar = service.getCharacteristic(UUID.fromString(Constants.RP2040_CHARACTERISTICS_UUID));
                    if (liveChar != null) {
                        setCharacteristicNotification(liveChar, true);
                    }
                }

                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);

            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status)
        {
            setCharacteristicNotification(characteristic, true);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "onCharacteristicRead: Able to read the data from the sensor");
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);

            } else {
                Log.d(TAG, "onCharacteristicRead: Unable to read the data from the sensor");
            }
        }


        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic,
                                          int status) {

            if(status == BluetoothGatt.GATT_SUCCESS){
                Log.d(TAG, "onCharacteristicWrite: Data written to a bluetooth peripheral device");
            } else {
                Log.d(TAG, "onCharacteristicWrite: Data not written to the bluetooth device");
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic)
        {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            UUID uuid = characteristic.getUuid();

            if (UUID.fromString(Constants.RP2040_HISTORY_CHARACTERISTIC_UUID).equals(uuid)) {
                byte[] value = characteristic.getValue();
                HistoricalSensorRecord record = parseHistoricalRecord(value);
                if (record != null) {
                    Log.d(TAG, "Received historical packet: t=" + record.sampleMillis() + " T=" + record.temperature() + " H=" + record.humidity() + " P=" + record.pressure() + " A=" + record.altitude());
                    if (!(record.sampleMillis() == 0 && record.temperature() == 0 && record.humidity() == 0)) {
                        synchronized (historyAccumulator) {
                            historyAccumulator.add(record);
                        }
                        historyHandler.removeCallbacks(historyFlushRunnable);
                        historyHandler.postDelayed(historyFlushRunnable, 300);
                    }
                }
                return;
            }

            if (UUID.fromString(Constants.RP2040_CHARACTERISTICS_UUID).equals(uuid)) {
                float[] sensorFloats = getArraySensorData(characteristic);

                SensorData sensorData = new SensorData(
                        LocalDateTime.now().toString(), // current date and time of data collection
                        sensorFloats[0], // humidity in %
                        sensorFloats[1], // degree celsius
                        sensorFloats[2], // hPa
                        sensorFloats[3]); // metres

                if (sensorDataRepository != null) {
                    Log.d(TAG, "onCharacteristicChanged: Sensor Repository is not null");
                    Disposable disposable = sensorDataRepository.insert(sensorData)
                            .doOnComplete(() -> {
                                Log.d(TAG, "SUCCESS: Sensor data inserted into Room database.");
                                performAtmosphericAnalysis();
                            })
                            .doOnError(throwable -> Log.e(TAG, "ERROR: Failed to insert sensor data", throwable))
                            .subscribe(() -> {}, throwable -> Log.e(TAG, "ERROR: Failed to insert sensor data", throwable));
                    compositeDisposable.add(disposable);
                }

                Log.d(TAG, "onCharacteristicChanged: " + sensorData);
                final Intent intent = new Intent(ACTION_DATA_AVAILABLE);
                intent.putExtra(EXTRA_SENSOR_DATA, sensorFloats);
                sendBroadcast(intent);
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt,
                                      BluetoothGattDescriptor descriptor,
                                      int status)
        {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "onDescriptorWrite: Descriptor written successfully for " + descriptor.getCharacteristic().getUuid());
                if (UUID.fromString(Constants.RP2040_CHARACTERISTICS_UUID).equals(descriptor.getCharacteristic().getUuid())) {
                    BluetoothGattService service = gatt.getService(UUID.fromString(Constants.RP2040_SERVICE_UUID));
                    if (service != null) {
                        BluetoothGattCharacteristic historyChar = service.getCharacteristic(UUID.fromString(Constants.RP2040_HISTORY_CHARACTERISTIC_UUID));
                        if (historyChar != null) {
                            setCharacteristicNotification(historyChar, true);
                        }
                    }
                } else if (UUID.fromString(Constants.RP2040_HISTORY_CHARACTERISTIC_UUID).equals(descriptor.getCharacteristic().getUuid())) {
                    Log.d(TAG, "History notifications fully enabled. Now checking gap and triggering historical sync...");
                    checkAndTriggerHistoricalSync();
                }
            } else {
                Log.w(TAG, "onDescriptorWrite failed with status: " + status);
            }
            enableConfiguration(gatt, descriptor.getCharacteristic());

        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        ((WeatherApplication) getApplication()).getApplicationComponent().inject(this);
        //this.sensorDataRepository = new SensorDataRepository((Application) getApplication());
    }

    private void createNotificationChannels()
    {
        NotificationChannel serviceChannel = new NotificationChannel(CHANNEL_ID,
                "Weather Sensor Service", NotificationManager.IMPORTANCE_LOW);
        serviceChannel.setDescription("Keeps the Bluetooth sensor connection active.");

        NotificationChannel alertChannel = new NotificationChannel(ALERT_CHANNEL_ID,
                "Weather Alerts", NotificationManager.IMPORTANCE_HIGH);
        alertChannel.setDescription("Notifications for approaching storms and rain.");
        alertChannel.enableVibration(true);

        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(serviceChannel);
            manager.createNotificationChannel(alertChannel);
        }
    }

    private Notification createNotification(String title, String text)
    {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.sunny)
                .setOngoing(true)
                .build();
    }

    private Notification createSyncNotification(String title, String text, boolean isProgress)
    {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.rain_2)
                .setOngoing(true);
        if (isProgress) {
            builder.setProgress(0, 0, true);
        }
        return builder.build();
    }

    private void performAtmosphericAnalysis() {
        Disposable disposable = sensorDataRepository.getLastTwoEntries()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .take(1)
                .subscribe(list -> {
                    if (list.size() < 2) return;

                    SensorData prev = list.get(1); // DESC order, so index 1 is older
                    SensorData current = list.get(0); // index 0 is newest

                    // 1. Thermodynamic Analysis
                    StormReport thermoReport = StormThermodynamicsEngine.analyze(prev, current);

                    // 2. Static Rain Analysis
                    float dewPoint = (float) DewPointCalculator.calculate(current.getTemperature(), current.getHumidity());
                    boolean rainLikely = RainAnalysisEngine.isLikely(current.getTemperature(), dewPoint);
                    String rainPrediction = RainAnalysisEngine.predict(current.getTemperature(), dewPoint);

                    // Always save to Black Box for trend analysis and verification
                    saveThermodynamicEvent(current, thermoReport);

                    // Check for long-horizon predictions approaching the 45-minute mark
                    checkPending45mAlerts();

                    // 3. Notification Logic
                    double arrivalMins = thermoReport.arrivalTimeMinutes();
                    if (arrivalMins > 0 && arrivalMins <= 45) {
                        // Dedicated 45-Minute Thermodynamic Early Warning
                        String title = "Thermodynamic Warning: Rain Imminent";
                        String content = String.format(Locale.ENGLISH,
                                "Rain projected at %s (~%.0f min). Speed: %.1f km/h.",
                                thermoReport.arrivalTimestamp(), arrivalMins, thermoReport.stormSpeedKmh());
                        sendWeatherAlert(title, content);
                    } else if (!thermoReport.isSafeToWalk() || rainLikely) {
                        String title = "Weather Alert: Unstable Atmosphere";
                        String content = thermoReport.isSafeToWalk() ? 
                                "Rain expected: " + rainPrediction : 
                                thermoReport.statusMessage();
                        sendWeatherAlert(title, content);
                    }
                }, throwable -> Log.e(TAG, "Error during atmospheric analysis", throwable));
        compositeDisposable.add(disposable);
    }

    private void checkPending45mAlerts() {
        long now = System.currentTimeMillis();
        Disposable disposable = Observable.fromCallable(() -> 
                thermodynamicPredictionRepository.getPredictionsNeeding45mAlert(now))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> {
                    if (list != null && !list.isEmpty()) {
                        for (ThermodynamicPrediction p : list) {
                            long arrivalMillis = p.getWindowStartMillis() + (5 * 60 * 1000);
                            double remainingMins = Math.max(0, (arrivalMillis - now) / 60000.0);
                            
                            String title = "Thermodynamic Alert: Upcoming Storm";
                            String content = String.format(Locale.ENGLISH,
                                    "Storm predicted at %s is expected in ~%.0f minutes. Prepare for rain.",
                                    p.getPredictedArrival(), remainingMins);
                            
                            sendWeatherAlert(title, content);
                            
                            // Mark as notified so it fires exactly once
                            compositeDisposable.add(
                                thermodynamicPredictionRepository.mark45mAlertSent(p.getId())
                                    .subscribeOn(Schedulers.io())
                                    .subscribe(() -> {}, err -> Log.e(TAG, "Error marking 45m alert sent", err))
                            );
                        }
                    }
                }, throwable -> Log.e(TAG, "Error checking pending 45m alerts", throwable));
        compositeDisposable.add(disposable);
    }

    /**
     * Relative Tick Back-Projection:
     * Calculates the exact ISO UTC timestamp for a historical sample based on the Arduino's relative tick counter.
     */
    public static String calculateUtcTimestamp(long phoneUtcMillis, long arduinoCurrentMillis, long sampleMillis) {
        long deltaMillis = arduinoCurrentMillis - sampleMillis;
        long sampleUtcMillis = phoneUtcMillis - deltaMillis;
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(sampleUtcMillis), ZoneId.systemDefault()).toString();
    }

    /**
     * Parses a 20-byte historical telemetry payload from Arduino:
     * Bytes 0-3: timestampMillis (uint32_t)
     * Bytes 4-7: humidity (float)
     * Bytes 8-11: temperature (float)
     * Bytes 12-15: pressure (float)
     * Bytes 16-19: altitude (float)
     */
    public static HistoricalSensorRecord parseHistoricalRecord(byte[] value) {
        if (value == null || value.length < 20) return null;
        ByteBuffer buffer = ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN);
        long sampleMillis = buffer.getInt(0) & 0xFFFFFFFFL; // uint32_t unsigned conversion
        float humidity = buffer.getFloat(4);                  // bytes 4-7
        float temp = buffer.getFloat(8);                      // bytes 8-11
        float pressure = buffer.getFloat(12);                 // bytes 12-15
        float altitude = buffer.getFloat(16);                 // bytes 16-19
        return new HistoricalSensorRecord(sampleMillis, temp, humidity, pressure, altitude);
    }

    /**
     * Sends a 1-byte command (0x01 = SYNC, 0x02 = ACK) to the Arduino sync control characteristic.
     */
    public void sendSyncControlCommand(byte command) {
        if (bluetoothGatt == null) {
            Log.w(TAG, "sendSyncControlCommand: bluetoothGatt is null");
            return;
        }
        BluetoothGattService service = bluetoothGatt.getService(UUID.fromString(Constants.RP2040_SERVICE_UUID));
        if (service == null) {
            Log.w(TAG, "sendSyncControlCommand: RP2040_SERVICE_UUID not found");
            return;
        }
        BluetoothGattCharacteristic syncChar = service.getCharacteristic(UUID.fromString(Constants.RP2040_SYNC_CONTROL_CHARACTERISTIC_UUID));
        if (syncChar == null) {
            Log.w(TAG, "sendSyncControlCommand: RP2040_SYNC_CONTROL_CHARACTERISTIC_UUID not found");
            return;
        }

        Log.d(TAG, "Sending sync command: 0x" + Integer.toHexString(command));
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (command == Constants.SYNC_COMMAND_REQUEST) {
            if (manager != null) {
                manager.notify(NOTIFICATION_ID, createSyncNotification("Syncing Sensor History", "Downloading offline readings from sensor...", true));
            }
            broadcastUpdate(ACTION_SYNC_STARTED);
        } else if (command == Constants.SYNC_COMMAND_ACK) {
            if (manager != null) {
                manager.notify(NOTIFICATION_ID, createNotification("Weather Sensor Connected", "Receiving live data from BME680 sensor..."));
            }
            broadcastUpdate(ACTION_SYNC_COMPLETED);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bluetoothGatt.writeCharacteristic(syncChar, new byte[]{command}, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        } else {
            syncChar.setValue(new byte[]{command});
            syncChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            bluetoothGatt.writeCharacteristic(syncChar);
        }
    }

    public static long parseDateToMillis(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return 0;
        }
        try {
            return Instant.parse(dateStr).toEpochMilli();
        } catch (DateTimeParseException e) {
            try {
                return LocalDateTime.parse(dateStr).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            } catch (DateTimeParseException e2) {
                return 0;
            }
        }
    }

    /**
     * Processes a batch of historical telemetry samples received from Arduino buffer,
     * applying Relative Tick Back-Projection and pre-insert deduplication.
     */
    public void processHistoricalSyncBatch(List<HistoricalSensorRecord> records) {
        if (records == null || records.isEmpty() || sensorDataRepository == null) return;

        long phoneUtcMillis = System.currentTimeMillis();

        long maxTick = 0;
        for (HistoricalSensorRecord r : records) {
            if (r.sampleMillis() > maxTick) {
                maxTick = r.sampleMillis();
            }
        }
        final long referenceTick = maxTick;

        Disposable disposable = Observable.fromCallable(() -> sensorDataRepository.getLatestSensorData())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(latestList -> {
                    long latestDatabaseMillis = 0;
                    if (latestList != null && !latestList.isEmpty()) {
                        latestDatabaseMillis = parseDateToMillis(latestList.get(0).getDate());
                    }

                    List<SensorData> batchToInsert = new ArrayList<>();
                    for (HistoricalSensorRecord record : records) {
                        long deltaMillis = Math.max(0, referenceTick - record.sampleMillis());
                        long sampleUtcMillis = phoneUtcMillis - deltaMillis;

                        // Deduplication: Skip samples older than or equal to the latest database entry
                        if (latestDatabaseMillis > 0 && sampleUtcMillis <= latestDatabaseMillis) {
                            continue;
                        }

                        String isoDate = LocalDateTime.ofInstant(
                                Instant.ofEpochMilli(sampleUtcMillis),
                                ZoneId.systemDefault()
                        ).toString();

                        batchToInsert.add(new SensorData(
                                isoDate,
                                record.humidity(),
                                record.temperature(),
                                record.pressure(),
                                record.altitude()
                        ));
                    }

                    // Sort batch in strict ascending chronological order (oldest to newest)
                    batchToInsert.sort((a, b) -> a.getDate().compareTo(b.getDate()));

                    if (!batchToInsert.isEmpty()) {
                        Log.d(TAG, "Backfilling " + batchToInsert.size() + " historical records in strict chronological order via Relative Tick Back-Projection...");
                        compositeDisposable.add(
                            sensorDataRepository.insertAll(batchToInsert)
                                .doOnComplete(() -> {
                                    Log.d(TAG, "SUCCESS: Historical batch (" + batchToInsert.size() + " records) backfilled into Room. Sending ACK (0x02)...");
                                    sendSyncControlCommand(Constants.SYNC_COMMAND_ACK);
                                    performAtmosphericAnalysis();
                                })
                                .doOnError(err -> Log.e(TAG, "Error backfilling historical batch", err))
                                .subscribe(() -> {}, err -> Log.e(TAG, "Error backfilling historical batch", err))
                        );
                    } else {
                        Log.d(TAG, "Historical batch contained no new samples after deduplication. Sending ACK (0x02)...");
                        sendSyncControlCommand(Constants.SYNC_COMMAND_ACK);
                    }
                }, err -> Log.e(TAG, "Error querying latest database entry for historical sync", err));

        compositeDisposable.add(disposable);
    }

    private void checkAndTriggerHistoricalSync() {
        if (sensorDataRepository == null) return;

        Disposable disposable = Observable.fromCallable(() -> sensorDataRepository.getLatestSensorData())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(latestList -> {
                    long now = System.currentTimeMillis();
                    long latestMillis = 0;
                    if (latestList != null && !latestList.isEmpty()) {
                        latestMillis = parseDateToMillis(latestList.get(0).getDate());
                    }

                    long tenMins = 10 * 60 * 1000L;
                    if (latestMillis == 0 || (now - latestMillis) > tenMins) {
                        long gapMins = latestMillis == 0 ? -1 : (now - latestMillis) / 60000;
                        Log.d(TAG, "Gap check passed (gap: " + gapMins + " mins). Writing SYNC request (0x01)...");
                        sendSyncControlCommand(Constants.SYNC_COMMAND_REQUEST);
                    } else {
                        Log.d(TAG, "No history sync needed. Latest entry is recent (" + ((now - latestMillis) / 60000) + " mins ago).");
                    }
                }, err -> Log.e(TAG, "Error checking gap for historical sync", err));

        compositeDisposable.add(disposable);
    }

    private void saveThermodynamicEvent(SensorData current, StormReport report) {
        Log.d(TAG, "saveThermodynamicEvent: Attempting to save prediction...");
        Disposable disposable = thermodynamicPredictionRepository.getLatestPrediction()
                .subscribeOn(Schedulers.io())
                .take(1)
                .subscribe(list -> {
                    String eventId;
                    long now = System.currentTimeMillis();
                    ThermodynamicPrediction latest = (list == null || list.isEmpty()) ? null : list.get(0);
                    
                    // Grouping Logic: 30-minute threshold or same event if pending
                    if (latest != null && latest.getVerificationStatus().equals("PENDING")) {
                         eventId = latest.getStormEventId();
                         Log.d(TAG, "saveThermodynamicEvent: Continuing existing event: " + eventId);
                    } else {
                        eventId = "EVT_" + now;
                        Log.d(TAG, "saveThermodynamicEvent: Starting NEW storm event: " + eventId);
                    }

                    // Window: arrival ± 5 minutes.
                    double minsToArrival = report.arrivalTimeMinutes();
                    long windowStart, windowEnd;
                    
                    if (minsToArrival > 0) {
                        long arrivalMillis = now + (long)(minsToArrival * 60 * 1000);
                        windowStart = arrivalMillis - (5 * 60 * 1000);
                        windowEnd = arrivalMillis + (5 * 60 * 1000);
                    } else {
                        // If no arrival time (stable), we save the prediction for the Black Box
                        // but we set the window in the far future or zero so it doesn't trigger 
                        // the Audit Card immediately.
                        windowStart = 0; 
                        windowEnd = 0;
                    }

                    ThermodynamicPrediction prediction = new ThermodynamicPrediction(
                            current.getDate(),
                            current.getTemperature(),
                            current.getHumidity(),
                            current.getPressure(),
                            report.arrivalTimestamp(),
                            report.stormSpeedKmh(),
                            report.thermodynamicResidual(),
                            windowStart,
                            windowEnd,
                            "PENDING",
                            eventId
                    );

                    compositeDisposable.add(
                        thermodynamicPredictionRepository.insert(prediction)
                            .doOnComplete(() -> Log.d(TAG, "SUCCESS: Black Box prediction saved."))
                            .doOnError(e -> Log.e(TAG, "ERROR: Failed to save Black Box prediction", e))
                            .subscribe(() -> {}, e -> Log.e(TAG, "ERROR: Failed to save Black Box prediction", e))
                    );
                }, throwable -> Log.e(TAG, "Error fetching latest prediction for grouping", throwable));
        compositeDisposable.add(disposable);
    }

    private void sendWeatherAlert(String title, String content) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        // Use direct navigation to the sensor screen if possible, but MainActivity handles routing
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 
                PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, ALERT_CHANNEL_ID)
                .setSmallIcon(R.drawable.rain_2)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "Notification permission missing for alert");
                return;
            }
        }
        notificationManager.notify(ALERT_NOTIFICATION_ID, builder.build());
    }
    
    public boolean initialize()
    {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if(bluetoothAdapter == null)
        {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter." );
            return false;
        }
        return true;
    }

    public boolean connect(final String address)
    {
        if(bluetoothAdapter == null || address == null)
        {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address");
            return false;
        }
        
        try {
            final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
            bluetoothGatt = device.connectGatt(this, false, bluetoothGattCallback);
            return true;
        } catch (IllegalArgumentException e){
            Log.w(TAG, "Device not found with provided address.  Unable to connect.", e);
            return false;
        }
    }
    public List<BluetoothGattService> getSupportedGattServices()
    {
        if(bluetoothGatt == null)
        {
            Log.w(TAG, "BluetoothGatt not initialized");
            return null;
        }
        return bluetoothGatt.getServices();
    }
    
    public void readCharacteristic(BluetoothGattCharacteristic characteristic)
    {
        if(bluetoothGatt == null)
        {
            Log.w(TAG, "readCharacteristic: Bluetooth not initialized.");
            return;
        }
        bluetoothGatt.readCharacteristic(characteristic);
    }

    public void writeCharacteristic(BluetoothGattCharacteristic characteristic)
    {
        if(bluetoothGatt == null)
        {
            Log.w(TAG, "writeCharacteristic: Bluetooth not initialized.");
            return;
        }
        bluetoothGatt.writeCharacteristic(characteristic);
    }
    
    private void broadcastUpdate(final String action)
    {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }
    
    private void broadcastUpdate(final String action, 
                                 final BluetoothGattCharacteristic characteristic)
    {
        final Intent intent = new Intent(action);
        
        if(UUID.fromString(Constants.RP2040_CHARACTERISTICS_UUID)
                .equals(characteristic.getUuid()))
        {
            float[] arraySensorData = getArraySensorData(characteristic);
            intent.putExtra(EXTRA_SENSOR_DATA, arraySensorData);
        }
        
        sendBroadcast(intent);
        
    }
    
    
    @NonNull
    private float[] getArraySensorData(BluetoothGattCharacteristic characteristic)
    {
        byte[] value = characteristic.getValue();

        if (value == null || value.length < 16)
        {
            Log.e(TAG, "getArraySensorData: Received invalid data length: " +
                    (value != null ? value.length : 0));

            return new float[]{0f, 0f, 0f, 0f};
        }

        ByteBuffer buffer = ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN);

        float humidity;
        float temperature;
        float pressure;
        float altitude;

        if (value.length >= 20) {
            long sampleMillis = buffer.getInt(0) & 0xFFFFFFFFL; // Bytes 0-3: t (uint32_t)
            humidity = buffer.getFloat(4);                      // Bytes 4-7: h (float)
            temperature = buffer.getFloat(8);                   // Bytes 8-11: temp (float)
            pressure = buffer.getFloat(12);                     // Bytes 12-15: p (float)
            altitude = buffer.getFloat(16);                     // Bytes 16-19: alt (float)
            Log.d(TAG, "Decoded 20-byte payload [t=" + sampleMillis + "]: H: " + humidity + " | T: " + temperature + " | P: " + pressure + " | A: " + altitude);
        } else {
            humidity = buffer.getFloat(0);    // Bytes 0-3
            temperature = buffer.getFloat(4); // Bytes 4-7
            pressure = buffer.getFloat(8);    // Bytes 8-11
            altitude = buffer.getFloat(12);   // Bytes 12-15
            Log.d(TAG, "Decoded 16-byte payload: H: " + humidity + " | T: " + temperature + " | P: " + pressure + " | A: " + altitude);
        }

        return new float[]{humidity, temperature, pressure, altitude};
    }

    private void enableConfiguration(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic){

    }

    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled)
    {
        if(bluetoothGatt == null){
            Log.w(TAG, "setCharacteristicNotification: Bluetooth not initialized.");
            return;
        }

        bluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                UUID.fromString(Constants.DESCRIPTOR_GEN_UUID));
        if (descriptor != null) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            bluetoothGatt.writeDescriptor(descriptor);
        }
    }
    private void close()
    {
        if(bluetoothGatt == null) {
            return;
        }
        bluetoothGatt.close();
        bluetoothGatt = null;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannels();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, createNotification("Weather Sensor Connecting", "Attempting to connect to BLE sensor..."), ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE);
        } else {
            startForeground(NOTIFICATION_ID, createNotification("Weather Sensor Connecting", "Attempting to connect to BLE sensor..."));
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        close();
        compositeDisposable.clear();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Toast.makeText(this,
                "Battery Low, Recharge and feel fresh again",
                Toast.LENGTH_SHORT).show();

    }

    public class LocalBinder extends  Binder
    {
        public WeatherBLEService getService() {
            return WeatherBLEService.this;
        }
    }
}
