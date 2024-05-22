    package com.mypalli.pallismsparser.dataclasses

    import android.util.Log

    class SMSRepository(private var smsWebservice: SMSWebservice = SMSWebservice()) {

        suspend fun postSMSData(smsData:SMSData):SMSData{
            Log.d("Sending Data to WebService:", smsData.toString())
            return smsWebservice.postSMSData(smsData)
        }
    }