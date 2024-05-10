package com.mypalli.pallismsparser.dataclasses

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.mypalli.pallismsparser.MainActivity
import com.mypalli.pallismsparser.SMSViewModels.SMSViewModel

class NetworkChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (isOnline(context)) {

            Log.d("Logs", "App is Online")

            val viewModel = ViewModelProvider(context as MainActivity)[SMSViewModel::class.java]
            viewModel.synchronizeData()
        }
        else
            Log.d("Logs", "App is offline")
    }

    private fun isOnline(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        Log.d("Current Network Status","$networkInfo")
        return networkInfo != null && networkInfo.isConnected
    }
}

