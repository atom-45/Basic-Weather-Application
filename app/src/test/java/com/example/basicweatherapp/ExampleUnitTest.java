package com.example.basicweatherapp;

import org.junit.Test;

import static org.junit.Assert.*;

import android.util.Log;

import java.time.LocalTime;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @org.junit.Before
    public void setUp() {
        io.reactivex.rxjava3.android.plugins.RxAndroidPlugins.setInitMainThreadSchedulerHandler(scheduler -> io.reactivex.rxjava3.schedulers.Schedulers.trampoline());
    }

    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testingTime(){
        LocalTime localTime = LocalTime.now();
        System.out.println();
    }

    @Test
    public void testingTimeSplit(){
        String str = "2022-07-22 00:00";
        System.out.println(str.split(" ")[1].substring(0,2));
    }

    @Test
    public void testing20BytePayloadParsing() {
        java.nio.ByteBuffer buffer = java.nio.ByteBuffer.allocate(20).order(java.nio.ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(123456);     // t (uint32_t)
        buffer.putFloat(55.5f);    // h (humidity)
        buffer.putFloat(24.2f);    // temp (temperature)
        buffer.putFloat(1013.25f); // p (pressure)
        buffer.putFloat(150.0f);   // alt (altitude)

        byte[] payload = buffer.array();
        com.example.basicweatherapp.data.models.HistoricalSensorRecord record =
                com.example.basicweatherapp.services.WeatherBLEService.parseHistoricalRecord(payload);

        assertNotNull(record);
        assertEquals(123456L, record.sampleMillis());
        assertEquals(55.5f, record.humidity(), 0.001f);
        assertEquals(24.2f, record.temperature(), 0.001f);
        assertEquals(1013.25f, record.pressure(), 0.001f);
        assertEquals(150.0f, record.altitude(), 0.001f);
    }

    @Test
    public void testingRainAnalysisEngine() {
        float temp = 20.0f;
        float humidity = 85.0f;
        double dewPoint = com.example.basicweatherapp.physics.DewPointCalculator.calculate(temp, humidity);
        assertTrue(dewPoint > 17.0 && dewPoint < 18.0);
        String prediction = com.example.basicweatherapp.physics.RainAnalysisEngine.predict(temp, (float) dewPoint);
        assertNotNull(prediction);
        assertTrue(com.example.basicweatherapp.physics.RainAnalysisEngine.isLikely(temp, (float) dewPoint));
    }

    @Test
    public void testingObservable(){
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        Observable<Integer> integerObservable = Observable.just(1,2,3,4);
        Observable<String> stringObservable = Observable.just("A","B","C");

        Observable<List<?>> concatBoth = Observable
                .concat(integerObservable.toList().toObservable(),
                        stringObservable.toList().toObservable());

        Disposable disposable = concatBoth
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(disposable1 -> {
                    if(!disposable1.isDisposed()){
                        compositeDisposable.add(disposable1);

                    }})
                .doOnError(throwable -> {
                    Log.e("TEST 1", "testingObservable: ", throwable);
                    compositeDisposable.dispose();})
                .doOnComplete(() -> {
                    Log.d("TEST 1", "testingObservable: completed ");
                    compositeDisposable.dispose();})
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();

        compositeDisposable.add(disposable);
        compositeDisposable.dispose();
    }

    @Test
    public void testingDateParsingInBLEService() {
        // Test parsing ISO string without Z offset (LocalDateTime format: '2026-10-16T18:43:05.273')
        long millis1 = com.example.basicweatherapp.services.WeatherBLEService.parseDateToMillis("2026-10-16T18:43:05.273");
        assertTrue(millis1 > 0);

        // Test parsing ISO string with Z offset ('2026-10-16T18:43:05.273Z')
        long millis2 = com.example.basicweatherapp.services.WeatherBLEService.parseDateToMillis("2026-10-16T18:43:05.273Z");
        assertTrue(millis2 > 0);

        // Test parsing ISO truncated string with Z offset ('2026-10-16T18:43:05Z')
        long millis3 = com.example.basicweatherapp.services.WeatherBLEService.parseDateToMillis("2026-10-16T18:43:05Z");
        assertTrue(millis3 > 0);
    }

    @Test
    public void testingCalculateUtcTimestampNoZ() {
        String ts = com.example.basicweatherapp.services.WeatherBLEService.calculateUtcTimestamp(1000000L, 500000L, 200000L);
        assertNotNull(ts);
        assertFalse(ts.endsWith("Z"));
    }

    @Test
    public void testingDateParsingInThermodynamicsEngine() {
        com.example.basicweatherapp.data.models.SensorData s1 =
                new com.example.basicweatherapp.data.models.SensorData("2026-10-16T18:40:00.273", 50.0f, 20.0f, 1013.25f, 100.0f);
        com.example.basicweatherapp.data.models.SensorData s2 =
                new com.example.basicweatherapp.data.models.SensorData("2026-10-16T18:43:05.273Z", 52.0f, 19.5f, 1012.00f, 100.0f);

        com.example.basicweatherapp.physics.StormReport report =
                com.example.basicweatherapp.physics.StormThermodynamicsEngine.analyze(s1, s2);

        assertNotNull(report);
        assertNotEquals("Timestamp Error", report.statusMessage());
    }
}