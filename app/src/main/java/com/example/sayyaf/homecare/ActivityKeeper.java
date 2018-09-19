package com.example.sayyaf.homecare;

public class ActivityKeeper {

    private Class backPressActivityClass;

    public ActivityKeeper(Class backPressActivityClass){
        this.backPressActivityClass = backPressActivityClass;
    }

    public Class getBackPressActivityClass() {
        return backPressActivityClass;
    }
}
