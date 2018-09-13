package com.example.sayyaf.homecare.requests;

import com.example.sayyaf.homecare.accounts.User;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

public class RequestController {

    public static void acceptRequest(DatabaseReference ref, String email, String id, User currentUser) {

        // add user to both users friend list
        addFriendForCurrentUser(ref, email, id, currentUser);
        addFriendForRequestSender(ref, id, currentUser);

        // use time as one of the chat database identifier
        long time = new Date().getTime();

        // add common chat room name to both users chat database list
        addChatDatabaseRequestSender(ref, id, currentUser, time);
        addChatDatabaseCurrentUser(ref, id, currentUser, time);

        // clear up pending requests and sents
        removeRequest(ref, id, currentUser);
        removeSentRequest(ref, id, currentUser);
    }

    public static void declineRequest(DatabaseReference ref, String id, User currentUser) {

        // clear up pending requests and sents
        removeRequest(ref, id, currentUser);
        removeSentRequest(ref, id, currentUser);
    }

    public static void addSentRequest(DatabaseReference ref, String otherId, String otherEmail,
                                String currentId) {

        ref.child("User").child(currentId)
                .child("requestsSent")
                .push();

        ref.child("User").child(currentId)
                .child("requestsSent")
                .child(otherId)
                .setValue(otherEmail);
    }

    public static void addReceiverRequest(DatabaseReference ref, String otherId, String currentEmail,
                                    String currentId) {

        ref.child("User").child(otherId)
                .child("requests")
                .push();

        ref.child("User").child(otherId)
                .child("requests")
                .child(currentId)
                .setValue(currentEmail);

    }

    public static void removeUser(DatabaseReference ref, User currentUser, User otherUser) {
        ref.child("User").child(currentUser.getId())
                .child("friends")
                .child(otherUser.getId())
                .removeValue();

        ref.child("User")
                .child(otherUser.getId())
                .child("friends")
                .child(currentUser.getId())
                .removeValue();


        ref.child("User").child(currentUser.getId())
                .child("chatDatabase")
                .child(otherUser.getId())
                .removeValue();

        ref.child("User").child(otherUser.getId())
                .child("chatDatabase")
                .child(currentUser.getId())
                .removeValue();

        removeCommonChatroom(ref, currentUser, otherUser);
    }

    private static void addFriendForCurrentUser(DatabaseReference ref, String otherEmail,
                                               String otherId, User currentUser) {

        ref.child("User").child(currentUser.getId()).child("friends").push();
        ref.child("User").child(currentUser.getId())
                .child("friends")
                .child(otherId)
                .setValue(otherEmail);
    }

    private static void addFriendForRequestSender(DatabaseReference ref, String otherId,
                                                  User currentUser) {

        ref.child("User").child(otherId).child("friends").push();
        ref.child("User")
                .child(otherId)
                .child("friends")
                .child(currentUser.getId())
                .setValue(currentUser.getEmail());
    }

    public static void removeSentRequest(DatabaseReference ref, String otherId,
                                          User currentUser) {
        ref.child("User")
                .child(otherId)
                .child("requestsSent")
                .child(currentUser.getId())
                .removeValue();
    }

    public static void removeRequest(DatabaseReference ref, String otherId,
                                          User currentUser) {

        ref.child("User")
                .child(currentUser.getId())
                .child("requests")
                .child(otherId)
                .removeValue();
    }

    private static void addChatDatabaseCurrentUser(DatabaseReference ref, String otherId,
                                                   User currentUser, long time) {

        ref.child("User")
                .child(currentUser.getId())
                .child("chatDatabase")
                .push();

        ref.child("User")
                .child(currentUser.getId())
                .child("chatDatabase")
                .child(otherId)
                .setValue(currentUser.getId() + time + otherId);
    }

    private static void addChatDatabaseRequestSender(DatabaseReference ref, String otherId,
                                                     User currentUser, long time) {

        ref.child("User")
                .child(otherId)
                .child("chatDatabase")
                .push();

        ref.child("User")
                .child(otherId)
                .child("chatDatabase")
                .child(currentUser.getId())
                .setValue(currentUser.getId()+ time + otherId);
    }

    private static void removeCommonChatroom(DatabaseReference ref, User currentUser, User otherUser){

        ref.child("chatDB")
                .child(otherUser.getChatDatabase().get(currentUser.getId()))
                .removeValue();

        ref.child("user")
                .child(currentUser.getId())
                .child("chatDatabase")
                .child(otherUser.getId())
                .removeValue();

        ref.child("user")
                .child(otherUser.getId())
                .child("chatDatabase")
                .child(currentUser.getId())
                .removeValue();
    }



}
