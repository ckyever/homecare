package com.example.sayyaf.homecare;


import android.app.Activity;
import android.content.ComponentName;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.filters.LargeTest;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
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
    private Button mCreateUserButton;
    private EditText mNameEditText;
    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private EditText mConfirmPasswordEditText;
    private TextView mLoginTextView;
    private RadioButton mCaregiver;
    private RadioButton mAssistedPerson;
    private Activity activity;

    @Rule
   public IntentsTestRule<AccountRegisterActivity> mActivityRule = new IntentsTestRule<>(AccountRegisterActivity.class);


    @Before
    public void setUp() {
        activity = mActivityRule.getActivity();
        mCreateUserButton = (Button) activity.findViewById(R.id.createUserButton);
        mNameEditText = (EditText) activity.findViewById(R.id.nameEditText);
        mEmailEditText = (EditText)activity.findViewById(R.id.emailEditText);
        mPasswordEditText = (EditText)activity.findViewById(R.id.passwordEditText);
        mConfirmPasswordEditText = (EditText)activity.findViewById(R.id.confirmPasswordEditText);
        mLoginTextView = (TextView) activity.findViewById(R.id.loginTextView);
        mCaregiver = (RadioButton) activity.findViewById(R.id.caregiver);
        mAssistedPerson = (RadioButton) activity.findViewById(R.id.assistedPerson);
    }

    @Test
    public void checkUINotNull() {
        assertThat(mCreateUserButton, notNullValue());
        assertThat(mEmailEditText, notNullValue());
        assertThat(mPasswordEditText, notNullValue());
        assertThat(mNameEditText, notNullValue());
        assertThat(mConfirmPasswordEditText, notNullValue());
        assertThat(mLoginTextView, notNullValue());
        assertThat(mCaregiver, notNullValue());
        assertThat(mAssistedPerson, notNullValue());
    }

    @Test
    public void checkButtonUnenabled() {
        onView(withId(R.id.createUserButton)).perform(click());
        onView(withId(R.id.createUserButton)).check(ViewAssertions.matches(IsNot.not(isEnabled())));
    }

    @Test
    public void checkActivityChangedOnClick() {
        onView(withId(R.id.loginTextView)).perform(click());
        intended(hasComponent(new ComponentName(getTargetContext(),LoginActivity.class)));
    }

    @Test
    public void checkButtonUnenabledFieldsFilled() {
        onView(withId(R.id.emailEditText)).perform(ViewActions.clearText())
                .perform(ViewActions.typeText("test1@gmail.com"),closeSoftKeyboard());
        onView(withId(R.id.passwordEditText)).perform(ViewActions.clearText())
                .perform(ViewActions.typeText("friends12"),closeSoftKeyboard());
        onView(withId(R.id.confirmPasswordEditText)).perform(ViewActions.clearText())
                .perform(ViewActions.typeText("friends12"),closeSoftKeyboard());


        onView(withId(R.id.loginButton)).perform(click());
        onView(withId(R.id.loginButton)).check(ViewAssertions.matches(isEnabled()));
    }

    @Test
    public void checkButtonEnabled() {
        onView(withId(R.id.nameEditText)).perform(ViewActions.clearText())
                .perform(ViewActions.typeText("Testing Name"),closeSoftKeyboard());
        onView(withId(R.id.emailEditText)).perform(ViewActions.clearText())
                .perform(ViewActions.typeText("test1@gmail.com"),closeSoftKeyboard());
        onView(withId(R.id.passwordEditText)).perform(ViewActions.clearText())
                .perform(ViewActions.typeText("friends12"),closeSoftKeyboard());
        onView(withId(R.id.confirmPasswordEditText)).perform(ViewActions.clearText())
                .perform(ViewActions.typeText("friends12"),closeSoftKeyboard());
        onView(withId(R.id.caregiver)).perform(ViewActions.click());
        onView(withId(R.id.createUserButton)).check(ViewAssertions.matches(isEnabled()));
    }

    @Test
    public void checkOnlyOneRadioButtonChecked() {
        onView(withId(R.id.confirmPasswordEditText)).perform(ViewActions.clearText())
                .perform(ViewActions.typeText("friends12"),closeSoftKeyboard());
        onView(withId(R.id.caregiver)).perform(ViewActions.click());
        onView(withId(R.id.assistedPerson)).perform(ViewActions.click());
        assert(!mCaregiver.isChecked());
        assert(mAssistedPerson.isChecked());
    }

    @Test
    public void checkActivityChanged() {
        onView(withId(R.id.loginTextView)).perform(ViewActions.click());
        intended(hasComponent(new ComponentName(getTargetContext(), LoginActivity.class)));
    }


}
