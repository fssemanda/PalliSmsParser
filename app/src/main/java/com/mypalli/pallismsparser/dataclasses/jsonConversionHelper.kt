package com.mypalli.pallismsparser.dataclasses

import android.app.Application
import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.mypalli.pallismsparser.SMSViewModels.SMSViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

object jsonConversionHelper {
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
        Log.d("My Json Entries", jsonEntries)
        return "{$jsonEntries}"
    }
    fun handleSMSData(context: Context, data: String, smsViewModel: SMSViewModel) {
        Log.d("Data for conversation",data)
        // Convert JSON back to your object and process it with ViewModel
        try {
            val smsData = Gson().fromJson(data, SMSData::class.java)
            if (smsData != null) {
//        val smsData = Gson().fromJson(data, SMSData::class.java)

//        smsViewModel.smsDataList = SMSData(smsData.telNetwork,smsData.transactionType,smsData.amount,smsData.phone_number,smsData.date,
//            smsData.fee,smsData.balance,smsData.name,smsData.reason,smsData.transactionId))
        smsViewModel.amount.value=smsData.amount!!.toString()
        smsViewModel.balance.value=smsData.balance!!.toString()
        smsViewModel.name.value=smsData.name!!.toString()
        smsViewModel.fee.value=smsData.fee!!.toString()
        smsViewModel.phone_number.value=smsData.phone_number!!.toString()
        smsViewModel.reason.value=smsData.reason!!.toString()
        smsViewModel.transactionType.value=smsData.transactionType!!.toString()
        smsViewModel.transactionId.value=smsData.transactionId!!.toString()
        smsViewModel.date.value = smsData.date!!.toString()
        smsViewModel.code.value = smsData.code!!.toString()
        smsViewModel.telNetworkState.value=smsData.telNetwork!!.toString()
        smsViewModel.tax.value=smsData.tax!!.toString()

        Log.d("Code before being processed",smsData.code.toString())

        smsViewModel.postData()
            } else {
                Log.e("JSONConversion", "Failed to parse JSON data.")
            }
        } catch (e: JsonSyntaxException) {
            Log.e("JSONConversion", "Error parsing JSON")
        }
    }
//

}

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideDatabase(application: Application): AppDatabase = AppDatabase.getDatabase(application)

    @Provides
    fun provideRepository(database: AppDatabase): SMSRepository = SMSRepository()
}