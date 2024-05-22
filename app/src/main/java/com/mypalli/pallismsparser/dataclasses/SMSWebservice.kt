package com.mypalli.pallismsparser.dataclasses

import android.util.Log

private lateinit var SMSApi:SMSApiInterface

class SMSWebservice {
    init {

        val retrofitBuilderClass = RetrofitBuilderClass.getRetroInstance().create(SMSApiInterface::class.java)
        SMSApi = retrofitBuilderClass
    }

    suspend fun postSMSData(smsData:SMSData): SMSData {
        Log.d("Sending Data to endpoint", smsData.toString())
        return SMSApi.postSMSData(smsData)

    }
}