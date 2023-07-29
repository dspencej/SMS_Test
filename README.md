SMS Test App

SMS Test App is a simple Android application that allows you to send and receive SMS 
messages using an Android device or emulator. It demonstrates the basic functionality 
of sending and receiving SMS messages, as well as displaying message metadata such as 
the originating address, timestamp, protocol, and display originating address.

Prerequisites
Android device or emulator running Android OS version X.X or higher.

Permissions:
android.permission.RECEIVE_SMS: Required to receive incoming SMS messages.
android.permission.SEND_SMS: Required to send SMS messages.
Note: It is recommended to use Android API 30 or earlier for the virtual Android devices. 
Newer versions may not route SMS messages between devices correctly.

Installation
Clone the repository or download the source code.
Open the project in Android Studio.
Build and run the application on your Android device or emulator.

Usage
Launch the SMS Test App on your Android device or emulator.
Grant the necessary permissions (RECEIVE_SMS and SEND_SMS) when prompted.
On the main screen, enter a phone number and a message in the respective fields.
Tap the "Send" button to send the SMS message.
The sent message's metadata, including the originating address, timestamp, protocol, 
and display originating address, will be displayed in the app's interface.
If you receive an SMS message while the app is open, the incoming message's metadata 
will be displayed in the app.

Known Bugs
Permission Issue: There is a bug in the permission checking where you have to open the 
app, close the app, and then re-open the app in order to send SMS messages from a real 
device. If you receive the "SMS failed to send" error message, try closing and re-opening the app.

Contributing
Contributions to SMS Test App are welcome! 
If you would like to contribute to the project, please follow these steps:

Fork the repository.
Create a new branch for your feature or bug fix.
Make your changes and commit them to your branch.
Push your changes to your forked repository.
Submit a pull request with a description of your changes.

License
SMS Test App is open-source software released under the MIT License.

Acknowledgements
This application was developed using Android Studio.
The Android SDK was used for SMS message handling.

Contact
For any questions, issues, or feedback, please contact Dustin Spencer (dspenc18@jhu.edu).
