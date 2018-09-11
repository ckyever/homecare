package com.example.sayyaf.homecare.requests;

import com.example.sayyaf.homecare.User;
import com.google.firebase.database.DatabaseReference;

import java.util.Date;

public class RequestController {

        public static void acceptRequest(DatabaseReference ref, String email, String id, User currentUser) {
            ref.child("User").child(currentUser.getId()).child("friends").push();
            ref.child("User").child(currentUser.getId())
                    .child("friends")
                    .child(id)
                    .setValue(email);

            ref.child("User").child(id).child("friends").push();
            ref.child("User")
                    .child(id)
                    .child("friends")
                    .child(currentUser.getId())
                    .setValue(currentUser.getEmail());
            ref.child(currentUser.getId()).child("requests").child(id).removeValue();
            ref.child(id).child("requestsSent").child(currentUser.getId()).removeValue();

            long date = new Date().getTime();

            ref.child("User")
                    .child(currentUser.getId())
                    .child("chatDatabase")
                    .push();

            ref.child("User")
                    .child(currentUser.getId())
                    .child("chatDatabase")
                    .child(id)
                    .setValue(currentUser.getId() + date + id);

            ref.child("User")
                    .child(id)
                    .child("chatDatabase")
                    .push();

            ref.child(id)
                    .child("chatDatabase")
                    .child(currentUser.getId())
                    .setValue(currentUser.getId()+ date + currentUser.getId());
            //
        }

    }
