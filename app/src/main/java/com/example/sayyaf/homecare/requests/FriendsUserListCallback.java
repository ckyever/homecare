package com.example.sayyaf.homecare.requests;

import com.example.sayyaf.homecare.accounts.User;

import java.util.ArrayList;

public interface FriendsUserListCallback {
    public void onFriendsCallback(ArrayList<User> requests, User currentUser);
}
