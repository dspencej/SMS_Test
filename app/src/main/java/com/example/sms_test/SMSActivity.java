package com.example.sms_test;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import android.app.PendingIntent;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class SMSActivity extends Activity {

    private static final int PERMISSION_REQUEST_RECEIVE_SMS = 123;
    private static final int PERMISSION_REQUEST_SEND_SMS = 456;

    private TextView headerTextView;
    private EditText phoneNumberEditText;
    private EditText messageEditText;
    private Button sendButton;
    private Button sendRegularButton;
    private Button sendCovertButton;
    private Button clearButton;

    // Sample pre-shared key (for demo purposes, replace this with the actual secret key)
    private static final String SECRET_KEY = "ThisIsASecretKey123";


    private BroadcastReceiver smsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    if (pdus != null) {
                        for (Object pdu : pdus) {
                            SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                            String messageBody = smsMessage.getMessageBody();
                            handleReceivedSms(messageBody);
                        }
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);

        headerTextView = findViewById(R.id.header_text_view);
        phoneNumberEditText = findViewById(R.id.phone_number_edit_text);
        messageEditText = findViewById(R.id.message_edit_text);
        sendRegularButton = findViewById(R.id.send_regular_button);
        sendCovertButton = findViewById(R.id.send_covert_button);
        clearButton = findViewById(R.id.clear_button);

        // Check and request permission to receive SMS
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECEIVE_SMS},
                    PERMISSION_REQUEST_RECEIVE_SMS);
        } else {
            // Permission already granted, register the receiver
            registerSmsReceiver();
        }

        // Check and request permission to send SMS
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    PERMISSION_REQUEST_SEND_SMS);
        }

        sendRegularButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = phoneNumberEditText.getText().toString();
                String message = messageEditText.getText().toString();
                if (!phoneNumber.isEmpty() && !message.isEmpty()) {
                    sendRegularSMS(phoneNumber, message);
                } else {
                    Toast.makeText(SMSActivity.this, "Please enter a phone number and message", Toast.LENGTH_SHORT).show();
                }
            }
        });

        sendCovertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = phoneNumberEditText.getText().toString();
                String message = messageEditText.getText().toString();
                if (!phoneNumber.isEmpty() && !message.isEmpty()) {
                    sendCovertSMS(phoneNumber, message);
                } else {
                    Toast.makeText(SMSActivity.this, "Please enter a phone number and message", Toast.LENGTH_SHORT).show();
                }
            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                headerTextView.setText("");
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_RECEIVE_SMS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, register the receiver
                    registerSmsReceiver();
                } else {
                    Toast.makeText(this, "Receive SMS permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
            case PERMISSION_REQUEST_SEND_SMS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Send SMS permission granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Send SMS permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void registerSmsReceiver() {
        IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(smsReceiver, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(smsReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(smsReceiver);
    }


    private void sendRegularSMS(String phoneNumber, String message) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(this, "Regular SMS sent successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to send Regular SMS", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void sendCovertSMS(String phoneNumber, String message) {
        try {
            // Step 1: Encrypt the message with the pre-shared key
            byte[] encryptedMessage = encryptMessage(message, SECRET_KEY);

            // Step 2: Encode the encrypted message in Base64
            String base64EncodedMessage = Base64.encodeToString(encryptedMessage, Base64.DEFAULT);

            // Step 3: URL-encode the Base64 encoded message
            String payload = Uri.encode(base64EncodedMessage);

            // Step 4: Craft a fake URL domain
            String domain = "http://example.com/";

            // Step 5: Append the payload to the fake URL domain to create the full URL
            String fullUrl = domain + payload;

            // Step 6: Shorten the URL (You need to implement your URL shortening logic here)
            String shortenedUrl = shortenUrl(fullUrl);

            // Step 7: Send the shortened URL as the message body of the SMS
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, shortenedUrl, null, null);

            Toast.makeText(this, "Covert SMS sent successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to send Covert SMS", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void handleReceivedSms(String messageBody) {
        try {
            // Step 1: Attempt to extract the shortened URL from the message body
            String shortenedUrl = extractShortenedUrl(messageBody);

            if (shortenedUrl == null) {
                // Failure indicates it's not a covert message, ignore the message
                // Display the decrypted message to the user
                headerTextView.setText("Received Message: " + messageBody);
                return;
            }

            // Step 2: Attempt to restore the shortened URL to the full URL
            String fullUrl = restoreShortenedUrl(shortenedUrl);

            if (fullUrl == null) {
                // Failure indicates it's not a covert message or malformed covert message, ignore the message
                return;
            }

            // Step 3: Attempt to extract the payload from the full URL
            String payload = extractPayloadFromUrl(fullUrl);

            if (payload == null) {
                // Failure indicates it's not a covert message or malformed covert message, ignore the message
                return;
            }

            // Step 4: Decode the payload (Base64 decoding)
            byte[] decodedPayload = Base64.decode(payload, Base64.DEFAULT);

            // Step 5: Decrypt the decoded payload with the pre-shared key
            String decryptedMessage = decryptMessage(decodedPayload, SECRET_KEY);

            // Display the decrypted message to the user
            headerTextView.setText("Decrypted Message: " + decryptedMessage);

        } catch (Exception e) {
            Log.e("SMSActivity", "Failed to handle received SMS", e);
        }
    }


    // The following methods should be updated
    private String shortenUrl(String url) {
        // TODO
        // Implement the URL shortening logic here
        // Return the shortened URL
        return "http://short.url/" + new SecureRandom().nextInt(1000);
    }

    private String extractShortenedUrl(String messageBody) {
        // TODO
        // Implement the logic to extract the shortened URL from the message body
        // Return null if the message is not a covert message
        return "http://short.url/abc123";
    }

    private String restoreShortenedUrl(String shortenedUrl) {
        // TODO
        // Implement the logic to restore the shortened URL to the full URL
        // Return null if the message is not a covert message or if the shortened URL is invalid
        return "http://example.com/encoded_payload_here";
    }

    private String extractPayloadFromUrl(String fullUrl) {
        // TODO
        // Implement the logic to extract the payload from the full URL
        // Return null if the message is not a covert message or if the full URL is invalid
        return "encoded_payload_here";
    }

    private byte[] encryptMessage(String message, String key) {
        // TODO
        // Implement the encryption algorithm here using the provided key
        // Return the encrypted data
        return message.getBytes();
    }

    private String decryptMessage(byte[] data, String key) {
        // TODO
        // Implement the decryption algorithm here using the provided key
        // Return the decrypted message as a string
        return new String(data);
    }
}