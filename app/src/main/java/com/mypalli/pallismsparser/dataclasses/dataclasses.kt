package com.mypalli.pallismsparser.dataclasses

import androidx.room.Entity
import androidx.room.PrimaryKey

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
    val tax:String?,
)

@Entity(tableName = "sms_data")
data class SMSDataEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val telNetwork: String?,
    val transactionType: String?,
    val amount: String?,
    val phone_number: String?,
    val date: String?,
    val fee: String?,
    val balance: String?,
    val name: String?,
    val reason: String?,
    val transactionId: String?,
    val tax: String?,
    val isSynced: Boolean = false
)
