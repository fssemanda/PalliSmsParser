package com.mypalli.pallismsparser.dataclasses

data class SMSData(
    val telNetwork:String?,
    val transactionType:String?,
    val amount:String?,
    val phone_number:String?,
    val date:String?,
    val fee:String?,
    val balance:String?,
    val name:String?,
    val reason:String?,
    val transactionId:String?,
//    val :String?,
)
