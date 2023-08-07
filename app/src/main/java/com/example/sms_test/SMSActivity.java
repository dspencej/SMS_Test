package com.example.sms_test;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;

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
     * AsyncTask to shorten the URL and send using a background thread.
     */
    @SuppressLint("StaticFieldLeak")
    private class SendShortenUrlTask extends AsyncTask<String, Void, String> {
        private final String phoneNumber;
        private final String domain;

        public SendShortenUrlTask(String phoneNumber, String domain) {
            this.phoneNumber = phoneNumber;
            this.domain = domain;
        }

        @Override
        protected String doInBackground(String... params) {
            String fullUrl = params[0];
            String payload = params[1];
            return shortenUrl(fullUrl, payload);
        }

        @Override
        protected void onPostExecute(String shortenedUrl) {
            if (shortenedUrl != null) {
                // Create the custom spam message
                SmsManager smsManager = SmsManager.getDefault();
                String customMessage = createCustomMessage(domain, shortenedUrl); // Create custom message based on the domain
                smsManager.sendTextMessage(phoneNumber, null, customMessage, null, null);
                Toast.makeText(SMSActivity.this, "Covert SMS sent successfully", Toast.LENGTH_SHORT).show();
            } else {
                // Handle the failure case if needed
                Toast.makeText(SMSActivity.this, "Failed to shorten URL", Toast.LENGTH_SHORT).show();
            }
        }
        private String createCustomMessage(String domain, String shortenedUrl) {
            // Customize the message based on the selected domain
            switch (domain) {
                case "http://google.com/":
                    return "Your shipment could not be delivered, due to an unpaid duty fee. To reschedule a delivery date, continue now: " +
                            shortenedUrl + "\n\nRegards,\nGoogle Delivery";
                case "http://amazon.com/":
                    return "Your package is delayed and requires a small shipping fee. Proceed here to make the payment and reschedule delivery: " +
                            shortenedUrl + "\n\nRegards,\nAmazon Support";
                case "http://facebook.com/":
                    return "Important update: Your Facebook account requires verification. Click the link to verify your account: " +
                            shortenedUrl + "\n\nRegards,\nFacebook Team";
                case "http://apple.com/":
                    return "Your Apple ID has been locked for security reasons. Please click the link to verify your account and unlock it: " +
                            shortenedUrl + "\n\nRegards,\nApple Support";
                case "http://microsoft.com/":
                    return "Microsoft Account Security Alert: Unusual sign-in activity detected. Click the link to secure your account: " +
                            shortenedUrl + "\n\nRegards,\nMicrosoft Support";
                case "http://youtube.com/":
                    return "You have won a special prize from YouTube! Click the link to claim your reward: " +
                            shortenedUrl + "\n\nRegards,\nYouTube Team";
                case "http://netflix.com/":
                    return "Your Netflix subscription will expire soon. Renew your subscription now to continue enjoying uninterrupted streaming: " +
                            shortenedUrl + "\n\nRegards,\nNetflix Support";
                case "http://instagram.com/":
                    return "Your Instagram account has been flagged for suspicious activity. Verify your account to prevent it from being disabled: " +
                            shortenedUrl + "\n\nRegards,\nInstagram Team";
                case "http://twitter.com/":
                    return "Important security update for your Twitter account. Click the link to update your account settings: " +
                            shortenedUrl + "\n\nRegards,\nTwitter Support";
                case "http://linkedin.com/":
                    return "LinkedIn Account Alert: Unusual login attempt detected. Click the link to secure your account: " +
                            shortenedUrl + "\n\nRegards,\nLinkedIn Support";
                case "http://wikipedia.org/":
                    return "Your help is needed to support Wikipedia! Click the link to make a donation and contribute to knowledge sharing: " +
                            shortenedUrl + "\n\nRegards,\nWikipedia Team";
                case "http://yahoo.com/":
                    return "Your Yahoo account has been selected for a special offer. Click the link to claim your reward: " +
                            shortenedUrl + "\n\nRegards,\nYahoo Promotions";
                case "http://ebay.com/":
                    return "Congratulations! You won an eBay gift card. Click the link to redeem your prize: " +
                            shortenedUrl + "\n\nRegards,\neBay Rewards";
                case "http://reddit.com/":
                    return "Your Reddit account has received a new message. Click the link to check your inbox: " +
                            shortenedUrl + "\n\nRegards,\nReddit Notifications";
                case "http://cnn.com/":
                    return "Breaking News: Click the link to read the full story on CNN's website: " +
                            shortenedUrl + "\n\nRegards,\nCNN News";
                case "http://bbc.com/":
                    return "BBC News Alert: Important updates are available. Click the link to read more: " +
                            shortenedUrl + "\n\nRegards,\nBBC News";
                case "http://spotify.com/":
                    return "Exclusive offer from Spotify Premium! Click the link to claim your free trial: " +
                            shortenedUrl + "\n\nRegards,\nSpotify Team";
                case "http://github.com/":
                    return "You've been invited to collaborate on a GitHub project. Click the link to accept the invitation: " +
                            shortenedUrl + "\n\nRegards,\nGitHub Collaboration";
                case "http://stackoverflow.com/":
                    return "A new answer is posted to your Stack Overflow question. Click the link to view the answer: " +
                            shortenedUrl + "\n\nRegards,\nStack Overflow";
                case "http://wordpress.com/":
                    return "Your WordPress blog has a new comment. Click the link to view and reply to the comment: " +
                            shortenedUrl + "\n\nRegards,\nWordPress Blog";
                case "http://pinterest.com/":
                    return "Your Pinterest pin is getting popular! Click the link to see how many people saved it: " +
                            shortenedUrl + "\n\nRegards,\nPinterest Insights";
                case "http://walmart.com/":
                    return "Special offer from Walmart! Click the link to get a discount on your next purchase: " +
                            shortenedUrl + "\n\nRegards,\nWalmart Deals";
                case "http://bing.com/":
                    return "Bing Rewards: Click the link to claim your reward points and redeem exciting prizes: " +
                            shortenedUrl + "\n\nRegards,\nBing Rewards";
                case "http://nytimes.com/":
                    return "Stay updated with the latest news! Click the link to read The New York Times articles: " +
                            shortenedUrl + "\n\nRegards,\nThe New York Times";
                case "http://hulu.com/":
                    return "Don't miss the latest episodes on Hulu! Click the link to watch your favorite shows: " +
                            shortenedUrl + "\n\nRegards,\nHulu Streaming";
                case "http://paypal.com/":
                    return "Your PayPal account has been accessed from a new device. Click the link to secure your account: " +
                            shortenedUrl + "\n\nRegards,\nPayPal Security";
                case "http://adobe.com/":
                    return "Adobe Creative Cloud: Click the link to explore new features and updates for your tools: " +
                            shortenedUrl + "\n\nRegards,\nAdobe Creative Cloud";
                case "http://twitch.tv/":
                    return "Your favorite streamer is live on Twitch! Click the link to join the stream: " +
                            shortenedUrl + "\n\nRegards,\nTwitch Live";
                case "http://msn.com/":
                    return "MSN Daily Briefing: Click the link to read the top news stories of the day: " +
                            shortenedUrl + "\n\nRegards,\nMSN News";
                case "http://imdb.com/":
                    return "IMDb: Click the link to check out the latest movie trailers and reviews: " +
                            shortenedUrl + "\n\nRegards,\nIMDb Movie";
                case "http://wordpress.org/":
                    return "WordPress Plugin Update: Click the link to update your WordPress plugins for improved functionality: " +
                            shortenedUrl + "\n\nRegards,\nWordPress Updates";
                case "http://espn.com/":
                    return "ESPN Score Alert: Click the link to see the latest scores and game highlights: " +
                            shortenedUrl + "\n\nRegards,\nESPN Sports";
                case "http://tumblr.com/":
                    return "Your Tumblr blog has a new follower. Click the link to see their blog: " +
                            shortenedUrl + "\n\nRegards,\nTumblr Follower";
                case "http://microsoftonline.com/":
                    return "Microsoft Office: Click the link to access your online documents and collaboration tools: " +
                            shortenedUrl + "\n\nRegards,\nMicrosoft Office Online";
                case "http://whatsapp.com/":
                    return "You have a new message on WhatsApp. Click the link to open the chat: " +
                            shortenedUrl + "\n\nRegards,\nWhatsApp Messaging";
                case "http://salesforce.com/":
                    return "Salesforce CRM Update: Click the link to check your latest sales and leads data: " +
                            shortenedUrl + "\n\nRegards,\nSalesforce CRM";
                case "http://blogger.com/":
                    return "Blogger: Click the link to create a new blog post and share your thoughts: " +
                            shortenedUrl + "\n\nRegards,\nBlogger Platform";
                case "http://buzzfeed.com/":
                    return "Find out what's trending on BuzzFeed! Click the link to discover new stories: " +
                            shortenedUrl + "\n\nRegards,\nBuzzFeed Trending";
                case "http://tiktok.com/":
                    return "TikTok Video Alert: Click the link to watch the latest viral videos: " +
                            shortenedUrl + "\n\nRegards,\nTikTok Entertainment";
                case "http://soundcloud.com/":
                    return "Your favorite artist just released a new track on SoundCloud. Click the link to listen: " +
                            shortenedUrl + "\n\nRegards,\nSoundCloud Music";
                case "http://craigslist.org/":
                    return "Craigslist: Click the link to view the latest listings in your area: " +
                            shortenedUrl + "\n\nRegards,\nCraigslist Classifieds";
                case "http://bbc.co.uk/":
                    return "BBC News Update: Click the link to read the latest headlines and stories: " +
                            shortenedUrl + "\n\nRegards,\nBBC News";
                case "http://mozilla.org/":
                    return "Mozilla Firefox Update: Click the link to download the latest version of Firefox: " +
                            shortenedUrl + "\n\nRegards,\nMozilla Firefox";
                case "http://slack.com/":
                    return "New messages in your Slack workspace. Click the link to view and respond: " +
                            shortenedUrl + "\n\nRegards,\nSlack Messaging";
                case "http://chase.com/":
                    return "Chase Online Banking: Click the link to log in and manage your finances: " +
                            shortenedUrl + "\n\nRegards,\nChase Bank";
                default:
                    // Default message for unknown domains
                    return "Important message from " + domain + ": " + shortenedUrl;
            }
        }

    }

    /**
     * AsyncTask to receive the shortened URL using a background thread.
     */
    @SuppressLint("StaticFieldLeak")
    private class RecvShortenUrlTask extends AsyncTask<String, Void, String> {
        private final String messageBody;

        public RecvShortenUrlTask(String messageBody) {
            this.messageBody = messageBody;
        }

        @Override
        protected String doInBackground(String... params) {
            String shortenedUrl = params[0];
            return requestShortenedUrl(shortenedUrl);
        }

        @Override
        protected void onPostExecute(String response) {
            if (response == null) {
                // Failure indicates it's not a covert message or malformed covert message, ignore the message
                return;
            }

            String payload = extractPayloadFromUrl(response);
            byte[] decodedPayload;
            // Step 4: Decode the payload (Base64 decoding)
            try {
                Log.d("Base64 Encoded:",payload);
                decodedPayload = Base64.decode(payload, Base64.DEFAULT);
                Log.d("Base64 Decoded:", Arrays.toString(decodedPayload));
                // Step 5: Decrypt the decoded payload with the pre-shared key
                new DecryptMessageTask(messageBody).execute(decodedPayload);
                String decryptedMessage = decryptMessage(decodedPayload, SECRET_KEY);

                // Display the message to the user using resource strings with placeholders
                receivedMessageTextView.setText(messageBody);
                decryptedMessageTextView.setText(decryptedMessage);
            } catch (Exception e)   {
                // Handle any exceptions that occur during the get request
                Log.e("Decode/Decrypt Error", e.getMessage(), e);
            }

        }
    }
    /**
     * AsyncTask to encrypt the payload.
     */
    @SuppressLint("StaticFieldLeak")
    private class EncryptMessageTask extends AsyncTask<String, Void, byte[]> {
        private final String phoneNumber;

        public EncryptMessageTask(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        @Override
        protected byte[] doInBackground(String... params) {
            String message = params[0];
            return encryptMessage(message, SECRET_KEY);
        }

        @Override
        protected void onPostExecute(byte[] encryptedMessage) {
            // Step 2: Encode the encrypted message in Base64
            Log.d("Base64 Decoded:", Arrays.toString(encryptedMessage));
            String base64EncodedMessage = Base64.encodeToString(encryptedMessage, Base64.DEFAULT);
            Log.d("Base64 Encoded:",base64EncodedMessage);
            // Step 3: URL-encode the Base64 encoded message using URLEncoder
            String payload;
            try {
                payload = URLEncoder.encode(base64EncodedMessage, "UTF-8");
            } catch (Exception e) {
                // Handle encoding exception if needed
                e.printStackTrace();
                return;
            }

            // Step 4: Get a random domain
            String randomDomain = getRandomDomain();

            // Step 5: Generate the full URL by appending the payload to the selected domain
            String fullUrl = randomDomain + payload;

            // Steps 6 and 7: Shorten the URL using AsyncTask and send the SMS
            new SendShortenUrlTask(phoneNumber, randomDomain).execute(fullUrl, payload);
        }
    }
    /**
     * AsyncTask to decrypt the payload.
     */
    @SuppressLint("StaticFieldLeak")
    private class DecryptMessageTask extends AsyncTask<byte[], Void, String> {
        private final String messageBody;

        public DecryptMessageTask(String messageBody) {
            this.messageBody = messageBody;
        }
        @Override
        protected String doInBackground(byte[]... params) {
            byte[] encryptedData = params[0];
            return decryptMessage(encryptedData, SECRET_KEY);
        }

        @Override
        protected void onPostExecute(String decryptedMessage) {

            // Display the message to the user using resource strings with placeholders
            receivedMessageTextView.setText(messageBody);
            decryptedMessageTextView.setText(decryptedMessage);
        }
    }

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
     * Returns a randomly selected well-known domain name as a string.
     *
     * @return The randomly selected domain name.
     */
    private String getRandomDomain() {
        // List of well-known domain names
        String[] DOMAIN_NAMES = {
                "http://google.com/",
                "http://amazon.com/",
                "http://facebook.com/",
                "http://apple.com/",
                "http://microsoft.com/",
                "http://youtube.com/",
                "http://netflix.com/",
                "http://instagram.com/",
                "http://twitter.com/",
                "http://linkedin.com/",
                "http://wikipedia.org/",
                "http://yahoo.com/",
                "http://ebay.com/",
                "http://reddit.com/",
                "http://cnn.com/",
                "http://bbc.com/",
                "http://spotify.com/",
                "http://github.com/",
                "http://stackoverflow.com/",
                "http://wordpress.com/",
                "http://pinterest.com/",
                "http://walmart.com/",
                "http://bing.com/",
                "http://nytimes.com/",
                "http://hulu.com/",
                "http://paypal.com/",
                "http://adobe.com/",
                "http://twitch.tv/",
                "http://msn.com/",
                "http://imdb.com/",
                "http://wordpress.org/",
                "http://espn.com/",
                "http://tumblr.com/",
                "http://microsoftonline.com/",
                "http://wordpress.com/",
                "http://whatsapp.com/",
                "http://salesforce.com/",
                "http://blogger.com/",
                "http://apple.com/",
                "http://buzzfeed.com/",
                "http://bing.com/",
                "http://tiktok.com/",
                "http://soundcloud.com/",
                "http://craigslist.org/",
                "http://bbc.co.uk/",
                "http://bbc.com/",
                "http://espn.com/",
                "http://pinterest.com/",
                "http://mozilla.org/",
                "http://slack.com/",
                "http://twitch.tv/",
                "http://chase.com/"
        };

        // Select a random domain from the list
        Random random = new Random();
        int randomIndex = random.nextInt(DOMAIN_NAMES.length);
        return DOMAIN_NAMES[randomIndex];
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
            Log.e("Error Sending SMS", e.getMessage(), e);
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
            // This is done in a background task
            new EncryptMessageTask(phoneNumber).execute(message);


        } catch (Exception e) {
            // Handle any exceptions that occur during the covert SMS sending process
            Log.e("Error Sending Covert", e.getMessage(), e);
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
            String shortenedUrl = extractShortenedUrl(messageBody);
            if (shortenedUrl == null) {
                // Failure indicates it's not a covert message or malformed covert message, ignore the message
                return;
            }
            Log.d("Shortened URL:",shortenedUrl);

            // Step 2: Make a request to the shortened URL to retrieve the 404 message containing payload
            new RecvShortenUrlTask(messageBody).execute(shortenedUrl);


        } catch (Exception e) {
            Log.e("SMSActivity", "Failed to handle received SMS", e);
        }
    }

    /**
     * Send HTTP Get request to the shortened URL.
     *
     * @param shortenedUrl The shortened URL received in the SMS message.
     * @return The content of the 404 response to the request, null if the message is not a covert message or
     * if the shortened URL is invalid.
     */
    private String requestShortenedUrl(String shortenedUrl) {
        URL url;
        try {
            url = new URL(shortenedUrl);
        } catch (Exception e)   {
            // Handle any exceptions that occur during the get request
            Log.e("Shortened URL Malformed", e.getMessage(), e);

            // Return null to indicate shortening failure
            return null;
        }
        HttpURLConnection httpURLConnection;
        try {
            httpURLConnection = (HttpURLConnection) url.openConnection();
        } catch (Exception e)   {
            // Handle any exceptions that occur during the get request
            Log.e("Shortened HTTP Error", e.getMessage(), e);
            // Return null to indicate shortening failure
            return null;
        }

        try {
            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode != -1) {
                Log.d("Code:", String.valueOf(responseCode));
                if (responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
                        responseCode == HttpURLConnection.HTTP_MOVED_TEMP ||
                        responseCode == HttpURLConnection.HTTP_CREATED ||
                        responseCode == HttpURLConnection.HTTP_MULT_CHOICE ||
                        responseCode == HttpURLConnection.HTTP_SEE_OTHER) {
                    String location = httpURLConnection.getHeaderField("Location");
                    Log.d("Location:",location);
                    return location;
                }
                //Handle other response types
                if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
                    Log.d("Bad Request", String.valueOf(responseCode));
                    return null;
                }
            }
            else    {
                Log.d("Code:","Unknown");
            }
            return null;
        } catch (Exception e)   {
            // Handle any exceptions that occur during the get request
            Log.e("BR HTTP Error", e.getMessage(), e);

            // Return null to indicate shortening failure
            return null;
        }
    }

    /**
     * Extract the shortened URL from the message body.
     *
     * @param messageBody The body of the SMS message.
     * @return The shortened URL if it exists in messageBody, null if no URL is found.
     */
    private String extractShortenedUrl(String messageBody) {
        int startIndex = messageBody.indexOf("https://"); // Find the index of the first occurrence of "https://"
        if (startIndex != -1) {
            int endIndex = messageBody.indexOf(" ", startIndex); // Find the index of the first white space after "https://"
            int endIndexNewLine = messageBody.indexOf("\n", startIndex); // Find the index of the first new line character after "https://"
            if (endIndex != -1 && (endIndexNewLine == -1 || endIndex < endIndexNewLine)) {
                return messageBody.substring(startIndex, endIndex); // Extract the URL from the message
            } else if (endIndexNewLine != -1) {
                return messageBody.substring(startIndex, endIndexNewLine); // Extract the URL from the message (with new line character)
            } else {
                // If no white space or new line character is found after "https://", assume the URL continues until the end of the message
                return messageBody.substring(startIndex);
            }
        }
        // If "https://" is not found, return null to indicate no URL is present
        return null;
    }

    /**
     * Shorten the given URL using a URL shortening service.
     *
     * @param fullUrl The URL to be shortened.
     * @param payload the encrypted payload.
     * @return The shortened URL.
     */
    private String shortenUrl(String fullUrl, String payload) {
        //source: https://www.baeldung.com/httpurlconnection-post
        //custom alias for this message

        // Get the alias from the payload (substring from index 0 to 10)
        String alias = payload.substring(0, Math.min(10, payload.length()));
        // Remove '%' from the alias
        String cleanedAlias = alias.replace("%", "");
        String encodedAlias = cleanedAlias;
        // URL encode the alias
        try {
            encodedAlias = URLEncoder.encode(cleanedAlias, StandardCharsets.UTF_8.toString());
            Log.d("Encoded Alias:",encodedAlias);
        } catch (Exception e)   {
            // Handle any exceptions that occur during the shortening process
            Log.e("Alias Encoding Error", e.getMessage(), e);
        }

        //"{\n    \"url\": \"https://www.google.com\",\n    \"alias\": \"google\"\n}"
        String request_body = "{\n    \"url\": \"".concat(fullUrl).concat("\",\n    \"alias\": \"".concat(encodedAlias).concat("\"\n}"));
        Log.d("Request Body",request_body);
        URL url;
        String response_string;
        HttpURLConnection connection;

        try {
            url = new URL("https://url-shortener23.p.rapidapi.com/shorten");
        } catch (Exception e)   {
            // Handle any exceptions that occur during the shortening process
            Log.e("URL Shortening Error", e.getMessage(), e);

            // Return null to indicate shortening failure
            return null;
        }
        try {
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("content-type", "application/json");
            connection.setRequestProperty("X-RapidAPI-Key", "6644b5ebe7mshc082f2d4a9b5ff6p1521bfjsn2c31ab970604");
            connection.setRequestProperty("X-RapidAPI-Host", "url-shortener23.p.rapidapi.com");
            connection.setDoOutput(true);
        } catch (Exception e)   {
            // Handle any exceptions that occur when establishing the http connection
            Log.e("HTTP Connection Error", e.getMessage(), e);

            // Return null to indicate shortening failure
            return null;
        }
        try {
            OutputStream os = connection.getOutputStream();
            byte[] input = request_body.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        } catch (Exception e)   {
            // Handle any IO exceptions that occur
            Log.e("IO Error", e.getMessage(), e);

            // Return null to indicate shortening failure
            return null;
        }
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            response_string = response.toString();
            Log.d("Short URL Response:",response_string);
        } catch (Exception e)   {
            // Handle any IO exceptions that occur
            Log.e("BufferedReader Error:", e.getMessage(), e);

            // Return null to indicate shortening failure
            return null;
        }
        return response_string.substring(14, response_string.length() -2);
    }

    /**
     * Extracts the payload from a full URL.
     *
     * @param url The full URL from which to extract the payload.
     * @return The extracted payload as a string, or null if there was an error during extraction.
     */
    private String extractPayloadFromUrl(String url) {
        try {
            // Find the last occurrence of '/'
            int lastIndex = url.lastIndexOf('/');

            // Extract everything after ".com/" as the payload
            String payload = url.substring(lastIndex + 1);

            // URL decode the payload to handle any URL-encoded characters
            return URLDecoder.decode(payload, "UTF-8");
        } catch (Exception e) {
            // Handle any exceptions that occur during the extraction process
            Log.e("URL Extraction Error", e.getMessage(), e);

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

            return Arrays.copyOf(hashedKey, 16);
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

            e.printStackTrace();
            return null; // Return null to indicate decryption failure
        }
    }
}