package com.example.sayyaf.homecare.requests;

import com.example.sayyaf.homecare.User;

import java.util.ArrayList;
import java.util.HashMap;

public interface RequestsUserListCallback {

    void onRequestsCallback(User currentUser);

    void onFriendsCallback(ArrayList<User> requests, User currentUser);
}
