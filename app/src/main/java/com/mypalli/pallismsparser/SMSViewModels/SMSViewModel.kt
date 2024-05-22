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
            try {
                val smsPostData = postSMSData(smsData)
                smsDataState.value = smsPostData
                Log.d("Sending Data to Repository", smsData.toString())
            }
            catch (ex:Exception){
                Log.d("Failed to Post Saving to localDB", smsData.toString())
                saveDataLocally(smsData)

            }
        }
    }
    private fun saveDataLocally(smsData: SMSData) {
        Log.d("Saving SMS Cache","Attempting Saving to Cache")
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
            try{
                database.smsDao().insert(entity)
                val dbData= database.smsDao().getAllSMSData()
                Log.d("Saved SMS Cache","Successfully Saved Cache")
                dbData.forEach{
                    println(it.amount)
                }
            }
            catch (ex:Exception){
                val dbData= database.smsDao().getAllSMSData()
                dbData.forEach{
                    println("There was an exception: $ex $it")
                }


                Log.d("Failure","Failed Saved Cache: $ex")
            }
        }
    }

    fun synchronizeData() {
        viewModelScope.launch {
            try {
                val smsDataList = database.smsDao().getAllSMSData()
                Log.d("SMSLIST", "${smsDataList.size}")
                smsDataList.forEach {
                    try {
                        Log.e("Data Sync", "Synchronzing Data: TransactionID: ${it.transactionId}")

//                    postSMSData(smsDataState.value!!)
                        val mySMSData = SMSData(
                            telNetwork = it.telNetwork,
                            amount = it.amount,
                            name = it.name,
                            transactionId = it.transactionId,
                            transactionType = it.transactionType,
                            tax = it.tax,
                            balance = it.balance,
                            phone_number = it.phone_number,
                            date = it.date,
                            fee = it.fee,
                            reason = it.reason,
                        )
                        postSMSData(mySMSData)

                        database.smsDao().deleteSMSData(it.id)  // Delete after successful upload
                    } catch (e: Exception) {
                        Log.e("Sync Error", "Failed to upload: ${e.message}")
                    }
                }
            }catch (ex:Exception){
                Log.e("Sync Error", "Failed to upload: ${ex.message}")

            }
        }
    }

    suspend fun postSMSData(smsData: SMSData):SMSData{
        Log.d("Posting Data", smsData.toString())
        return smsRepository.postSMSData(smsData)
    }

}