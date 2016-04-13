##Mobile Computing:
####Project Proposal:
I'll be creating a simple android chatting app. It will have chatting capabilities similar to Facebook messenger, google talk and Apple Messages. A simple chat server has been set up using Amazon Web Services. All accounts will be created on said server, and the app will be hardwired to only use this server.   
The app is based upon the XMPP protocol which is an open standard for messaging. When google talk was still around, it was based upon XMPP. It has now been replaced by Hangouts. Unfortunately, Hangouts does not rely on XMPP as much as Google talk did, so using it as a server would be risky, which is why I am using a full XMPP server.  
The idea originated www.ssavr.com a website that enables you to share notes with people on the same network. My hope was for an app were people on the same network could automatically be part of the same chat room and chat it up. After a bit of research, I found it would be practical to make the app more general. The t
arget audience is anyone who is interested in using it.   

####Features that will be implemented in the Prototype:  
- List of contacts.  
- Start a chat will anyone on the contacts list.  
- Listen for incoming chats from anyone with an account on the server


####Use Cases:

###### User wants to add a new contact
If the username entered exists on the server, the contact will be added to ther user's friend's list. If the username doesn't exist, the user will be alerted that the username doens't exist.

###### User wants to start chatting with existing contact

Regardless of whether the contact is online or not, the chat will open up and the user can send messages. The contact won't see these messages until he/she logs in.

###### Incoming Chat
User is notified via an alert that a new chat message from "username@gordon.edu" has arrived.

