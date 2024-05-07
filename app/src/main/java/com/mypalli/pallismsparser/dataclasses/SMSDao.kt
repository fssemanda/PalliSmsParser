package com.mypalli.pallismsparser.dataclasses

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SMSDao {
    @Insert
    suspend fun insert(smsDataEntity: SMSDataEntity)

    @Query("SELECT * FROM sms_data")
    suspend fun getAllSMSData(): List<SMSDataEntity>

    @Query("DELETE FROM sms_data WHERE id = :id")
    suspend fun deleteSMSData(id: Int)

    @Query("SELECT * FROM sms_data WHERE isSynced = 0")
    fun getUnsyncedSMS(): LiveData<List<SMSDataEntity>>

    @Query("UPDATE sms_data SET isSynced = 1 WHERE id = :smsId")
    suspend fun markAsSynced(smsId: Int)
}
