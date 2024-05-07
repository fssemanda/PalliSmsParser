package com.mypalli.pallismsparser.SMSViewModels

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mypalli.pallismsparser.dataclasses.AppDatabase
import com.mypalli.pallismsparser.dataclasses.SMSData
import com.mypalli.pallismsparser.dataclasses.SMSDataEntity
import com.mypalli.pallismsparser.dataclasses.SMSRepository
import kotlinx.coroutines.launch

class SMSViewModel(private var smsRepository: SMSRepository= SMSRepository(),private val database: AppDatabase):ViewModel() {
     var smsDataList = mutableStateOf<SMSData?>(null)

    val smsDataState = mutableStateOf<SMSData?>(null)
    val telNetworkState = mutableStateOf<String?>(null)
    val transactionType= mutableStateOf<String?>(null)
    val amount= mutableStateOf<String?>(null)
    val phone_number= mutableStateOf<String?>(null)
    val date= mutableStateOf<String?>(null)
    val fee= mutableStateOf<String?>(null)
    val balance= mutableStateOf<String?>(null)
    val name= mutableStateOf<String?>(null)
    val reason= mutableStateOf<String?>(null)
    val transactionId= mutableStateOf<String?>(null)
    val tax= mutableStateOf<String?>(null)

    
//    fun postData(data:ArrayList<SMSData>){
//        viewModelScope.launch {
////            val smsData =
//            val smsData = SMSData(
//                telNetwork = telNetworkState.value,
//                transactionType = transactionType.value,
//                amount=amount.value,
//                phone_number = phone_number.value,
//                date = date.value,
//                fee = fee.value,
//                balance = balance.value,
//                name = name.value,
//                reason=reason.value,
//                transactionId.value,
//
//            )
//            val smsPostData = postSMSData(smsData)
//            smsDataState.value = smsPostData
//        }
//    }
    fun postData(){
        viewModelScope.launch {
//            val mySMSData =  (data)
            val smsData = SMSData(
                telNetwork = telNetworkState.value,
                transactionType = transactionType.value,
                amount=amount.value,
                phone_number = phone_number.value,
                date = date.value,
                fee = fee.value,
                balance = balance.value,
                name = name.value,
                reason=reason.value,
                transactionId=transactionId.value,
                tax = tax.value

            )

            Log.d("Data to be posted", smsData.toString())
//            var datalist = smsDataList
            val smsPostData = postSMSData(smsData)
            smsDataState.value = smsPostData
            Log.d("Data to be posted", smsData.toString())
        }
    }
    fun saveDataLocally(smsData: SMSData) {
        viewModelScope.launch {
            val entity = SMSDataEntity(
                telNetwork = smsData.telNetwork,
                transactionType = smsData.transactionType,
                amount = smsData.amount,
                phone_number = smsData.phone_number,
                date = smsData.date,
                fee = smsData.fee,
                balance = smsData.balance,
                name = smsData.name,
                reason = smsData.reason,
                transactionId = smsData.transactionId,
                tax = smsData.tax
            )
            database.smsDao().insert(entity)
        }
    }

    fun synchronizeData() {
        viewModelScope.launch {
            val smsDataList = database.smsDao().getAllSMSData()
            smsDataList.forEach {
                try {
                    Log.e("Saving Data to DB", "it")

                    postSMSData(smsDataState.value!!)
                    database.smsDao().deleteSMSData(it.id)  // Delete after successful upload
                } catch (e: Exception) {
                    Log.e("Sync Error", "Failed to upload: ${e.message}")
                }
            }
        }
    }

    suspend fun postSMSData(smsData: SMSData):SMSData{
        Log.d("Inside postSMSData in ViewModel", smsData.toString())
        return smsRepository.postSMSData(smsData)
    }

}