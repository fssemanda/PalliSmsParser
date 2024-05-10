package com.mypalli.pallismsparser

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import com.mypalli.pallismsparser.SMSViewModels.SMSViewModel
import com.mypalli.pallismsparser.dataclasses.AppDatabase
import com.mypalli.pallismsparser.dataclasses.SMSData
import com.mypalli.pallismsparser.dataclasses.SMSRepository
import com.mypalli.pallismsparser.ui.theme.jsonConversionHelper.toJsonObject
import com.mypalli.pallismsparser.ui.theme.jsonConversionHelper
import javax.inject.Inject

class SMSProcessingService : Service() {
    companion object {
        const val NOTIFICATION_ID = 1 // Unique ID for the notification
    }
//    private lateinit var  smsViewModel: SMSViewModel
    @Inject lateinit var smsViewModel: SMSViewModel

//    override fun onCreate() {
//        super.onCreate()
//        val database = AppDatabase.getDatabase(this)
//        val repository:SMSRepository = SMSRepository() // Adjust constructor as needed
//        smsViewModel = SMSViewModel(repository, database)
//    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        val data = intent.getStringExtra("sms_data")
        Log.d("Inside Service", "$data")

//        The challenge is Data being returned is null and there is no way a null string can be formatted

//        val formattedString = toJsonObject(data.toString())
        Log.d("Data Received via intent", "$data")

//        data?.let {
//
//            jsonConversionHelper.handleSMSData(this, it, smsViewModel)
//        }
        handleSMSData(formattedString!!)




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
    fun handleSMSData(data:String) {
        // Convert JSON back to your object and process it with ViewModel
        val smsData = Gson().fromJson(data, SMSData::class.java)
//        val smsData = Gson().fromJson(data, SMSData::class.java)

//        smsViewModel.smsDataList = SMSData(smsData.telNetwork,smsData.transactionType,smsData.amount,smsData.phone_number,smsData.date,
//            smsData.fee,smsData.balance,smsData.name,smsData.reason,smsData.transactionId))
        smsViewModel.amount.value=smsData.amount
        smsViewModel.balance.value=smsData.balance
        smsViewModel.name.value=smsData.name
        smsViewModel.fee.value=smsData.fee
        smsViewModel.phone_number.value=smsData.phone_number
        smsViewModel.reason.value=smsData.reason
        smsViewModel.transactionType.value=smsData.transactionType
        smsViewModel.transactionId.value=smsData.transactionId
        smsViewModel.date.value = smsData.date
        smsViewModel.telNetworkState.value=smsData.telNetwork.toString()
        smsViewModel.tax.value=smsData.tax.toString()

        Log.d("Data before being processed",smsData.tax.toString())

        smsViewModel.postData()
        // Update your ViewModel state or call a function to process the data
    }

    fun toJsonObject(input: String): String {
        // Remove the enclosing braces and trim any surrounding whitespace
        val trimmed = input.trim().removeSurrounding("{", "}")

        // Split into key-value pairs
        val keyValuePairs = trimmed.split(", ")

        // Build JSON string
        val jsonEntries = keyValuePairs.map { pair ->
            val (key, value) = pair.split("=")
            // Properly quote the key and value, escaping necessary characters in JSON
            "\"${key.trim()}\": \"${value.trim().replace("\"", "\\\"")}\""
        }.joinToString(", ")

        return "{$jsonEntries}"
    }
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

}

