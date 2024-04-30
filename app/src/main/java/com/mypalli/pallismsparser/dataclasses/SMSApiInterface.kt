package com.mypalli.pallismsparser.dataclasses

import android.util.Log
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface SMSApiInterface {

    @POST("sms/sms-post/")
    @Headers("Accept:application/json", "Content-Type:application/json")
//    suspend fun postSMSData(@Body params:SMSData):SMSData
    suspend fun postSMSData(@Body params:SMSData):SMSData{
        println(params)
        Log.d("Inside the interface", params.toString())
        return params
    }


}