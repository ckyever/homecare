package com.example.sayyaf.homecare.requests;

import com.example.sayyaf.homecare.accounts.User;
import com.google.firebase.database.DatabaseReference;

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

}
