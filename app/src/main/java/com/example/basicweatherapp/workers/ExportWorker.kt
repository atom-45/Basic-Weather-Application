package com.example.basicweatherapp.workers

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.basicweatherapp.R
import com.example.basicweatherapp.data.local.database.WeatherDatabase
import com.example.basicweatherapp.data.models.PredictionVerification
import com.example.basicweatherapp.data.models.SensorData
import com.example.basicweatherapp.data.models.ThermodynamicPrediction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ExportWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        private const val CHANNEL_ID = "ExportChannel"
        private const val NOTIFICATION_ID = 101
        const val KEY_START_DATE = "start_date"
        const val KEY_END_DATE = "end_date"
        const val TAG = "ExportWorker"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val startDate = inputData.getString(KEY_START_DATE) ?: return@withContext Result.failure()
        val endDate = inputData.getString(KEY_END_DATE) ?: return@withContext Result.failure()

        showNotification("Exporting...", "Generating your weather dataset...")

        try {
            val db = WeatherDatabase.getInstance(applicationContext)
            val sensorData = db.sensorDAO().getSensorDataByRange(startDate, endDate)
            val verifications = db.predictionVerificationDAO().getVerificationsByRange(startDate, endDate)
            val thermoPredictions = db.thermodynamicPredictionDAO().getPredictionsByRange(startDate, endDate)

            if (sensorData.isEmpty() && verifications.isEmpty() && thermoPredictions.isEmpty()) {
                showNotification("Export Failed", "No data found for the selected range.")
                return@withContext Result.failure()
            }

            val cacheDir = applicationContext.cacheDir
            val sensorFile = File(cacheDir, "raw_sensor_data.csv")
            val verifFile = File(cacheDir, "labeled_verifications.csv")
            val thermoFile = File(cacheDir, "thermodynamic_blackbox.csv")
            val zipFile = File(cacheDir, "weather_export_${startDate.substring(0, 10)}.zip")

            // Write Sensor Data CSV
            FileOutputStream(sensorFile).use { out ->
                out.write("Date,Temperature(C),Humidity(%),Pressure(hPa),Altitude(m)\n".toByteArray())
                sensorData.forEach { data ->
                    val row = String.format(Locale.ENGLISH, "%s,%.2f,%.2f,%.2f,%.2f\n", 
                        data.date, data.temperature, data.humidity, data.pressure, data.altitude)
                    out.write(row.toByteArray())
                }
            }

            // Write Verifications CSV
            FileOutputStream(verifFile).use { out ->
                out.write("Timestamp,Temp,Humidity,Pressure,Prediction,UserFeedback\n".toByteArray())
                verifications.forEach { v ->
                    val row = String.format(Locale.ENGLISH, "%s,%.2f,%.2f,%.2f,%s,%s\n",
                        v.timestamp, v.temperature, v.humidity, v.pressure, v.prediction, v.actualOutcome)
                    out.write(row.toByteArray())
                }
            }

            // Write Thermodynamic Predictions CSV
            FileOutputStream(thermoFile).use { out ->
                out.write("Timestamp,Temp,Humidity,Pressure,PredictedArrival,Speed,Residual,EventID,Status,ActualOutcome\n".toByteArray())
                thermoPredictions.forEach { p ->
                    val row = String.format(Locale.ENGLISH, "%s,%.2f,%.2f,%.2f,%s,%.2f,%.2f,%s,%s,%s\n",
                        p.originalTimestamp, p.temperature, p.humidity, p.pressure, 
                        p.predictedArrival, p.stormSpeed, p.residual, p.stormEventId, 
                        p.verificationStatus, p.actualOutcome ?: "N/A")
                    out.write(row.toByteArray())
                }
            }

            // Create Zip
            ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
                addToZip(zos, sensorFile)
                addToZip(zos, verifFile)
                addToZip(zos, thermoFile)
            }

            saveToDownloads(zipFile)

            showNotification("Export Complete", "Dataset saved to Downloads folder.")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Export error", e)
            showNotification("Export Failed", "An error occurred while generating the file.")
            Result.failure()
        }
    }

    private fun addToZip(zos: ZipOutputStream, file: File) {
        zos.putNextEntry(ZipEntry(file.name))
        file.inputStream().use { it.copyTo(zos) }
        zos.closeEntry()
    }

    @SuppressLint("NewApi")
    private fun saveToDownloads(file: File) {
        val resolver = applicationContext.contentResolver
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/zip")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let {
                resolver.openOutputStream(it)?.use { outputStream ->
                    file.inputStream().use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }
        } else {
            // Fallback for API < 29
            @Suppress("DEPRECATION")
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val destFile = File(downloadsDir, file.name)
            file.inputStream().use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
    }

    @SuppressLint("MissingPermission", "NotificationPermission")
    private fun showNotification(title: String, message: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Data Export", NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.sunny)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
