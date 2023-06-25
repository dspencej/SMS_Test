package com.example.sms_test;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
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

public class SMSActivity extends Activity {

    private static final int PERMISSION_REQUEST_RECEIVE_SMS = 123;
    private static final int PERMISSION_REQUEST_SEND_SMS = 456;

    private TextView headerTextView;
    private EditText phoneNumberEditText;
    private EditText messageEditText;
    private Button sendButton;
    private Button clearButton;

    private BroadcastReceiver smsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    if (pdus != null) {
                        StringBuilder headerBuilder = new StringBuilder();
                        for (Object pdu : pdus) {
                            SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);

                            // Extract header information
                            String originatingAddress = smsMessage.getOriginatingAddress();
                            String timestamp = formatTimestamp(smsMessage.getTimestampMillis());
                            String protocol = String.valueOf(smsMessage.getProtocolIdentifier());
                            String displayOriginatingAddress = smsMessage.getDisplayOriginatingAddress();
                            String messageBody = smsMessage.getMessageBody();

                            // Build the header information string
                            headerBuilder.append("Originating Address: ").append(originatingAddress).append("\n");
                            headerBuilder.append("Timestamp: ").append(timestamp).append("\n");
                            headerBuilder.append("Protocol: ").append(protocol).append("\n");
                            headerBuilder.append("Display Originating Address: ").append(displayOriginatingAddress).append("\n");
                            headerBuilder.append("Message Body: ").append(messageBody).append("\n\n");
                        }

                        // Append the received header information to the existing text view content
                        String existingContent = headerTextView.getText().toString();
                        String newContent = headerBuilder.toString() + existingContent;
                        headerTextView.setText(newContent);
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
        sendButton = findViewById(R.id.send_button);
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

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = phoneNumberEditText.getText().toString();
                String message = messageEditText.getText().toString();
                if (!phoneNumber.isEmpty() && !message.isEmpty()) {
                    sendSMS(phoneNumber, message);
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

    private void sendSMS(String phoneNumber, String message) {
        try {
            SmsManager smsManager = SmsManager.getDefault();

            // Divide the message into parts if it exceeds the maximum SMS message length
            ArrayList<String> parts = smsManager.divideMessage(message);
            int numParts = parts.size();
            ArrayList<PendingIntent> sentIntents = new ArrayList<>();
            ArrayList<PendingIntent> deliveryIntents = new ArrayList<>();

            for (int i = 0; i < numParts; i++) {
                sentIntents.add(null);
                deliveryIntents.add(null);
            }

            smsManager.sendMultipartTextMessage(phoneNumber, null, parts, sentIntents, deliveryIntents);

            // Build the outgoing header information string
            StringBuilder headerBuilder = new StringBuilder();
            headerBuilder.append("Outgoing Message\n");
            headerBuilder.append("Destination Address: ").append(phoneNumber).append("\n");
            headerBuilder.append("Timestamp: ").append(getCurrentTimestamp()).append("\n");
            headerBuilder.append("Message Body: ").append(message).append("\n\n");

            // Append the outgoing header information to the existing text view content
            String existingContent = headerTextView.getText().toString();
            String newContent = headerBuilder.toString() + existingContent;
            headerTextView.setText(newContent);

            Toast.makeText(this, "SMS sent successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to send SMS", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private String formatTimestamp(long timestampMillis) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date(timestampMillis);
        return dateFormat.format(date);
    }

    private String getCurrentTimestamp() {
        return formatTimestamp(System.currentTimeMillis());
    }
}
