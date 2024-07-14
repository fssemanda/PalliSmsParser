package com.mypalli.pallismsparser;

import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {28})
public class SmsReceiverTest {

    private Context context;
    private SmsReceiver smsReceiver;

    @Before
    public void setUp() {
        context = Robolectric.buildActivity(Activity.class).get();
        smsReceiver = new SmsReceiver();
    }

    @Test
    public void testSmsReceivedAction() {
        Intent intent = new Intent(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
        Telephony.Sms.Intents.getMessagesFromIntent(intent);
        smsReceiver.onReceive(context, intent);

        // Assert that the LocalBroadcastManager sent the intent
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);
        Intent expectedIntent = new Intent("com.mypalli.pallismsparser.SMS_RECEIVED");
        expectedIntent.putExtra("sms_data", "expected data");
        localBroadcastManager.sendBroadcast(expectedIntent);

        // Add assertions to verify the expected behavior
    }

    @Test
    public void testExtractTransactionDetails_Airtel_Deposit() {
        String messageBody = "CASH DEPOSIT of UGX 500,000 from JOHN DOE. Bal UGX 1,000,000. TID:12345. Date 22-May";
        String originatingAddress = "Airtel";

        Map<String, String> result = SmsReceiver.extractTransactionDetails(messageBody, originatingAddress);

        assertEquals("Deposit", result.get("transactionType"));
        assertEquals("Airtel", result.get("telNetwork"));
        assertEquals("500,000", result.get("amount"));
        assertEquals("JOHN DOE", result.get("name"));
        assertEquals("Airtel", result.get("phone_number"));
        assertEquals("22-May", result.get("date"));
        assertEquals("12345", result.get("transactionId"));
        assertEquals("1,000,000", result.get("balance"));
    }

    @Test
    public void testExtractTransactionDetails_InvalidMessage() {
        String messageBody = "Invalid message content";
        String originatingAddress = "Airtel";

        Map<String, String> result = SmsReceiver.extractTransactionDetails(messageBody, originatingAddress);

        assertNull(result);
    }

    // Add more test cases for other message types
}
