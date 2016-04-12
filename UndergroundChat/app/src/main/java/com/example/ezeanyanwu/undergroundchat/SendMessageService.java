package com.example.ezeanyanwu.undergroundchat;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * helper methods.
 */
public class SendMessageService extends IntentService {


    public SendMessageService() {
        super("SendMessageService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        String message = intent.getStringExtra("TEXT_TO_SEND");
        Log.d("SendMessageService:", "Hey! The Service is running!, we are sending ..." + message);

    }



}
