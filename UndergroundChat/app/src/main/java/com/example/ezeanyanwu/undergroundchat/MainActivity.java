package com.example.ezeanyanwu.undergroundchat;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;


import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    /* UI Elements */
    TextView chatDestination;
    EditText chatEditWindow;
    Button chatSendButton;
    ListView chatListview;


    /* The ListView UI element is special and needs friends to help it work */
    /* Friends of chatListview */
    ArrayAdapter mArrayAdapter;
    ArrayList mChatList = new ArrayList();


    XmppServiceStart myService;
    XMPPConnection myConnection;
    ChatManager myChatManager;
    Roster myRoster;

    String currentUsername;

    boolean isBound = false;

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(myIMListener, new IntentFilter("Message-Received"));
    }

    @Override
    public void onDestroy()
    {
        unbindService(mConnection);
        super.onDestroy();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        chatDestination = (TextView) findViewById(R.id.destination);
        chatEditWindow = (EditText) findViewById(R.id.chat_edittext);
        chatSendButton = (Button) findViewById(R.id.chat_send_button);
        chatListview = (ListView) findViewById(R.id.chat_list_view);


        /* The next two lines are taken from ListView documentation at developer.android.com */
        mArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, mChatList);
        chatListview.setAdapter(mArrayAdapter);



        /* Register a listener for the "Send" button */
        chatSendButton.setOnClickListener(sendButtonPressed);

        /* Prepare and intent and bind to the XmppServiceStart classs */
        Intent intent = new Intent(this, XmppServiceStart.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        Intent creator = getIntent();
        currentUsername = creator.getStringExtra("USERNAME");


    }


    /* A Broadcaast Receiver to get contents of incoming chat */
    private BroadcastReceiver myIMListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String from = intent.getStringExtra("FROM");
            String text = intent.getStringExtra("TEXT");
            if ( from.equals(currentUsername) )
            {
                chatDestination.setText(from);
                String updateString = from + ": " + text + "\n";
                mChatList.add(updateString);
                mArrayAdapter.notifyDataSetChanged();
                chatListview.setSelection(mArrayAdapter.getCount() - 1);
            }
        }
    };

    /* This declaration is needed to bind a service. Directly taken from developer.android.com */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {

            XmppServiceStart.LocalBinder binder = (XmppServiceStart.LocalBinder) service;
            myService = binder.getService();
            myConnection = myService.getConnection();
            myChatManager = myService.getChatManager();
            myRoster = myService.getRoster();

            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isBound = false;
        }

    };


    private View.OnClickListener sendButtonPressed = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String text = chatEditWindow.getText().toString();
            chatEditWindow.setText("");
            String threadID = myService.getChat(currentUsername);
            myService.sendChat(text, threadID);
            String updateString = "eze@gordon.com: " + text + "\n";
            mChatList.add(updateString);
            mArrayAdapter.notifyDataSetChanged();
            chatListview.setSelection(mArrayAdapter.getCount() - 1);
        }
    };


}
