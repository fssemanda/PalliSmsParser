package com.mypalli.pallismsparser

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.mypalli.pallismsparser.SMSViewModels.SMSViewModel
import okhttp3.internal.http.toHttpDateString
import java.util.Date
import kotlin.time.Duration.Companion.days
//class SmsReceiver : BroadcastReceiver() {
//    override fun onReceive(context: Context, intent: Intent) {
//        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
//            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
//            messages?.forEach { message ->
//                val sender = message.originatingAddress ?: "Unknown"
//                val body = message.messageBody ?: "No message content"
//                val timeDelivered = message.timestampMillis
//                val date = Date(timeDelivered)
//
//                val myData = extractTransactionDetails(body)
//
//                Log.d("SmsReceiver", "Received SMS from $sender: $body on $date")
//                myData?.let {
//                    Log.d("My Regex data", it.toString())
//                } ?: Log.d("My Regex data", "No data extracted")
//            }
//        }
//    }
//}

    class SmsReceiver : BroadcastReceiver() {
    private lateinit var messageString:String
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)

//            val vm:SMSViewModel= viewModel()

//            messages.forEach {   println(it.messageBody) }
            messageString=""

            for (singleMessage in messages)
            {
                messageString += singleMessage.messageBody.toString()

            }
            println(messageString)
//            messages.forEach { message ->
//                // Handle each message here. For example, print the content.
//                val sender = message.displayOriginatingAddress
//                val body = message.messageBody
//                val timeDelivered = message.timestampMillis.toLong()
//                val millis: Long = timeDelivered // example milliseconds
//                val date = Date(millis)
//                println(message.messageBody)
////                var myData= extractTransactionDetails(messageString,sender)
//
//
//
//                Log.d("SmsReceiver", "$body")
//                Log.d("My Regex data", "$myData")
//            }
            var myData= extractTransactionDetails(messageString,messages.first().displayOriginatingAddress)
            val myDataIntent = Intent("com.mypalli.pallismsparser.SMS_RECEIVED")
            myDataIntent.putExtra("sms_data", myData.toString()) // assuming myData is a JSON string
            LocalBroadcastManager.getInstance(context).sendBroadcast(myDataIntent)
        }
    }
}

