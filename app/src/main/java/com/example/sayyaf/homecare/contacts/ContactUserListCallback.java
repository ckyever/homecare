package com.example.sayyaf.homecare.contacts;

import com.example.sayyaf.homecare.accounts.User;
import java.util.ArrayList;

public interface ContactUserListCallback {
    void onCallback(ArrayList<User> friends);
}

