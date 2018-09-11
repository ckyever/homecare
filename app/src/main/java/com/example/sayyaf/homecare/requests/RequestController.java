package com.example.sayyaf.homecare.requests;

import com.example.sayyaf.homecare.User;
import com.google.firebase.database.DatabaseReference;

public class RequestController {

        public static void acceptRequest(DatabaseReference ref, String email, String id, User currentUser) {
            ref.child(currentUser.getId()).child("friends").push();
            ref.child(currentUser.getId())
                    .child("friends")
                    .child(id)
                    .setValue(email);

            ref.child(id).child("friends").push();
            ref.child(id)
                    .child("friends")
                    .child(currentUser.getId())
                    .setValue(currentUser.getEmail());
            ref.child(currentUser.getId()).child("requests").child(id).removeValue();

        }

    }
