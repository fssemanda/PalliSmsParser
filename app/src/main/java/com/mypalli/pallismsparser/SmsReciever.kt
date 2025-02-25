package com.mypalli.pallismsparser

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.lang.Exception
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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

            messageString=""

            for (singleMessage in messages)
            {
                messageString += singleMessage.messageBody.toString()

            }
            println(messageString)

            try {
                var myData= extractTransactionDetails(messageString,messages.first().displayOriginatingAddress)
                val myDataIntent = Intent("com.mypalli.pallismsparser.SMS_RECEIVED")
                myDataIntent.putExtra("sms_data", myData.toString()) // assuming myData is a JSON string
                LocalBroadcastManager.getInstance(context).sendBroadcast(myDataIntent)
//                context.startService(myDataIntent)
                Intent(context, SMSProcessingService::class.java).also { intent ->
                    intent.putExtra("message_body", myData.toString())
                    ContextCompat.startForegroundService(context, intent)
                }
            }
            catch (ex: Exception){
                val myIntent = Intent("SMS_ISSUES")
                myIntent.putExtra("sms_data", "${ex.message}")
                LocalBroadcastManager.getInstance(context).sendBroadcast(myIntent)

            }

        }
    }
}

fun getRegexPatterns(): List<Pair<String, Regex>> {
    return listOf(
        // Airtel
        // (\d{2}-[A-Za-z]+-\d{4} \d{2}:\d{2})
        // CASH DEPOSIT of UGX ([\d,]+) from\s+([A-Z ]+ [A-Z ]+).+Bal UGX ([\d,]+)\. TID:(\d+)\. Date (\d{2}-[A-Za-z]+)""".toRegex()
        "Airtel_Deposit" to """CASH DEPOSIT of UGX ([\d,]+) from\s+([A-Za-z ]+). Bal UGX ([\d,]+)\. TID:(\d+).+Date (\d{2}-[A-Za-z]+)""".toRegex(),
        "Airtel_Deposit" to """CASH DEPOSIT of UGX ([\d,]+) from  ([A-Za-z ]+)\. Bal UGX ([\d,]+)\. TID (\d+)\. (\d{1,2}-[A-Za-z]+-\d{4}) \d{2}:\d{2}""".toRegex(),
        "Airtel_InternalPayment" to """PAID UGX ([\d,]+) to ([A-Za-z ]+).*Charge UGX ([\d,]+), TID (\d+).*Bal UGX ([\d,]+) Date: (\d{2}-[A-Za-z]+-\d{4} \d{2}:\d{2})""".toRegex(),
//        "Airtel_CustomerPayment" to """RECEIVED UGX ([\d,]+) from (\d+), ([A-Z ]+).+Bal UGX ([\d,]+)\. TID: (\d+)""".toRegex(),
        "Airtel_CustomerPayment" to """RECEIVED\. TID (\d+)\. UGX ([\d,]+) from (\d+), ([A-Za-z ]+)\. Bal UGX ([\d,]+)\.""".toRegex(),
//        "Airtel_Remittance" to """SENT UGX ([\d,]+) to ([A-Za-z ]+) (\d+).+Fee UGX ([\d,]+).+Bal UGX ([\d,]+)\. TID: (\d+).+Date: (\d{2}-[A-Za-z]+-\d{4} \d{2}:\d{2})""".toRegex(),
        "Airtel_Remittance" to """SENT\.TID (\d+)\. UGX ([\d,]+) to ([A-Za-z ]+)  (\d+)\. Fee UGX ([\d,]+)\. Bal UGX ([\d,]+)\.""".toRegex(),
        "Airtel_Transfer" to """You have been debited UGX ([\d,]+)\. Fee UGX ([\d,]+)\. Bal UGX ([\d,]+)\. TID (\d+)""".toRegex(),
       "Airtel_Withdraw" to """Withdraw of UGX([\d,]+) with Agent ID: (\d+)\.Fee UGX ([\d,]+)\. Bal UGX ([\d,]+)\.TID: (\d+)\. Date (\d{2}-[A-Za-z]+-\d{4} \d{2}:\d{2})\.Tax UGX (\d+)\.https://bit\.ly/3ZgpiNw""".toRegex(),
       //Airtel TO Bank Regex
        "STANBIC_Transfer" to """PAID\.TID (\d+)\. UGX ([\d,]+) to STANBIC Charge UGX ([\d,]+)\. Bal UGX ([\d,]+)\. (\d{1,2}-[A-Za-z]+-\d{4} \d{2}:\d{2})""".toRegex(),
        // Airtel To Equity
        "EQUITY_Transfer" to """You have sent Amount: UGX ([\d,]+) to Bank Account: 1035203139887. Txn ID: (\d+)\. Bal UGX ([\d,]+)""".toRegex(),

        "Centenary_Transfer" to """SENT. TID (\d+). UGX ([\d,]+) to CENTENARY BANK on 3100106557. Fee UGX ([\d,]+) Balance UGX ([\d,]+) Date (\d{2}-[A-Za-z]+-\d{4} \d{2}:\d{2})\.""".toRegex(),

       // MTN
        "MTN_Remittance" to """You have sent UGX ([\d,]+) to ([A-Z ]+), (\d+) on (\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}), fee: (\d+).+New balance: (\d+). ID :(\d+)""".toRegex(),
        "MTN_Withdraw" to """You have withdrawn UGX ([\d,]+) on (\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2})\. Fee: UGX ([\d,]+), Tax: UGX ([\d,]+)\. New balance: UGX ([\d,.]+)""".toRegex(),
//        "MTN_CustomerPayment" to """You have received UGX ([\d,]+) from ([A-Za-z ]+), (\d+) on (\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}).+fee:(\d+).+New balance: UGX ([\d]+)\. ID: (\d+)""".toRegex(),
        "MTN_CustomerPayment" to """You have received UGX ([\d,]+) from ([A-Za-z ]+), (\d+) on (\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}).+fee:(\d+).+Reason:\s*(.*).+New balance: UGX ([\d]+)\. ID: (\d+)""".toRegex(),
//        You have received UGX 4000000 from JERSA NAKALUNGI, 256788444788 on 2024-08-14 11:58:10. fee:0. Reason: urban 1. New balance: UGX 8806971. ID: 27621554061.

//        "MTN_Deposit" to """You have deposited UGX ([\d,]+) from ([A-Z ]+) on (\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}). New balance: UGX ([\d,]+). ID: (\d+). Do NOT share your Mobile Money PIN.""".toRegex(),

        "MTN_Deposit" to """You have deposited UGX ([\d]+) from (.*?)(?:\.|\s)on (\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2})\. New balance: UGX ([\d]+). ID: (\d+). Do NOT share your Mobile Money PIN.""".toRegex(),



//        "MTN_WithdrawRequest" to """Y'ello\. You have requested a withdrawal of UGX ([\d,]+) from ([\w\s]+)\. Dial \*165# and select My Approvals to authorize the transaction\.The total fee is UGX ([\d,]+) inclusive of ([\d\.]+) percent tax\.Transaction ID (\d+)""".toRegex(),
        "MTN_Request" to """Y'ello\. You have requested a withdrawal of UGX ([\d,]+) from ([\w\s]+)\. Dial \*165# and select My Approvals to authorize the transaction\.The total fee is  UGX ([\d,]+) inclusive of ([\d\.]+) percent tax\.Transaction ID (\d+)""".toRegex(),
//        "MTN_BANK_TRANSFER" to """Y'ello\. You have transferred UGX UGX ([\d,]+) to ([\w\s]+)\. TX Charge  UGX (\d+)\. Your new balance: UGX UGX ([\d]+)\.\s+Transaction ID:(\d+)\.""".toRegex(),
//        "MTN_BANK_TRANSFER" to """Y'ello\. You have transferred UGX UGX ([\d,]+) to ([\w\s]+)\. TX Charge  UGX (\d+)\.\s+Your new balance:\s+UGX\s([\d]+)\.\s+Transaction ID:(\d+)\.""".toRegex(),

//        "MOMO_Payment" to """You have received ([\d,]+) UGX from ([A-Z ]+) \((\d+)\) on your mobile money account at (\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}).+Your new balance: ([\d,]+) UGX. Fee was ([\d,]+) UGX. Financial Transaction Id: (\d+).""".toRegex(),
//        "MOMO_Payment" to """You have received (\d+) UGX from ([A-Z ]+) \((\d+)\) on your mobile money account at (\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}).+Message from sender: Till:(\d+).+Your new balance: ([\d,]+) UGX. Fee was (\d+) UGX. Financial Transaction Id: (\d+).""".toRegex()
        "MOMO_Payment" to """You have received (\d+) UGX from ([A-Z ]+) \((\d+)\) on your mobile money account at (\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}).+Your new balance: (\d+) UGX.+ Fee was (\d+) UGX. Financial Transaction Id: (\d+).""".toRegex(),
       //MTN TO BANK
        "Bank_Transfer" to """You have transferred UGX UGX ([\d,]+\.\d{2}) to ([\w\s]+)\.""".toRegex(),

        //MOMO CODE
        "MOMO_CODE" to """<#> Y'ello. Please enter the following code :(\d+) to complete your login\. Be Careful\. NEVER Share this code\.""".toRegex(),


//        "BANK_TRANSFER" to """Y'ello\. You have transferred UGX UGX ([\d,]+) to ([A-Za-z ]+). TX Charge  UGX (\d+)\. Your new balance: UGX UGX ([\d,]+)\. Transaction ID:(\d+)\.""".toRegex(),
//        "MOMO_Payment" to """You have received (\d+) UGX from ([A-Z ]+) \((\d+)\) on your mobile money account at (\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}).+Your new balance: (\d+) UGX.+ Fee was (\d+) UGX. Financial Transaction Id: (\d+).""".toRegex()
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
                        "fee" to values.getOrNull(2),
                        "transactionId" to values.getOrNull(3),
                        "balance" to values.getOrNull(4),
                        "date" to values.getOrNull(5),
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
                        "amount" to values.getOrNull(1)!!,
                        "name" to values.getOrNull(2)!!,  // Adjust according to actual group index for each pattern
                        "phone_number" to values.getOrNull(3)!!,  // Adjust according to actual group index for each pattern
//                        "phone_number" to values.getOrNull(3)!!,  // Adjust according to actual group index for each pattern
//                                              "fee" to values.getOrNull(2),
                        "transactionId" to values.getOrNull(0),
                        "balance" to values.getOrNull(4),
//                        "date" to values.getOrNull(4),
//                        "transactionId" to values.getOrNull(4) // Adjust according to actual group index for each pattern
                    )
                }
                else if(type=="Airtel_Remittance"){
                    return mapOf(
                        "transactionType" to type.split("_")[1],
                        "telNetwork" to type.split("_")[0],
                        "amount" to values.getOrNull(1)!!,
                        "name" to values.getOrNull(2)!!,  // Adjust according to actual group index for each pattern
                        "phone_number" to values.getOrNull(3)!!,  // Adjust according to actual group index for each pattern
//                        "phone_number" to values.getOrNull(3)!!,  // Adjust according to actual group index for each pattern
                        "fee" to values.getOrNull(4),
                        "transactionId" to values.getOrNull(0),
                        "balance" to values.getOrNull(5),
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
                else if(type=="STANBIC_Transfer"){
                    return mapOf(
                        "transactionType" to type.split("_")[1],
                        "telNetwork" to type.split("_")[0],
                        "amount" to values.getOrNull(1)!!,
                        "name" to "Palli Airtel",  // Adjust according to actual group index for each pattern
//                        "phone_number" to values.getOrNull(2)!!,  // Adjust according to actual group index for each pattern
//                        "phone_number" to values.getOrNull(3)!!,  // Adjust according to actual group index for each pattern
                        "fee" to values.getOrNull(2),
                        "transactionId" to values.getOrNull(0),
                        "balance" to values.getOrNull(3),
                        "date" to values.getOrNull(4),

                    )
                }
                else if(type=="EQUITY_Transfer"){
                    return mapOf(
                        "transactionType" to type.split("_")[1],
                        "telNetwork" to type.split("_")[0],
                        "amount" to values.getOrNull(0)!!,
                        "name" to "Palli Airtel",  // Adjust according to actual group index for each pattern
//                        "phone_number" to values.getOrNull(2)!!,  // Adjust according to actual group index for each pattern
//                        "phone_number" to values.getOrNull(3)!!,  // Adjust according to actual group index for each pattern
                        "fee" to "No Fee",
                        "transactionId" to values.getOrNull(1),
                        "balance" to values.getOrNull(2),
                        "date" to getDate(),

                        )
                }
                else if(type=="Centenary_Transfer"){
                    return mapOf(
                        "transactionType" to type.split("_")[1],
                        "telNetwork" to type.split("_")[0],
                        "amount" to values.getOrNull(1)!!,
                        "name" to "Palli Airtel",  // Adjust according to actual group index for each pattern
//                        "phone_number" to values.getOrNull(2)!!,  // Adjust according to actual group index for each pattern
//                        "phone_number" to values.getOrNull(3)!!,  // Adjust according to actual group index for each pattern
                        "fee" to values.getOrNull(2),
                        "transactionId" to values.getOrNull(0),
                        "balance" to values.getOrNull(3),
                        "date" to values.getOrNull(4),

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
                else if(type=="MOMO_CODE"){
//                    Log.d("Type is", type)
                    return mapOf(
                        "transactionType" to "MoMo Code",
                        "telNetwork" to type.split("_")[0],
                        "code" to values.getOrNull(0),
//                        "transactionId" to values.getOrNull(4) // Adjust according to actual group index for each pattern
                    )
                }
// "MTN_Deposit" to """You have received UGX ([\d,]+) from ([A-Za-z ]+), (\d+) on (\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}).+fee:(\d+).+New balance: UGX ([\d,]+)\. ID: (\d+)""".toRegex(),

                else if(type=="MTN_CustomerPayment"){
                    return mapOf(
                        "transactionType" to type.split("_")[1],
                        "telNetwork" to type.split("_")[0],
                        "amount" to values.getOrNull(0)!!,
                        "name" to values.getOrNull(1)!!,  // Adjust according to actual group index for each pattern
                        "phone_number" to values.getOrNull(2)!!,  // Adjust according to actual group index for each pattern
//                        "phone_number" to values.getOrNull(3)!!,  // Adjust according to actual group index for each pattern

                        "transactionId" to values.getOrNull(7),
                        "reason" to values.getOrNull(5),
                        "fee" to values.getOrNull(4),
                        "balance" to values.getOrNull(6),
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
                else if(type=="MTN_STANBIC_TRANSFER"){
                    return mapOf(
                        "transactionType" to type.split("_")[2],
                        "telNetwork" to type.split("_")[1],
                        "amount" to values.getOrNull(0)!!,
                        "name" to "Palli MTN",
                        "fee" to values.getOrNull(2),
//
                        "balance" to values.getOrNull(3),

                        "transactionId" to values.getOrNull(4)
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

fun getDate():String{
    val currentDateTime = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("dd-MMMM-yyyy HH:mm")
    val dateTimeString = currentDateTime.format(formatter)

    return dateTimeString
}

