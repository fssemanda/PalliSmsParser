package com.mypalli.pallismsparser

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.lifecycle.lifecycleScope
import com.mypalli.pallismsparser.ConnectivityObserver.ConnectivityObserver
import com.mypalli.pallismsparser.ConnectivityObserver.NetworkConnectivityObserver
import com.mypalli.pallismsparser.SMSViewModels.SMSViewModel
import com.mypalli.pallismsparser.dataclasses.AppDatabase
import com.mypalli.pallismsparser.dataclasses.SMSRepository
import com.mypalli.pallismsparser.dataclasses.jsonConversionHelper.toJsonObject
import com.mypalli.pallismsparser.dataclasses.jsonConversionHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class SMSProcessingService : Service() {
    companion object {
        const val NOTIFICATION_ID = 1 // Unique ID for the notification
    }
    private lateinit var  smsViewModel: SMSViewModel
//    @Inject lateinit var smsViewModel: SMSViewModel
    private lateinit var connectivityObserver: ConnectivityObserver
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback

    override fun onCreate() {
        super.onCreate()
        val database = AppDatabase.getDatabase(this)
        val repository:SMSRepository = SMSRepository() // Adjust constructor as needed
        smsViewModel = SMSViewModel(repository, database)
        val syncJob = Job()
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                // Network is available, synchronize data
                Log.d("SMSProcessingService", "Network available, synchronizing data...")
                try{
                    smsViewModel?.let {
                        CoroutineScope(syncJob+Dispatchers.IO).launch {
                            it.synchronizeData()
                        }
                    }
                }catch (ex:Exception){
                    Toast.makeText(this@SMSProcessingService, "Error: App will restart.",Toast.LENGTH_SHORT).show()
                    Log.e("Sync Error", "Failed to upload: ${ex.message}")
                }

            }

            override fun onLost(network: Network) {
                super.onLost(network)
                // Network is lost, handle accordingly if needed
                Log.d("SMSProcessingService", "Network lost")
            }
        }
        // Register network callback
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.unregisterNetworkCallback(networkCallback)

    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        val data = intent.getStringExtra("message_body")
//        val data = intent.getStringExtra("sms_data")
        Log.d("Inside Service", "$data")
        try {
            if (data == null) {
                Log.d("SMS Data was null", "formattedString")
                return START_NOT_STICKY
            } else {
                val formattedString = toJsonObject(data.toString())
                Log.d("Data Received via intent", "$formattedString")

                formattedString?.let {

                    jsonConversionHelper.handleSMSData(this, it, smsViewModel)
                }
            }
        }catch (ex:Exception){
            Toast.makeText(this@SMSProcessingService, "BG Service Error: $ex.",Toast.LENGTH_SHORT).show()
            Log.e("Sync Error", "Failed to upload: ${ex.message}")
        }
        return START_NOT_STICKY
    }
    private fun createNotification(): Notification {
        val notificationChannelId = "sms_service_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "SMS Service Channel"
            val descriptionText = "Notifications for SMS Service"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(notificationChannelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(this, notificationChannelId)
        return notificationBuilder
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_launcher_foreground)  // make sure you have this icon
            .setContentTitle("SMS Service Active")
            .setContentText("Listening for incoming SMS.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

}

