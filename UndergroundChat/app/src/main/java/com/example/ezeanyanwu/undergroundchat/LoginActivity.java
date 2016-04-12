package com.example.ezeanyanwu.undergroundchat;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.jivesoftware.smack.XMPPConnection;

public class LoginActivity extends AppCompatActivity {

    /* UI Elements */
    EditText usernameBox;
    EditText passwordBox;
    Button loginButton;


    XmppServiceStart myService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        usernameBox = (EditText) findViewById(R.id.username);
        passwordBox = (EditText) findViewById(R.id.password);
        loginButton = (Button) findViewById(R.id.login_button);
        loginButton.setOnClickListener(loginButtonListener);

    }


    /* Once login is pressed, bind to the XmppServiceStart service to attempt to connect */
    private View.OnClickListener loginButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String username = usernameBox.getText().toString();
            String password = passwordBox.getText().toString();

            Intent intent = new Intent(LoginActivity.this, XmppServiceStart.class);
            intent.putExtra("USERNAME", username);
            intent.putExtra("PASSWORD", password);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            startService(intent);
        }
    };

    /* Once connected, create an intent to switch activities */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {

            XmppServiceStart.LocalBinder binder = (XmppServiceStart.LocalBinder) service;
            myService = binder.getService();
            Intent mainActivityIntent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(mainActivityIntent);

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {

        }

    };

}
