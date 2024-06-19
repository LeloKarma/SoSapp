package com.example.sosapp;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 1;
    private EditText contactNumber1;
    private EditText contactNumber2;
    private EditText customMessage;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "SOSPrefs";
    private static final String CONTACT_1_KEY = "ContactNumber1";
    private static final String CONTACT_2_KEY = "ContactNumber2";
    private static final String MESSAGE_KEY = "CustomMessage";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        contactNumber1 = findViewById(R.id.contactNumber1);
        contactNumber2 = findViewById(R.id.contactNumber2);
        customMessage = findViewById(R.id.customMessage);
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        loadSavedData();

        requestPermissions();

        Button addContactButton = findViewById(R.id.addContactButton);
        addContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveContactNumbers();
            }
        });

        Button saveMessageButton = findViewById(R.id.saveMessageButton);
        saveMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCustomMessage();
            }
        });

        Button sosButton = findViewById(R.id.sosButton);
        sosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSOSMessage();
            }
        });
    }

    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                        != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.SEND_SMS,
                            Manifest.permission.CALL_PHONE,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.READ_CONTACTS
                    }, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                // All permissions granted
            }
        }
    }

    private void saveContactNumbers() {
        String number1 = contactNumber1.getText().toString();
        String number2 = contactNumber2.getText().toString();

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(CONTACT_1_KEY, number1);
        editor.putString(CONTACT_2_KEY, number2);
        editor.apply();

        Toast.makeText(this, "Contact numbers saved", Toast.LENGTH_SHORT).show();
    }

    private void saveCustomMessage() {
        String message = customMessage.getText().toString();

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(MESSAGE_KEY, message);
        editor.apply();

        Toast.makeText(this, "Custom message saved", Toast.LENGTH_SHORT).show();
    }

    private void loadSavedData() {
        String number1 = sharedPreferences.getString(CONTACT_1_KEY, "");
        String number2 = sharedPreferences.getString(CONTACT_2_KEY, "");
        String message = sharedPreferences.getString(MESSAGE_KEY, "");

        contactNumber1.setText(number1);
        contactNumber2.setText(number2);
        customMessage.setText(message);
    }

    private void sendSOSMessage() {
        String number1 = contactNumber1.getText().toString();
        String number2 = contactNumber2.getText().toString();
        String message = customMessage.getText().toString();

        if (number1.isEmpty() && number2.isEmpty()) {
            Toast.makeText(this, "Please enter at least one contact number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (message.isEmpty()) {
            Toast.makeText(this, "Please enter a custom message", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!number1.isEmpty()) {
            sendMessage(number1, message);
        }
        if (!number2.isEmpty()) {
            sendMessage(number2, message);
        }
    }

    private void sendMessage(String phoneNumber, String message) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(this, "SOS message sent to " + phoneNumber, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to send SOS message", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
