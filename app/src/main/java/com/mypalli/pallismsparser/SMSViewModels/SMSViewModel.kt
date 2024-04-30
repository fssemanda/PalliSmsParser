package com.mypalli.pallismsparser.SMSViewModels

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mypalli.pallismsparser.dataclasses.SMSData
import com.mypalli.pallismsparser.dataclasses.SMSRepository
import kotlinx.coroutines.launch

class SMSViewModel(private var smsRepository: SMSRepository= SMSRepository()):ViewModel() {
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

            )

            Log.d("Data to be posted", smsData.toString())
//            var datalist = smsDataList
            val smsPostData = postSMSData(smsData)
            smsDataState.value = smsPostData
            Log.d("Data to be posted", smsData.toString())
        }
    }
    
    suspend fun postSMSData(smsData: SMSData):SMSData{
        Log.d("Inside postSMSData in ViewModel", smsData.toString())
        return smsRepository.postSMSData(smsData)
    }

}