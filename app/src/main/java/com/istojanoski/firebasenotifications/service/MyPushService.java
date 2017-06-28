package com.istojanoski.firebasenotifications.service;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.backendless.push.BackendlessPushService;

/**
 * Created by User on 16/06/2017.
 */

public class MyPushService extends BackendlessPushService {

    @Override
    public void onRegistered(Context context, String registrationId )
    {
        Toast.makeText( context, "device registered" + registrationId, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUnregistered( Context context, Boolean unregistered )
    {
        Toast.makeText( context, "device unregistered", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onMessage( Context context, Intent intent )
    {
        String message = intent.getStringExtra( "message" );
        Toast.makeText( context, "Push message received. Message: " + message, Toast.LENGTH_LONG ).show();
        // When returning 'true', default Backendless onMessage implementation will be executed.
        // The default implementation displays the notification in the Android Notification Center.
        // Returning false, cancels the execution of the default implementation.
        return false;
    }

    @Override
    public void onError( Context context, String message )
    {
        Toast.makeText( context, message, Toast.LENGTH_SHORT).show();
    }
}
