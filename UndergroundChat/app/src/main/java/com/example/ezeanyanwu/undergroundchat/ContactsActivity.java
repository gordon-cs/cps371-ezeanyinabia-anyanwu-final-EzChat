package com.example.ezeanyanwu.undergroundchat;

import android.app.Dialog;
import android.app.LauncherActivity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntries;
import org.jivesoftware.smack.roster.RosterEntry;

import java.util.ArrayList;
import java.util.Collection;

public class ContactsActivity extends AppCompatActivity {


    ListView contactsListView;
    ArrayAdapter contactsArrayAdapter;
    ArrayList contactsArrayList = new ArrayList();


    static XmppServiceStart myService;
    XMPPConnection myConnection;
    ChatManager myChatManager;
    Roster myRoster;
    boolean isBound = false;

    public String userToAdd = "";
    public String userToDelete = "";

    @Override
    public void onResume()
    {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(myInitialRosterListener, new IntentFilter("Initial-Roster-List"));
        LocalBroadcastManager.getInstance(this).registerReceiver(myAddRosterListener, new IntentFilter("Add-Roster-List"));
        LocalBroadcastManager.getInstance(this).registerReceiver(myDeleteRosterListener, new IntentFilter("Delete-Roster-List"));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        contactsListView = (ListView) findViewById(R.id.contacts);

        /* Prepare and intent and bind to the XmppServiceStart classs */
        Intent intent = new Intent(this, XmppServiceStart.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        contactsArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, contactsArrayList);
        contactsListView.setAdapter(contactsArrayAdapter);
        contactsListView.setOnItemClickListener(onContactsClick);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.contacts_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.add_contact:
                // User chose to add a friend
                DialogFragment dialog1 = new AddFriendDialogFragment();
                dialog1.show(getSupportFragmentManager(), "add_friend");
                return true;
            case R.id.remove_contact:
                DialogFragment dialog2 = new DeleteFriendDialogFragment();
                dialog2.show(getSupportFragmentManager(), "remove_friend");
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

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

        }

    };
    /* A Broadcaast Receiver to get contents of incoming chat */
    private BroadcastReceiver myInitialRosterListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String[] usernames = intent.getStringArrayExtra("ROSTER");
            for(String user: usernames)
            {
                contactsArrayList.add(user);
            }
            contactsArrayAdapter.notifyDataSetChanged();
        }
    };
    private BroadcastReceiver myAddRosterListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String[] usernames = intent.getStringArrayExtra("ROSTER");
            for(String user: usernames)
            {
                if(!contactsArrayList.contains(user))
                {
                    contactsArrayList.add(user);
                    myService.addFriend(user);
                }
            }
            contactsArrayAdapter.notifyDataSetChanged();
        }
    };
    private BroadcastReceiver myDeleteRosterListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String[] usernames = intent.getStringArrayExtra("ROSTER");
            for(String user: usernames)
            {
                int i = contactsArrayList.indexOf(user);
                if(contactsArrayList.contains(user))
                {

                    contactsArrayList.remove(i);
                    myService.deleteFriend(user);

                }
            }
            contactsArrayAdapter.notifyDataSetChanged();
        }
    };

    private AdapterView.OnItemClickListener onContactsClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String username = contactsArrayList.get(position).toString();
            Intent intent = new Intent(ContactsActivity.this, MainActivity.class);
            intent.putExtra("USERNAME", username);
            startActivity(intent);
        }
    };

    public static class AddFriendDialogFragment extends DialogFragment
    {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View promptView = inflater.inflate(R.layout.dialog_addfriend,null);
            Log.d("DIALOG:", "My dialog created!");
            builder.setView(promptView);
            final EditText input = (EditText) promptView.findViewById(R.id.new_friend);
            builder.setPositiveButton(R.string.add_friend_option, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String usernames[] = new String[1];
                    usernames[0] = input.getText().toString();
                    Log.d("DIALOG:", "My dialog created!" + usernames[0]);
                    Intent rosterIntent = new Intent("Add-Roster-List");
                    rosterIntent.putExtra("ROSTER", usernames);
                    LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(rosterIntent);


                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.d("DIALOG:", "My dialog created!");
                }
            });
            return builder.create();

        }
    }

    public static class DeleteFriendDialogFragment extends DialogFragment
    {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater= getActivity().getLayoutInflater();
            View promptView = inflater.inflate(R.layout.dialog_deletefriend, null);
            Log.d("DIALOG:", "My dialog created!");
            builder.setView(promptView);
            final EditText input = (EditText) promptView.findViewById(R.id.remove_friend_name);
            builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String usernames[] = new String[1];
                    usernames[0] = input.getText().toString();
                    Log.d("DIALOG:", "My dialog created!" + usernames[0]);
                    Intent rosterIntent = new Intent("Delete-Roster-List");
                    rosterIntent.putExtra("ROSTER", usernames);
                    LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(rosterIntent);
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            return builder.create();
        }
    }
}
