package com.example.sayyaf.homecare.requests;

import com.example.sayyaf.homecare.User;

import java.util.ArrayList;

public interface RequestsUserListCallback {
    void onCallback(ArrayList<User> requests);
}
