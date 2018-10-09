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
public class TestLoginActivity {

    @Rule
    public IntentsTestRule<LoginActivity> mActivityRule = new IntentsTestRule<>(LoginActivity.class);
    Activity activity;
    TextView mRegisterTextView;
    EditText mEmailEditText;
    EditText mPasswordEditText;
    Button mLoginButton;
    private FirebaseAuth mAuth;

    private ProgressBar progressBar;
    private TextView progressBarMsg;

    @Before
    public void setUp() {
        activity = mActivityRule.getActivity();
        mRegisterTextView = (TextView) activity.findViewById(R.id.registerTextView);
        mEmailEditText = (EditText) activity.findViewById(R.id.emailEditText);
        mPasswordEditText = (EditText)activity.findViewById(R.id.passwordEditText);
        mLoginButton = (Button) activity.findViewById(R.id.loginButton);
    }

    @Test
    public void checkUINotNull() {
        assertThat(mRegisterTextView, notNullValue());
        assertThat(mEmailEditText, notNullValue());
        assertThat(mPasswordEditText, notNullValue());
        assertThat(mLoginButton, notNullValue());

    }

    @Test
    public void checkButtonUnenabled() {
        onView(withId(R.id.emailEditText)).perform(ViewActions.clearText())
                .perform(ViewActions.typeText("sayyaf17@hotmail.com"),closeSoftKeyboard());
        onView(withId(R.id.passwordEditText)).perform(ViewActions.clearText())
                .perform(ViewActions.typeText(""),closeSoftKeyboard());
        onView(withId(R.id.loginButton)).perform(click());
        onView(withId(R.id.loginButton)).check(ViewAssertions.matches(IsNot.not(isEnabled())));
    }

    @Test
    public void checkActivityChangedOnClick() {
        onView(withId(R.id.registerTextView)).perform(click());
        intended(hasComponent(new ComponentName(getTargetContext(), AccountRegisterActivity.class)));

    }

    @Test
    public void checkButtonEnabled() {
        onView(withId(R.id.emailEditText)).perform(ViewActions.clearText())
                .perform(ViewActions.typeText("sayyaf17@hotmail.com"),closeSoftKeyboard());
        onView(withId(R.id.passwordEditText)).perform(ViewActions.clearText())
                .perform(ViewActions.typeText("friends12"),closeSoftKeyboard());
        onView(withId(R.id.loginButton)).perform(click());
        onView(withId(R.id.loginButton)).check(ViewAssertions.matches(isEnabled()));
    }

}
