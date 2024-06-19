package com.example.sosapp;


import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

public class SOSService extends Service {
    private Handler handler = new Handler();
    private List<String> contactNumbers;
    private String sosMessage;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        contactNumbers = intent.getStringArrayListExtra("contactNumbers");
        sosMessage = intent.getStringExtra("sosMessage");

        handler.post(sendLocationUpdates);
        return START_STICKY;
    }

    private Runnable sendLocationUpdates = new Runnable() {
        @Override
        public void run() {
            sendSOSMessage();
            handler.postDelayed(this, 5 * 60 * 1000); // 5 minutes interval
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(sendLocationUpdates);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void sendSOSMessage() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();
                                String message = sosMessage.isEmpty() ? "SOS! My current location is: https://maps.google.com/?q=" + latitude + "," + longitude : sosMessage + " Location: https://maps.google.com/?q=" + latitude + "," + longitude;
                                for (String number : contactNumbers) {
                                    sendSMS(number, message);
                                }
                            } else {
                                Toast.makeText(SOSService.this, "Unable to get location", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void sendSMS(String phoneNumber, String message) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(this, "SOS message sent to " + phoneNumber, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "SMS failed, please try again", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
