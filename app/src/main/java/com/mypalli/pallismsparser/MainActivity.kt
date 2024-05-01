package com.mypalli.pallismsparser

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.provider.Telephony
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.gson.Gson
import com.mypalli.pallismsparser.SMSViewModels.SMSViewModel
import com.mypalli.pallismsparser.dataclasses.SMSData
import com.mypalli.pallismsparser.ui.theme.PALLISMSPARSERTheme

class MainActivity : ComponentActivity() {

    private val smsViewModel by viewModels<SMSViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PALLISMSPARSERTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MyScreen()
                }
            }
        }
        try{
            LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                IntentFilter("com.mypalli.pallismsparser.SMS_RECEIVED")
            )
        }
        catch (ex:Exception){
            Toast.makeText(this, "${ex.message}",Toast.LENGTH_LONG).show()
        }

    }
    private val mMessageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Extract data from the intent
            val data = intent.getStringExtra("sms_data")
            Log.d("Data Received via intent", "$data")
            val formattedString = toJsonObject(data.toString())
            Log.d("Data Received via intent", formattedString)

            if (formattedString != null) {
                try {
                    handleSMSData(formattedString)
                }
                catch (ex:Exception){
                    Toast.makeText(context, "${ex.message}",Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun handleSMSData(data: String) {
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



    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver)
        super.onDestroy()
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

}

@Composable
fun MyScreen() {
    PermissionRequester(
        onPermissionGranted = {
//            val context = LocalContext.
           Log.d("Permissions","User has been granted SMS permissions")
//            val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
//            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, "com.mypalli.pallismsparser")
//            startActivity(,intent)
        },
        onPermissionDenied = {
            Log.d("Permissions","User has been Denied SMS permissions")
        }
    )

    }

@SuppressLint("RememberReturnType")
@Composable
fun PermissionRequester(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit
) {
    val context = LocalContext.current
    val permissions = listOf(Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS)

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissionsMap ->
            if (permissionsMap.values.all { it }) {
                onPermissionGranted()
            } else {
                onPermissionDenied()
            }
        }

    )

    LaunchedEffect(Unit) {
        if (permissions.any { ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED }) {
            permissionLauncher.launch(permissions.toTypedArray())
        } else {
            onPermissionGranted()
        }
    }

//    val vm:SMSViewModel= viewModel()
//    vm.postData()

}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PALLISMSPARSERTheme {
        MyScreen()
    }
}