fun getRegexPatterns(): List<Pair<String, Regex>> {
    return listOf(
        // Airtel
        // (\d{2}-[A-Za-z]+-\d{4} \d{2}:\d{2})
        // CASH DEPOSIT of UGX ([\d,]+) from\s+([A-Z ]+ [A-Z ]+).+Bal UGX ([\d,]+)\. TID:(\d+)\. Date (\d{2}-[A-Za-z]+)""".toRegex()
        "Airtel_Deposit" to """CASH DEPOSIT of UGX ([\d,]+) from\s+([A-Z ]+).+Bal UGX ([\d,]+)\. TID:(\d+).+Date (\d{2}-[A-Za-z]+)""".toRegex(),
        "Airtel_InternalPayment" to """PAID UGX ([\d,]+) to ([A-Z ]+).*Charge UGX ([\d,]+), TID (\d+).*Bal UGX ([\d,]+) Date: (\d{2}-[A-Za-z]+-\d{4} \d{2}:\d{2})""".toRegex(),
        "Airtel_CustomerPayment" to """RECEIVED UGX ([\d,]+) from (\d+), ([A-Z ]+).+Bal UGX ([\d,]+)\. TID: (\d+)""".toRegex(),
        "Airtel_Remittance" to """SENT UGX ([\d,]+) to ([A-Z ]+) (\d+).+Fee UGX ([\d,]+).+Bal UGX ([\d,]+)\. TID: (\d+).+Date: (\d{2}-[A-Za-z]+-\d{4} \d{2}:\d{2})""".toRegex(),
        "Airtel_Transfer" to """You have been debited UGX ([\d,]+)\. Fee UGX ([\d,]+)\. Bal UGX ([\d,]+)\. TID (\d+)""".toRegex(),
       "Airtel_Withdraw" to """Withdraw of UGX([\d,]+) with Agent ID: (\d+)\.Fee UGX ([\d,]+)\. Bal UGX ([\d,]+)\.TID: (\d+)\. Date (\d{2}-[A-Za-z]+-\d{4} \d{2}:\d{2})\.Tax UGX (\d+)\.https://bit\.ly/3ZgpiNw""".toRegex(),
        // MTN
        "MTN_Remittance" to """You have sent UGX ([\d,]+) to ([A-Z ]+), (\d+) on (\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}), fee: (\d+).+New balance: (\d+). ID :(\d+)""".toRegex(),
        "MTN_Withdraw" to """You have withdrawn UGX ([\d,]+) on (\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2})\. Fee: UGX ([\d,]+), Tax: UGX ([\d,]+)\. New balance: UGX ([\d,.]+)""".toRegex(),
        "MTN_Transfer" to """You have received UGX ([\d,]+) from ([A-Za-z ]+), (\d+) on (\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}).+fee:(\d+).+New balance: UGX ([\d,]+)\. ID: (\d+)""".toRegex(),
        "MTN_Deposit" to """You have deposited UGX ([\d,]+) from ([A-Z ]+) on (\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}). New balance: UGX ([\d,]+). ID: (\d+). Do NOT share your Mobile Money PIN.""".toRegex(),
//        "MTN_WithdrawRequest" to """Y'ello\. You have requested a withdrawal of UGX ([\d,]+) from ([\w\s]+)\. Dial \*165# and select My Approvals to authorize the transaction\.The total fee is UGX ([\d,]+) inclusive of ([\d\.]+) percent tax\.Transaction ID (\d+)""".toRegex(),
        "MTN_Request" to """Y'ello\. You have requested a withdrawal of UGX ([\d,]+) from ([\w\s]+)\. Dial \*165# and select My Approvals to authorize the transaction\.The total fee is  UGX ([\d,]+) inclusive of ([\d\.]+) percent tax\.Transaction ID (\d+)""".toRegex(),

        // MOMO""".toRegex() //
//        "MOMO_Payment" to """You have received ([\d,]+) UGX from ([A-Z ]+) \((\d+)\) on your mobile money account at (\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}).+Your new balance: ([\d,]+) UGX. Fee was ([\d,]+) UGX. Financial Transaction Id: (\d+).""".toRegex(),
//        "MOMO_Payment" to """You have received (\d+) UGX from ([A-Z ]+) \((\d+)\) on your mobile money account at (\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}).+Message from sender: Till:(\d+).+Your new balance: ([\d,]+) UGX. Fee was (\d+) UGX. Financial Transaction Id: (\d+).""".toRegex()
        "MOMO_Payment" to """You have received (\d+) UGX from ([A-Z ]+) \((\d+)\) on your mobile money account at (\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}).+Your new balance: (\d+) UGX.+ Fee was (\d+) UGX. Financial Transaction Id: (\d+).""".toRegex()
        )
}
fun extractTransactionDetails(text: String, originatingAddress:String): Map<String?, String?>? {
    val patterns = getRegexPatterns()
//    patterns.forEach { (type, pattern) ->
    for ((type, pattern) in patterns) {
//            println(type)
            pattern.find(text)?.let { matchResult ->
            var values = matchResult.groupValues.drop(1)
                if(type=="Airtel_Deposit"){
                    return mapOf(
                        "transactionType" to type.split("_")[1],
                        "telNetwork" to type.split("_")[0],
                        "amount" to values.getOrNull(0)!!,
                        "name" to values.getOrNull(1)!!,  // Adjust according to actual group index for each pattern
                        "phone_number" to originatingAddress,  // Adjust according to actual group index for each pattern
//                        "phone_number" to values.getOrNull(3)!!,  // Adjust according to actual group index for each pattern
//
                        "date" to values.getOrNull(4),
//                        "fee" to values.getOrNull(2),
                        "transactionId" to values.getOrNull(3),
                        "balance" to values.getOrNull(2),
//                        "fee" to values.getOrNull(3),
//                        "transactionId" to values.getOrNull(4) // Adjust according to actual group index for each pattern
                    )
                }
                else if(type=="Airtel_InternalPayment"){
                    return mapOf(
                        "transactionType" to type.split("_")[1],
                        "telNetwork" to type.split("_")[0],
                        "amount" to values.getOrNull(0)!!,
                        "name" to values.getOrNull(1)!!,  // Adjust according to actual group index for each pattern
                        "phone_number" to originatingAddress,  // Adjust according to actual group index for each pattern
//                        "phone_number" to values.getOrNull(3)!!,  // Adjust according to actual group index for each pattern
                        "fee" to values.getOrNull(1),
                        "transactionId" to values.getOrNull(2),
                        "balance" to values.getOrNull(3),
                        "date" to values.getOrNull(4),
                        // Adjust according to actual group index for each pattern
                    )
                } else if(type=="Airtel_Transfer"){
                    return mapOf(
                        "transactionType" to type.split("_")[1],
                        "telNetwork" to type.split("_")[0],
                        "amount" to values.getOrNull(0)!!,
                        "phone_number" to originatingAddress,
                        "fee" to values.getOrNull(1),
                        "transactionId" to values.getOrNull(3),
                        "balance" to values.getOrNull(2),
//                        "date" to values.getOrNull(4),
//                        "transactionId" to values.getOrNull(4) // Adjust according to actual group index for each pattern
                    )
                }

                else if(type=="Airtel_CustomerPayment"){
                    return mapOf(
                        "transactionType" to type.split("_")[1],
                        "telNetwork" to type.split("_")[0],
                        "amount" to values.getOrNull(0)!!,
                        "name" to values.getOrNull(2)!!,  // Adjust according to actual group index for each pattern
                        "phone_number" to values.getOrNull(1)!!,  // Adjust according to actual group index for each pattern
//                        "phone_number" to values.getOrNull(3)!!,  // Adjust according to actual group index for each pattern
//                                              "fee" to values.getOrNull(2),
                        "transactionId" to values.getOrNull(4),
                        "balance" to values.getOrNull(3),
//                        "date" to values.getOrNull(4),
//                        "transactionId" to values.getOrNull(4) // Adjust according to actual group index for each pattern
                    )
                }
                else if(type=="Airtel_Remittance"){
                    return mapOf(
                        "transactionType" to type.split("_")[1],
                        "telNetwork" to type.split("_")[0],
                        "amount" to values.getOrNull(0)!!,
                        "name" to values.getOrNull(1)!!,  // Adjust according to actual group index for each pattern
                        "phone_number" to values.getOrNull(2)!!,  // Adjust according to actual group index for each pattern
//                        "phone_number" to values.getOrNull(3)!!,  // Adjust according to actual group index for each pattern
                        "fee" to values.getOrNull(3),
                        "transactionId" to values.getOrNull(5),
                        "balance" to values.getOrNull(4),
                        "date" to values.getOrNull(6),
//                        "transactionId" to values.getOrNull(4) // Adjust according to actual group index for each pattern
                    )
                }
                else if(type=="Airtel_Withdraw"){
                    return mapOf(
                        "transactionType" to type.split("_")[1],
                        "telNetwork" to type.split("_")[0],
                        "amount" to values.getOrNull(0)!!,
                        "name" to values.getOrNull(1)!!,  // Adjust according to actual group index for each pattern
//                        "phone_number" to values.getOrNull(2)!!,  // Adjust according to actual group index for each pattern
//                        "phone_number" to values.getOrNull(3)!!,  // Adjust according to actual group index for each pattern
                        "fee" to values.getOrNull(2),
                        "transactionId" to values.getOrNull(4),
                        "balance" to values.getOrNull(3),
                        "date" to values.getOrNull(5),
                        "tax" to values.getOrNull(6) // Adjust according to actual group index for each pattern
                    )
                }

//                "MTN_Remittance" to """You have sent UGX ([\d,]+) to ([A-Z ]+), (\d+) on (\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}), fee: (\d+).+New balance: (\d+). ID :(\d+)""".toRegex(),
//                "MTN_Withdraw" to """You have withdrawn UGX ([\d,]+) on (\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2})\. Fee: UGX ([\d,]+), Tax: UGX ([\d,]+)\. New balance: UGX ([\d,.]+)""".toRegex(),


                else if(type=="MTN_Remittance"){
                    return mapOf(
                        "transactionType" to type.split("_")[1],
                        "telNetwork" to type.split("_")[0],
                        "amount" to values.getOrNull(0)!!,
                        "name" to values.getOrNull(1)!!,  // Adjust according to actual group index for each pattern
                        "phone_number" to values.getOrNull(2)!!,  // Adjust according to actual group index for each pattern
//                        "phone_number" to values.getOrNull(3)!!,  // Adjust according to actual group index for each pattern
                        "fee" to values.getOrNull(4),
                        "transactionId" to values.getOrNull(6),
                        "balance" to values.getOrNull(5),
                        "date" to values.getOrNull(3),
//                        "transactionId" to values.getOrNull(4) // Adjust according to actual group index for each pattern
                    )
                }
                else if(type=="MTN_Request"){
                    return mapOf(
                        "transactionType" to type.split("_")[1],
                        "telNetwork" to type.split("_")[0],
                        "amount" to values.getOrNull(0)!!,
                        "name" to values.getOrNull(1),
                        "fee" to values.getOrNull(2),
//                        "tax" to values.getOrNull(3),
////                        "balance" to values.getOrNull(4),
                        "transactionId" to values.getOrNull(4),
////                        "transactionId" to values.getOrNull(4) // Adjust according to actual group index for each pattern
                    )
                }
                else if(type=="MTN_Withdraw"){
                    return mapOf(
                        "transactionType" to type.split("_")[1],
                        "telNetwork" to type.split("_")[0],
                        "amount" to values.getOrNull(0)!!,
                        "fee" to values.getOrNull(2),
                        "tax" to values.getOrNull(3),
                        "balance" to values.getOrNull(4),
                        "date" to values.getOrNull(1),
//                        "transactionId" to values.getOrNull(4) // Adjust according to actual group index for each pattern
                    )
                }
// "MTN_Deposit" to """You have received UGX ([\d,]+) from ([A-Za-z ]+), (\d+) on (\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}).+fee:(\d+).+New balance: UGX ([\d,]+)\. ID: (\d+)""".toRegex(),

                else if(type=="MTN_Transfer"){
                    return mapOf(
                        "transactionType" to type.split("_")[1],
                        "telNetwork" to type.split("_")[0],
                        "amount" to values.getOrNull(0)!!,
                        "name" to values.getOrNull(1)!!,  // Adjust according to actual group index for each pattern
                        "phone_number" to values.getOrNull(2)!!,  // Adjust according to actual group index for each pattern
//                        "phone_number" to values.getOrNull(3)!!,  // Adjust according to actual group index for each pattern

                        "transactionId" to values.getOrNull(5),
                        "balance" to values.getOrNull(4),
                        "date" to values.getOrNull(3),
//                        "transactionId" to values.getOrNull(4) // Adjust according to actual group index for each pattern
                    )
                }
                else if(type=="MTN_Deposit"){
                    return mapOf(
                        "transactionType" to type.split("_")[1],
                        "telNetwork" to type.split("_")[0],
                        "amount" to values.getOrNull(0)!!,
                        "name" to values.getOrNull(1)!!,  // Adjust according to actual group index for each pattern
//                        "phone_number" to values.getOrNull(2)!!,  // Adjust according to actual group index for each pattern
//                        "phone_number" to values.getOrNull(3)!!,  // Adjust according to actual group index for each pattern
//                        "fee" to values.getOrNull(3),
                        "transactionId" to values.getOrNull(4),
                        "balance" to values.getOrNull(3),
                        "date" to values.getOrNull(2),
//                        "transactionId" to values.getOrNull(4) // Adjust according to actual group index for each pattern
                    )
                }
//                "MOMO_Payment" to """You have received ([\d,]+) UGX from ([A-Z ]+) \((\d+)\) on your mobile money account at (\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}).+Your new balance: ([\d,]+) UGX. Fee was ([\d,]+) UGX. Financial Transaction Id: (\d+).""".toRegex()

                else if(type=="MOMO_Payment"){
                    return mapOf(
                        "transactionType" to type.split("_")[1],
                        "telNetwork" to type.split("_")[0],
                        "amount" to values.getOrNull(0)!!,
                        "name" to values.getOrNull(1)!!,  // Adjust according to actual group index for each pattern
                        "phone_number" to values.getOrNull(2)!!,  // Adjust according to actual group index for each pattern
//                        "phone_number" to values.getOrNull(3)!!,  // Adjust according to actual group index for each pattern
                        "fee" to values.getOrNull(5),
//                        "transactionId" to values.getOrNull(6),
                        "balance" to values.getOrNull(4),
                        "date" to values.getOrNull(3),
                        "transactionId" to values.getOrNull(6) // Adjust according to actual group index for each pattern
                    )
                }

                else
                    return  null
//            return mapOf(
//                "transactionType" to type.split("_")[1],
//                "telNetwork" to type.split("_")[0],
//                "amount" to values.getOrNull(2)!!,
//                "name" to values.getOrNull(1)!!,  // Adjust according to actual group index for each pattern
////                "phone_number" to originatingAddress,  // Adjust according to actual group index for each pattern
//                "phone_number" to values.getOrNull(3)!!,  // Adjust according to actual group index for each pattern
//                "date" to values.getOrNull(4),
//                "time" to values.getOrNull(5),
//                "fee" to values.getOrNull(6) , // Adjust according to actual group index for each pattern
//                "tax" to values.getOrNull(7) ,// Adjust according to actual group index for each pattern
//                "new_balance" to values.getOrNull(8),  // Adjust according to actual group index for each pattern
//                "transactionId" to values.getOrNull(9) // Adjust according to actual group index for each pattern
//            )
        }
    }
    return null
}


