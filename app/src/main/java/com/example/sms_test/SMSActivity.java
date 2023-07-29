package com.example.sms_test;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class SMSActivity extends Activity {

    private static final int PERMISSION_REQUEST_RECEIVE_SMS = 123;
    private static final int PERMISSION_REQUEST_SEND_SMS = 456;

    private TextView receivedMessageTextView;
    private TextView decryptedMessageTextView;
    private EditText phoneNumberEditText;
    private EditText messageEditText;

    // Sample pre-shared key (for demo purposes, replace this with the actual secret key)
    private static final String SECRET_KEY = "ThisIsASecretKey123";

    /**
     * BroadcastReceiver for handling incoming SMS messages.
     */
    private final BroadcastReceiver smsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Check if the received intent corresponds to an SMS message
            if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
                // Extract the SMS message from the received intent's extras
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    // Extract the Protocol Data Units (PDUs) from the SMS message
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    if (pdus != null) {
                        // Loop through all PDUs to handle each SMS message
                        for (Object pdu : pdus) {
                            // Create an SmsMessage object from the PDU
                            SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);

                            // Extract the message body from the SmsMessage
                            String messageBody = smsMessage.getMessageBody();

                            // Pass the received message body to the handleReceivedSms function
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

        receivedMessageTextView = findViewById(R.id.received_message_text_view);
        decryptedMessageTextView = findViewById(R.id.decrypted_message_text_view);
        phoneNumberEditText = findViewById(R.id.phone_number_edit_text);
        messageEditText = findViewById(R.id.message_edit_text);
        Button sendRegularButton = findViewById(R.id.send_regular_button);
        Button sendCovertButton = findViewById(R.id.send_covert_button);
        Button clearButton = findViewById(R.id.clear_button);

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

        sendRegularButton.setOnClickListener(v -> {
            String phoneNumber = phoneNumberEditText.getText().toString();
            String message = messageEditText.getText().toString();
            if (!phoneNumber.isEmpty() && !message.isEmpty()) {
                sendRegularSMS(phoneNumber, message);
            } else {
                Toast.makeText(SMSActivity.this, "Please enter a phone number and message", Toast.LENGTH_SHORT).show();
            }
        });

        sendCovertButton.setOnClickListener(v -> {
            String phoneNumber = phoneNumberEditText.getText().toString();
            String message = messageEditText.getText().toString();
            if (!phoneNumber.isEmpty() && !message.isEmpty()) {
                sendCovertSMS(phoneNumber, message);
            } else {
                Toast.makeText(SMSActivity.this, "Please enter a phone number and message", Toast.LENGTH_SHORT).show();
            }
        });

        clearButton.setOnClickListener(v -> {
            receivedMessageTextView.setText("");
            decryptedMessageTextView.setText("");
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


    /**
     * Sends a regular SMS message to the specified phone number.
     *
     * @param phoneNumber The destination phone number to which the SMS will be sent.
     * @param message     The message content to be sent.
     */
    private void sendRegularSMS(String phoneNumber, String message) {
        try {
            // Get the default instance of SmsManager
            SmsManager smsManager = SmsManager.getDefault();

            // Send the SMS with the specified phone number, message content, and other parameters as null
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);

            // Display a success toast message to the user
            Toast.makeText(this, "Regular SMS sent successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            // Handle any exceptions that occur during the SMS sending process

            // Display an error toast message to the user
            Toast.makeText(this, "Failed to send Regular SMS", Toast.LENGTH_SHORT).show();

            // Print the stack trace to the log for debugging and troubleshooting
            e.printStackTrace();
        }
    }


    /**
     * Sends a covert SMS message to the specified phone number.
     *
     * @param phoneNumber The destination phone number to which the covert SMS will be sent.
     * @param message     The message content to be sent covertly.
     */
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
            // smsManager.sendTextMessage(phoneNumber, null, shortenedUrl, null, null);
            smsManager.sendTextMessage(phoneNumber, null, fullUrl, null, null);
            Toast.makeText(this, "Covert SMS sent successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            // Handle any exceptions that occur during the covert SMS sending process

            // Display an error toast message to the user to indicate that the covert SMS sending failed
            Toast.makeText(this, "Failed to send Covert SMS", Toast.LENGTH_SHORT).show();

            // Print the stack trace to the log for debugging and troubleshooting
            e.printStackTrace();
        }
    }

    /**
     * Handle the received SMS message containing a covert URL.
     *
     * @param messageBody The message body received in the SMS.
     */
    private void handleReceivedSms(String messageBody) {
        try {
            // Step 1: Attempt to extract the shortened URL from the message body
            // TODO

            // Step 2: Attempt to restore the shortened URL to the full URL
            String fullUrl = restoreShortenedUrl(messageBody);
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

            // Display the message to the user using resource strings with placeholders
            receivedMessageTextView.setText(messageBody);
            decryptedMessageTextView.setText(decryptedMessage);

        } catch (Exception e) {
            Log.e("SMSActivity", "Failed to handle received SMS", e);
        }
    }


    /**
     * Shorten the given URL using a URL shortening service.
     *
     * @param url The URL to be shortened.
     * @return The shortened URL.
     */
    private String shortenUrl(String url) {
        // TODO: Implement the URL shortening logic here

        // Generate a random number between 0 and 999 (inclusive) to create a unique-looking URL
        int randomSuffix = new SecureRandom().nextInt(1000);

        // Return the shortened URL with the random suffix
        return "http://short.url/" + randomSuffix;
    }


    /**
     * Restore the shortened URL to the full URL.
     *
     * @param shortenedUrl The shortened URL received in the SMS message.
     * @return The full URL if successfully restored, null if the message is not a covert message or if the shortened URL is invalid.
     */
    private String restoreShortenedUrl(String shortenedUrl) {
        // TODO: Implement the logic to restore the shortened URL to the full URL

        // For this example, we assume the input shortenedUrl is already the full URL (no unshortening logic implemented)
        return shortenedUrl;
    }

    /**
     * Extracts the payload from a full URL.
     *
     * @param url The full URL from which to extract the payload.
     * @return The extracted payload as a string, or null if there was an error during extraction.
     */
    private String extractPayloadFromUrl(String url) {
        try {
            // Get the index of the first occurrence of ".com/"
            int startIndex = url.indexOf(".com/") + 5;

            // Extract everything after ".com/" as the payload
            String payload = url.substring(startIndex);

            // URL decode the payload to handle any URL-encoded characters
            return URLDecoder.decode(payload, "UTF-8");
        } catch (Exception e) {
            // Handle any exceptions that occur during the extraction process
            Log.e("URL Extraction Error", e.getMessage(), e);

            // Display an error message to the user using showToast function
            showToast("URL Extraction Error: " + e.getMessage());

            // Return null to indicate extraction failure
            return null;
        }
    }

    /**
     * Generates a valid AES key from the provided key string by hashing it with SHA-256.
     *
     * @param key The original key as a string.
     * @return A valid AES key as a byte array with a length of 16, 24, or 32 bytes.
     *         Returns null if there is an error during the key generation process.
     */
    private byte[] getValidAESKey(String key) {
        try {
            // Step 1: Convert the key into a byte array
            byte[] keyData = key.getBytes(StandardCharsets.UTF_8);

            // Step 2: Create a MessageDigest instance for the SHA-256 algorithm
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // Step 3: Hash the key data using SHA-256 to get a 256-bit hash
            byte[] hashedKey = md.digest(keyData);

            // Step 4: Return the first 16 bytes of the hashed key
            // to obtain a valid AES key of length 128 bits respectively.
            byte[] finalKey = Arrays.copyOf(hashedKey, 16); // AES-128 key (16 bytes)

            return finalKey;
        } catch (Exception e) {
            Log.e("Key Generation Error", e.getMessage(), e);
            e.printStackTrace();
            return null; // Return null in case of hashing algorithm not found
        }
    }

    /**
     * Encrypts the message using the provided key using the AES encryption algorithm.
     *
     * @param message The message to be encrypted.
     * @param key     The encryption key (should be a string representation of the key).
     * @return The encrypted data as a byte array.
     */
    private byte[] encryptMessage(String message, String key) {
        try {
            // Get the valid AES key
            byte[] validKey = getValidAESKey(key);

            if (validKey != null) {
                // Convert the valid key into a SecretKeySpec object for AES encryption
                SecretKeySpec secretKeySpec = new SecretKeySpec(validKey, "AES");

                // Initialize the Cipher with AES/CBC/PKCS5Padding
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

                // Generate an Initialization Vector (IV)
                byte[] iv = new byte[cipher.getBlockSize()];
                SecureRandom secureRandom = new SecureRandom();
                secureRandom.nextBytes(iv);

                // Initialize the Cipher in ENCRYPT_MODE using the provided key and IV
                cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));

                // Encrypt the message by converting the message string to bytes and performing the encryption
                byte[] encryptedData = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));

                // Combine the IV and encrypted data into a single byte array
                byte[] combinedData = new byte[iv.length + encryptedData.length];
                System.arraycopy(iv, 0, combinedData, 0, iv.length);
                System.arraycopy(encryptedData, 0, combinedData, iv.length, encryptedData.length);

                return combinedData;
            } else {
                return null; // Return null to indicate encryption failure
            }
        } catch (Exception e) {
            Log.e("Encryption Error", e.getMessage(), e);
            e.printStackTrace();
            return null; // Return null to indicate encryption failure
        }

    }

    /**
     * Decrypts the encrypted data using the provided key using the AES decryption algorithm.
     *
     * @param data The encrypted data as a byte array.
     * @param key  The decryption key (should be a string representation of the key).
     * @return The decrypted message as a string.
     */
    private String decryptMessage(byte[] data, String key) {
        try {
            // Get the valid AES key
            byte[] validKey = getValidAESKey(key);

            if (validKey != null) {
                // Convert the valid key into a SecretKeySpec object for AES encryption
                SecretKeySpec secretKeySpec = new SecretKeySpec(validKey, "AES");

                // Initialize the Cipher with AES/CBC/PKCS5Padding
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

                // Separate the IV and encrypted data from the combined data byte array
                byte[] iv = Arrays.copyOfRange(data, 0, cipher.getBlockSize());
                byte[] encryptedData = Arrays.copyOfRange(data, cipher.getBlockSize(), data.length);

                // Initialize the Cipher in DECRYPT_MODE using the provided key and IV
                cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));

                // Decrypt the data by performing the decryption on the encrypted byte array
                byte[] decryptedData = cipher.doFinal(encryptedData);

                // Convert the decrypted byte array to a string and return it
                return new String(decryptedData, StandardCharsets.UTF_8);
            } else {
                return null; // Return null to indicate encryption failure
            }
        } catch (Exception e) {
            Log.e("Decryption Error", e.getMessage(), e);
            // Show the exception details in a toast message
            showToast("Decryption Error: " + e.getMessage());
            e.printStackTrace();
            return null; // Return null to indicate decryption failure
        }
    }
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}