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

        return "{$jsonEntries}"
    }
    fun handleSMSData(context: Context, data: String, smsViewModel: SMSViewModel) {
        // Convert JSON back to your object and process it with ViewModel
        try {
        val smsData = Gson().fromJson(data, SMSData::class.java)
            if (smsData != null) {
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
            } else {
                Log.e("JSONConversion", "Failed to parse JSON data.")
            }
        } catch (e: JsonSyntaxException) {
            Log.e("JSONConversion", "Error parsing JSON", e)
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