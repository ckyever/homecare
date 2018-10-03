package com.example.sayyaf.homecare.contacts;

import com.example.sayyaf.homecare.accounts.User;
import java.util.ArrayList;

public interface ContactUserListCallback {

    void onCurrentUserCallback(User this_device, String starter, boolean showResult);

    void onContactsCallback(ArrayList<User> friends);
}

