package com.example.basicweatherapp.services;

import android.annotation.SuppressLint;
import android.app.Application;
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
import com.example.basicweatherapp.data.models.SensorData;
import com.example.basicweatherapp.data.repositories.SensorDataRepository;
import com.example.basicweatherapp.di.application.WeatherApplication;
import com.example.basicweatherapp.physics.DewPointCalculator;
import com.example.basicweatherapp.physics.RainAnalysisEngine;
import com.example.basicweatherapp.physics.StormReport;
import com.example.basicweatherapp.physics.StormThermodynamicsEngine;
import com.example.basicweatherapp.presentation.activities.MainActivity;
import com.example.basicweatherapp.utilities.Constants;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
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

                BluetoothGattCharacteristic characteristic = service
                        .getCharacteristic(UUID
                                .fromString(Constants.RP2040_CHARACTERISTICS_UUID));


                readCharacteristic(characteristic);
                //writeCharacteristic(characteristic);
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

            float[] sensorFloats = getArraySensorData(characteristic);

            SensorData sensorData = new SensorData(
                    LocalDateTime.now().toString(), //current date and time of data collection
                    sensorFloats[0], //humidity in %
                    sensorFloats[1], // degree celsius
                    sensorFloats[2], //hPa
                    sensorFloats[3]); //metres

            if(sensorDataRepository != null) {
                Log.d(TAG, "onCharacteristicChanged: Sensor Repository is not null");
                Disposable disposable = sensorDataRepository.insert(sensorData)
                        .doOnComplete(() -> {
                            Log.d(TAG, "SUCCESS: Sensor data inserted into Room database.");
                            performAtmosphericAnalysis();
                        })
                        .doOnError(throwable -> Log.e(TAG, "ERROR: Failed to insert sensor data", throwable ))
                        .subscribe();
                compositeDisposable.add(disposable);
            }

            Log.d(TAG, "onCharacteristicChanged: "+sensorData);
            final Intent intent = new Intent(ACTION_DATA_AVAILABLE);
            intent.putExtra(EXTRA_SENSOR_DATA, sensorFloats);
            sendBroadcast(intent);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt,
                                      BluetoothGattDescriptor descriptor,
                                      int status)
        {
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

                    // 3. Combined Logic for Notification
                    if (!thermoReport.isSafeToWalk() || rainLikely) {
                        String title = "Weather Alert: Storm Approaching";
                        String content = thermoReport.isSafeToWalk() ? 
                                "Rain expected: " + rainPrediction : 
                                thermoReport.statusMessage();
                        
                        sendWeatherAlert(title, content);
                    }
                }, throwable -> Log.e(TAG, "Error during atmospheric analysis", throwable));
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

        if(value==null || value.length < 16)
        {
            Log.e(TAG, "getArraySensorData: Received invalid data length: " +
                    (value != null ? value.length : 0));

            return new float[]{0f, 0f, 0f, 0f};
        }

        ByteBuffer buffer = ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN);

        float humidity = buffer.getFloat();    // Reads bytes 0-3
        float temperature = buffer.getFloat(); // Reads bytes 4-7
        float pressure = buffer.getFloat();    // Reads bytes 8-11
        float altitude = buffer.getFloat();

        Log.d(TAG, "Decoded: H: " + humidity + " | T: " + temperature + " | P: " + pressure + " | A: " + altitude);

        return new float[]{humidity, temperature, pressure, altitude};
    }

    private void enableConfiguration(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic){

    }

    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled)
    {
        if(bluetoothGatt == null){
            Log.w(TAG, "setCharacteristicNotification: Bluetooth not initialized.");
        }

        bluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        if(UUID.fromString(Constants.RP2040_CHARACTERISTICS_UUID).equals(characteristic.getUuid())){

            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(Constants.DESCRIPTOR_GEN_UUID));

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
