package com.example.sayyaf.homecare.requests;
import com.example.sayyaf.homecare.accounts.User;
import com.google.firebase.database.DatabaseReference;
import java.util.Date;


/** Controller to handle the various forms of database operations related to request processes
 */
public class RequestController {

    /**
     * Used to add friend association to two users and handle the cancelling of friend requests
     * upon accept
     * @param ref reference to Firebase Realtime Database
     * @param email email address of the user to be added
     * @param id   id of the user to be added
     * @param currentUser the user currently signed in
     */
    public static void acceptRequest(DatabaseReference ref, String email, String id, User currentUser) {

        // add user to both users friend list
        addFriendForCurrentUser(ref, email, id, currentUser);
        addFriendForRequestSender(ref, id, currentUser);

        // use time as one of the chat database identifier
        long time = new Date().getTime();

        // add common chat room name to both users chat database list
        addChatDatabaseRequestSender(ref, id, currentUser, time);
        addChatDatabaseCurrentUser(ref, id, currentUser, time);

        // clear up pending requests and requests sent
        removeRequest(ref, id, currentUser);
        removeSentRequest(ref, id, currentUser);
    }

    /**
     * Used to decline a friend request, and clean up the requests and requests sent
     * @param ref reference to Firebase Realtime Database
     * @param id id of the request sender
     * @param currentUser the user currently signed in
     */
    public static void declineRequest(DatabaseReference ref, String id, User currentUser) {

        // clear up pending requests and sents
        removeRequest(ref, id, currentUser);
        removeSentRequest(ref, id, currentUser);
    }

    /**
     * Used to add a request to requestsSent
     * @param ref reference to Firebase Realtime Database
     * @param otherId id of the request receiver
     * @param otherEmail email address of the receiver
     * @param currentId id of the request sender
     */
    public static void addSentRequest(DatabaseReference ref, String otherId, String otherEmail,
                                String currentId) {

        // Add request to current users requests sent
        ref.child("User").child(currentId)
                .child("requestsSent")
                .push();

        ref.child("User").child(currentId)
                .child("requestsSent")
                .child(otherId)
                .setValue(otherEmail);
    }

    /**
     * Used to add a request to requests
     * @param ref reference to Firebase Realtime Database
     * @param otherId id of the request receiver
     * @param currentId id of the request sender
     */
    public static void addReceiverRequest(DatabaseReference ref, String otherId, String currentEmail,
                                    String currentId) {

     // Add request to receivers requests
        ref.child("User").child(otherId)
                .child("requests")
                .push();

        ref.child("User").child(otherId)
                .child("requests")
                .child(currentId)
                .setValue(currentEmail);

    }


    /**
     * Used to remove a friend for the current user
     * @param ref reference to Firebase Realtime Database
     * @param currentUser user currently signed in
     * @param otherUser user to be removed
     */
    public static void removeUser(DatabaseReference ref, User currentUser, User otherUser) {

        //Remove friend association
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

    /**
     * Used to create a friend association from the request sender to current user
     * @param ref reference to Firebase Realtime Database
     * @param otherEmail email of request sender
     * @param otherId id of request sender
     * @param currentUser user currently signed in
     */
    private static void addFriendForCurrentUser(DatabaseReference ref, String otherEmail,
                                               String otherId, User currentUser) {

        ref.child("User").child(currentUser.getId()).child("friends").push();
        ref.child("User").child(currentUser.getId())
                .child("friends")
                .child(otherId)
                .setValue(otherEmail);
    }

    /**
     * Used to create a friend association from the current user to request sender
     * @param ref
     * @param otherId
     * @param currentUser
     */
    private static void addFriendForRequestSender(DatabaseReference ref, String otherId,
                                                  User currentUser) {

        ref.child("User").child(otherId).child("friends").push();
        ref.child("User")
                .child(otherId)
                .child("friends")
                .child(currentUser.getId())
                .setValue(currentUser.getEmail());
    }

    /**
     * Used to remove a sent friend request from requestsSent
     * @param ref reference to Firebase Realtime Database
     * @param otherId id of request receiver
     * @param currentUser user currently signed in
     */
    public static void removeSentRequest(DatabaseReference ref, String otherId,
                                          User currentUser) {
        ref.child("User")
                .child(otherId)
                .child("requestsSent")
                .child(currentUser.getId())
                .removeValue();
    }

    /**
     * Used to remove a received friend request
     * @param ref reference to Firebase Realtime Database
     * @param otherId id of request receiver
     * @param currentUser user currently signed in
     */
    public static void removeRequest(DatabaseReference ref, String otherId,
                                          User currentUser) {

        ref.child("User")
                .child(currentUser.getId())
                .child("requests")
                .child(otherId)
                .removeValue();
    }

    /**
     * Create a reference to the shared database between two friends for the request receiver
     * @param ref reference to Firebase Realtime Database
     * @param otherId id of request receiver
     * @param currentUser user currently signed in
     * @param time current time
     */
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

    /**
     * Create a reference to the shared database between two friends for the request sender
     * @param ref reference to Firebase Realtime Database
     * @param otherId id of request receiver
     * @param currentUser user currently signed in
     * @param time current time
     */
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

   /**
     * On removal of friend, remove the shared database between them
     * @param ref reference to Firebase Realtime Database
     * @param otherUser  friend to be removed
     * @param currentUser user currently signed in
     */

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
