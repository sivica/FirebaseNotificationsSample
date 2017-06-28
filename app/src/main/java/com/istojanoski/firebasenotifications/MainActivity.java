package com.istojanoski.firebasenotifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.DeviceRegistration;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.messaging.DeliveryOptions;
import com.backendless.messaging.MessageStatus;
import com.backendless.messaging.PublishOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.istojanoski.firebasenotifications.utils.NotificationUtils;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private TextView txtRegId, txtMessage;
    private TextView txtBackendLessRegId;
    private Button btnMessage;
    private View.OnClickListener mOnPushClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
            String backendlessId = pref.getString("backendlessId", null);
            List<String>  backendlessIds = new ArrayList<>();
            backendlessIds.add("85ea740c");
            DeliveryOptions deliveryOptions = new DeliveryOptions();
            deliveryOptions.setPushSinglecast(backendlessIds);

            PublishOptions publishOptions = new PublishOptions();
            publishOptions.putHeader( "android-ticker-text", "You just got a private push notification!" );
            publishOptions.putHeader( "android-content-title", "This is a notification title" );
            publishOptions.putHeader( "android-content-text", "Push Notifications are cool" );

            Backendless.Messaging.publish((Object) "this is a private message!", publishOptions, deliveryOptions, new AsyncCallback<MessageStatus>() {
                @Override
                public void handleResponse(MessageStatus response) {
                    txtBackendLessRegId.setText("MessageStatus: ");
                }

                @Override
                public void handleFault(BackendlessFault fault) {
                    txtBackendLessRegId.setText(fault.getMessage());
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtRegId = (TextView) findViewById(R.id.txt_reg_id);
        txtBackendLessRegId = (TextView) findViewById(R.id.txt_backend_less_reg_id);
        txtMessage = (TextView) findViewById(R.id.txt_push_message);
        btnMessage = (Button) findViewById(R.id.btn_push);
        btnMessage.setOnClickListener(mOnPushClickListener);
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getAction().equals(Config.REGISTRATION_COMPLETE)) {
                    FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL);

                    displayFirebaseRegId();

                } else if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {

                    String message = intent.getStringExtra("message");

                    Toast.makeText(getApplicationContext(), "Push notification: " + message, Toast.LENGTH_LONG).show();

                    txtMessage.setText(message);
                }
            }
        };

        displayFirebaseRegId();

        Backendless.setUrl(Config.SERVER_URL);
        Backendless.initApp(getApplicationContext(), Config.APPLICATION_ID, Config.API_KEY);
        Backendless.Messaging.registerDevice(Config.SENDER_ID, "default", new AsyncCallback<Void>() {
            @Override
            public void handleResponse(Void response) {
                txtBackendLessRegId.setText("Backendless Reg Id: ");
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                txtRegId.setText("Backendless Reg Id is not received yet!");
            }
        });

        Backendless.Messaging.getDeviceRegistration(new AsyncCallback<DeviceRegistration>() {
            @Override
            public void handleResponse(DeviceRegistration response) {
                txtBackendLessRegId.setText("Backendless Reg Id: " + response.getDeviceId());
                SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("backendlessId", response.getDeviceId());
                editor.commit();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                txtBackendLessRegId.setText(fault.getMessage());
            }
        });
    }

    private void displayFirebaseRegId() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        String regId = pref.getString("regId", null);

        Log.e(TAG, "Firebase reg id: " + regId);

        if (!TextUtils.isEmpty(regId))
            txtRegId.setText("Firebase Reg Id: " + regId);
        else
            txtRegId.setText("Firebase Reg Id is not received yet!");
    }

    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.REGISTRATION_COMPLETE));

        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOTIFICATION));

        NotificationUtils.clearNotifications(getApplicationContext());
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }
}
