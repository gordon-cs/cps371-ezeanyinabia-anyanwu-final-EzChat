package com.example.ezeanyanwu.undergroundchat;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smack.util.XmlStringBuilder;
import org.jivesoftware.smackx.bytestreams.ibb.InBandBytestreamManager;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smack.filter.StanzaFilter;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class XmppServiceStart extends IntentService {

    private final IBinder myBinder = new LocalBinder();

    //Various variables offered by this service
    private ChatManager theChatManager;
    private MultiUserChatManager theMUChatManager;
    private XMPPConnection theConnection;
    private Roster theRoster;

    /* Variables to keep track of ongoing chats */
    //private List<String> chatList = new ArrayList<>();
    private List<String> muChatList = new ArrayList<>();
    private HashMap<String, String> chatList = new HashMap<>();
    private String mostRecentMuChat = "0";
    private String mostRecentChat = "0";

    /* User credentials */
    private String username;
    private String password;

    /* Constructor */
    public XmppServiceStart()
    {
        super("XmppServiceStart");
    }

    public class LocalBinder extends Binder
    {
        XmppServiceStart getService()
        {
            return XmppServiceStart.this;
        }
    }

    /* This is run when the activity calls startService() */
    @Override
    protected void onHandleIntent(Intent intent)
    {
        Log.d("Xmpp:", "Hey! The Service Ran!");
        username = intent.getStringExtra("USERNAME");
        password = intent.getStringExtra("PASSWORD");

        theConnection = connectToServer(username, password);
        theRoster = setupRoster();
        theChatManager = startListeningForChats();
        theMUChatManager = startListeningForMUChats();

        //Roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.accept_all);

    }

    /* This is called when the activity calls bindService() */
    @Override
    public IBinder onBind(Intent intent)
    {
        Log.d("Xmpp:", "Hey! The Service is Bound!");
        return myBinder;
    }

    /* Connect to the server */
    public XMPPConnection connectToServer(String user, String pass)
    {
        /* Amazon server name */
        String serverName ="ec2-52-33-88-207.us-west-2.compute.amazonaws.com";

        /* Configure connection settings */
        XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                .setUsernameAndPassword(user, pass)
                .setServiceName("gordon.com")
                .setHost(serverName)
                .setPort(5222)
                .setDebuggerEnabled(true)
                .build();

        AbstractXMPPConnection conn = new XMPPTCPConnection(config);

        /* Attempt to connect, throw exception if not able to */
        try {
            conn.connect();
        } catch (SmackException e) {
            Log.d("SmackException", e.getMessage().toString());
            System.exit(0);
        } catch (IOException e) {
            Log.d("IOException onConnect", e.getMessage().toString());
            System.exit(0);
        } catch (XMPPException e) {
            Log.d("XMPPException onConnect", e.getMessage().toString());
            System.exit(0);
        }

        Log.d("Connected:", "Hey! We are connected!");


        /* Attempt to login, throw exceptions if not able to */
        try {
            conn.login();
        } catch (XMPPException e) {
            e.printStackTrace();
            System.exit(0);
        } catch (SmackException e) {
            e.printStackTrace();
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
        Log.d("Logged In: ", "Hey! We are logged in!");

        /* Return the connection object */
        return conn;

    }


    /* Function to setup a listener for incoming multi-user chats (chat rooms) */

    public MultiUserChatManager startListeningForMUChats()
    {

        final MultiUserChatManager muChatManager = MultiUserChatManager.getInstanceFor(theConnection);
        muChatManager.addInvitationListener(new InvitationListener() {
            @Override
            public void invitationReceived(XMPPConnection conn, MultiUserChat room, String inviter, String reason, String password, Message message) {
                if (muChatList == null || !muChatList.contains(room.getRoom())) {
                    try {
                        room.join(username);
                        room.sendMessage("I've joined the MultiUserChat");
                    } catch (SmackException.NoResponseException e) {
                        Log.d("NEW_GRP_CHT", e.getMessage().toString());
                        e.printStackTrace();
                    } catch (XMPPException.XMPPErrorException e) {
                        Log.d("NEW_GRP_CHT", e.getMessage().toString());
                    } catch (SmackException.NotConnectedException e) {
                        Log.d("NEW_GRP_CHT", e.getMessage().toString());
                    }

                    muChatList.add(room.getRoom());
                    Log.d("NEW_GRP_CHT", room.getRoom());
                }

                mostRecentMuChat = room.getRoom();
            }
        });

        return muChatManager;
    }

    /* Function to setup a listener for incoming chats */
    public ChatManager startListeningForChats()
    {
        ChatManager chatmanager = ChatManager.getInstanceFor(theConnection);
        chatmanager.addChatListener(
                new ChatManagerListener() {
                    @Override
                    public void chatCreated(Chat chat, boolean createdLocally) {
                        if (!createdLocally) {
                            chat.addMessageListener(new ChatMessageListener());
                        }
                    }
                }
        );
        return chatmanager;
    }
    /* Function to setup roster and listen for incoming roster requests */
    public Roster setupRoster()
    {
        Roster roster = Roster.getInstanceFor(theConnection);
        Roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.accept_all);
        if(!roster.isLoaded())
        {
            try {
                roster.reloadAndWait();
            } catch (SmackException.NotLoggedInException e) {
                e.printStackTrace();
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        Collection<RosterEntry> rosterEntries = roster.getEntries();
        String[] usernames = new String[rosterEntries.size()];
        int size = rosterEntries.size();
        int counter = 0;

        for(RosterEntry entry: rosterEntries )
        {
            String user = entry.getUser();
            Log.d("ROSTER:", user);
            usernames[counter] = user;
            counter++;
        }

        Intent rosterIntent = new Intent("Initial-Roster-List");
        rosterIntent.putExtra("ROSTER", usernames);
        LocalBroadcastManager.getInstance(XmppServiceStart.this).sendBroadcast(rosterIntent);
        Log.d("ROSTER:","ROSTER has been setup");

        theConnection.addAsyncStanzaListener(new StanzaListener() {
            @Override
            public void processPacket(Stanza packet) throws SmackException.NotConnectedException {
                Log.d("THEXML:", packet.toXML().toString());
                Presence prez = (Presence) packet;
                String presenceType = prez.getType().toString();
                String from = prez.getFrom();
                String usernames[] = new String[1];
                Log.d("Presence:", presenceType);

                if(theRoster.getEntry(from) != null && presenceType.equals("unsubscribe"))
                {
                    usernames[0] = from;
                    Intent rosterIntent = new Intent("Delete-Roster-List");
                    rosterIntent.putExtra("ROSTER", usernames);
                    LocalBroadcastManager.getInstance(XmppServiceStart.this).sendBroadcast(rosterIntent);
                }
                else if(theRoster.getEntry(from) == null && presenceType.equals("subscribe"))
                {
                    usernames[0] = from;
                    Intent rosterIntent = new Intent("Add-Roster-List");
                    rosterIntent.putExtra("ROSTER", usernames);
                    LocalBroadcastManager.getInstance(XmppServiceStart.this).sendBroadcast(rosterIntent);
                }

                if(theRoster.getEntry(from) != null && presenceType.equals("Available"))
                {

                }

            }
        }, StanzaTypeFilter.PRESENCE);

        roster.addRosterListener(new RosterListener() {
            @Override
            public void entriesAdded(Collection<String> addresses) {
//                int size = addresses.size();
//                String[] usernames = new String[size];
//                int counter = 0;
//
//                for(String user: addresses )
//                {
//                    Log.d("ROSTER:", user);
//                    usernames[counter] = user;
//                    counter++;
//                }
//                Intent rosterIntent = new Intent("Add-Roster-List");
//                rosterIntent.putExtra("ROSTER", usernames);
//                LocalBroadcastManager.getInstance(XmppServiceStart.this).sendBroadcast(rosterIntent);
            }
            @Override
            public void entriesUpdated(Collection<String> addresses) {


            }

            @Override
            public void entriesDeleted(Collection<String> addresses) {
//                int size = addresses.size();
//                String[] usernames = new String[size];
//                int counter = 0;
//
//                for(String user: addresses )
//                {
//                    Log.d("ROSTER:", user);
//                    usernames[counter] = user;
//                    counter++;
//                }
//                Intent rosterIntent = new Intent("Delete-Roster-List");
//                rosterIntent.putExtra("ROSTER", usernames);
//                LocalBroadcastManager.getInstance(XmppServiceStart.this).sendBroadcast(rosterIntent);
            }

            @Override
            public void presenceChanged(Presence presence) {
                Log.d("ROSTER4:","Presence changed: " + presence.getFrom() + " " + presence);
            }
        });

        return roster;
    }
    public void addFriend(String userID)
    {
        try {
            theRoster.createEntry(userID,userID,null);
        } catch (SmackException.NotLoggedInException e) {
            e.printStackTrace();
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }

    }

    public void deleteFriend(String userID)
    {
        RosterEntry entry = theRoster.getEntry(userID);
        if(entry != null)
        {
            try {
                theRoster.removeEntry(entry);
            } catch (SmackException.NotLoggedInException e) {
                e.printStackTrace();
            } catch (SmackException.NoResponseException e) {
                e.printStackTrace();
            } catch (XMPPException.XMPPErrorException e) {
                e.printStackTrace();
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            }
        }

    }
    /* Convenience method to send message */
    /* NOT FINISHED YET */
    public void sendChat(String text, String threadID)
    {

        Chat chat = theChatManager.getThreadChat(threadID);
        try {
            chat.sendMessage(text);
        } catch (SmackException.NotConnectedException e) {
            Log.d("Xmpp: ", "NOT CONNECTED");
        }
    }

    public String getChat(String username)
    {
        String thread = "";
        if ( chatList.containsKey(username))
        {
            thread = chatList.get(username);
            //Chat chat = theChatManager.getThreadChat(thread);
        }
        else
        {
           Chat chat = theChatManager.createChat(username);
            chat.addMessageListener(new ChatMessageListener());
            thread = chat.getThreadID();
        }
        return thread;
    }
    /* Setters and Getters */
    public XMPPConnection getConnection()
    {
        return theConnection;
    }
    public Roster getRoster()
    {
        return theRoster;
    }
    public ChatManager getChatManager()
    {
        return theChatManager;
    }

    /* Listener object that is used by startListeningForChats */
    public class ChatMessageListener implements org.jivesoftware.smack.chat.ChatMessageListener
    {
        @Override
        public void processMessage(Chat chat, Message message)
        {
            if( chatList.isEmpty() || !chatList.containsKey(chat.getParticipant()))
            {
                chatList.put(chat.getParticipant(), chat.getThreadID());
                Log.d("NEWCHAT: ", chat.getParticipant() + " " + chat.getThreadID());
            }

            if((message.getType() == Message.Type.chat || message.getType() == Message.Type.groupchat) && hasBody(message))
            {
                String from = chat.getParticipant();
                String text = message.getBody().toString();
                Log.d("NEW CHAT:", from + ":" + text);
                Intent intent = new Intent("Message-Received");
                intent.putExtra("FROM", from);
                intent.putExtra("TEXT", text);
                LocalBroadcastManager.getInstance(XmppServiceStart.this).sendBroadcast(intent);
            }
        }

        private boolean hasBody(Message message)
        {

            if (message.getBody() == null )
            {
                return false;
            }
            else
            {
                return true;
            }
        }
    }

}
