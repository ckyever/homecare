package com.example.sayyaf.homecare;

import java.util.ArrayList;
import java.util.List;

public class Assisted extends User {

    List<Caregiver> caregivers = new ArrayList<>();

    public Assisted(String name, String email, boolean isCaregiver) {
        super(name, email, isCaregiver);
    }

}
