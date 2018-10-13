package com.example.sayyaf.homecare;


import android.app.Activity;
import android.content.ComponentName;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.filters.LargeTest;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.support.test.runner.AndroidJUnit4;

import org.hamcrest.core.IsNot;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import com.example.sayyaf.homecare.accounts.AccountRegisterActivity;
import com.example.sayyaf.homecare.accounts.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class TestAccountsRegistrationActivity {

    @Rule
   public IntentsTestRule<AccountRegisterActivity> mActivityRule = new IntentsTestRule<>(AccountRegisterActivity.class);
}
