package com.example.sayyaf.homecare;

import java.util.ArrayList;
import java.util.List;

public class Caregiver extends User {

    private List<Assisted> assisted = new ArrayList<>();

    public Caregiver(String name, String email, boolean isCaregiver) {
        super(name, email, isCaregiver);
    }

    public void setAssisted(List<Assisted> assisted) {
        this.assisted = assisted;
    }

    public List<Assisted> getAssisted() {
        return assisted;
    }

    public void addAssisted(Assisted assistedPerson) {
        assisted.add(assistedPerson);
    }
}
