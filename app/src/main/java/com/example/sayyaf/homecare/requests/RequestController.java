package com.example.sayyaf.homecare.requests;

import com.example.sayyaf.homecare.accounts.User;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

public class RequestController {

    public static void acceptRequest(DatabaseReference ref, String email, String id, User currentUser) {
        addFriendForCurrentUser(ref, email, id, currentUser);
        addFriendForRequestSender(ref, id, currentUser);
        removeRequest(ref, id, currentUser);
        removeSentRequest(ref, id, currentUser);
        addChatDatabaseRequestSender(ref, id, currentUser);
        addChatDatabaseCurrentUser(ref, id, currentUser);
    }

    public static void declineRequest(DatabaseReference ref, String id, User currentUser) {
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

    public static void removeUser(DatabaseReference ref, String otherId, String currentId) {
        ref.child("User").child(currentId)
                .child("friends")
                .child(otherId)
                .removeValue();

        // remove the common database
        /*ref.child("chatDB")
                .child(user.getChatDatabase().get(uid))
                .removeValue(); */

        ref.child("User").child(currentId)
                .child("chatDatabase")
                .child(otherId)
                .removeValue();

        ref.child("User").child(otherId)
                .child("chatDatabase")
                .child(currentId)
                .removeValue();
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

    private static void removeSentRequest(DatabaseReference ref, String otherId,
                                          User currentUser) {
        ref.child("User")
                .child(otherId)
                .child("requestsSent")
                .child(currentUser.getId())
                .removeValue();
    }

    private static void removeRequest(DatabaseReference ref, String otherId,
                                          User currentUser) {

        ref.child("User")
                .child(currentUser.getId())
                .child("requests")
                .child(otherId)
                .removeValue();
    }

    private static void addChatDatabaseCurrentUser(DatabaseReference ref, String otherId,
                                                   User currentUser) {
        long date = new Date().getTime();

        ref.child("User")
                .child(currentUser.getId())
                .child("chatDatabase")
                .push();

        ref.child("User")
                .child(currentUser.getId())
                .child("chatDatabase")
                .child(otherId)
                .setValue(currentUser.getId() + date + otherId);
    }

    private static void addChatDatabaseRequestSender(DatabaseReference ref, String otherId,
                                                     User currentUser) {

        long date = new Date().getTime();

        ref.child("User")
                .child(otherId)
                .child("chatDatabase")
                .push();

        ref.child("User")
                .child(otherId)
                .child("chatDatabase")
                .child(currentUser.getId())
                .setValue(currentUser.getId()+ date + otherId);
    }



}
