    package com.mypalli.pallismsparser.dataclasses

    import android.util.Log

    class SMSRepository(private var smsWebservice: SMSWebservice = SMSWebservice()) {

        suspend fun postSMSData(smsData:SMSData):SMSData{
            Log.d("Inside Repository Function", smsData.toString())
            return smsWebservice.postSMSData(smsData)
        }
    }