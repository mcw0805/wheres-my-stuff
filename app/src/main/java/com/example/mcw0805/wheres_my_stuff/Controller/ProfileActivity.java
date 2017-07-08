package com.example.mcw0805.wheres_my_stuff.Controller;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.ViewSwitcher;

import com.example.mcw0805.wheres_my_stuff.Model.User;
import com.google.firebase.auth.FirebaseAuth;

import com.example.mcw0805.wheres_my_stuff.R;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Map;

/**
 * Controller for showing a user's profile page.
 *
 * @author Melanie Hall
 */

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    /*
        Widgets for the profile activity.
    */
    private ToggleButton editToggleButton;
    private Button back_button;
    private EditText nicknameEdit;
    private TextView nicknameTextView;
    private EditText introductionEdit;
    private TextView introductionTextView;
    private TextView numberSubmissionTextView;
    private ViewSwitcher nicknameViewSwitcher;
    private ViewSwitcher introductionViewSwitcher;

    private String nickname;
    private Long itemCount;

    /*
        Firebase authorization
     */
    private FirebaseAuth mAuth;
    private FirebaseUser currUser;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private boolean isAuthListenerSet = false;
    private DatabaseReference userRef = User.getUserRef();
    private String currUserUID;

    private static final String TAG = "ProfileActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //Firebase authorization
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        currUser = mAuth.getCurrentUser();
        currUserUID = currUser.getUid();

        //instantiate buttons
        editToggleButton = (ToggleButton) findViewById(R.id.editToggleBtn);
        back_button = (Button) findViewById(R.id.back_button);
        //instantiate all the fields related to editing/viewing the nickname of the user
        nicknameEdit = (EditText) findViewById(R.id.nicknameEdit);
        nicknameTextView = (TextView) findViewById(R.id.nicknameText);
        nicknameViewSwitcher = (ViewSwitcher) findViewById(R.id.viewSwitcherNickname);

        //instantiate all the fields related to editing/viewing the intro/bio of the user
        introductionEdit = (EditText) findViewById(R.id.introductionEdit);
        introductionTextView = (TextView) findViewById(R.id.introductionText);

        introductionViewSwitcher = (ViewSwitcher) findViewById(R.id.viewSwitcherIntroduction);

        numberSubmissionTextView = (TextView) findViewById(R.id.num_items_posted);

        //listeners for the buttons
        editToggleButton.setOnClickListener(this);
        back_button.setOnClickListener(this);

        retrieveUserInfo();
        editToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                nicknameViewSwitcher.requestFocus();
                final String newNickname = nicknameEdit.getText().toString();
                String newIntro = introductionEdit.getText().toString();

                if (isChecked) { //edit is clicked (i.e. button is "on")
                    nicknameViewSwitcher.showPrevious(); //shows EditText
                    nicknameTextView.setText(newNickname);


                    introductionViewSwitcher.showPrevious();
                    introductionTextView.setText(newIntro);

                } else { //back to TextView
                    nicknameViewSwitcher.showNext();
                    nicknameTextView.setText(newNickname);

                    if (!nickname.equals(newNickname)) {
                        resetName(newNickname);

                    }

                    introductionViewSwitcher.showNext();
                    introductionTextView.setText(newIntro);
                }
            }
        });


    }


    @Override
    protected void onStart() {
        super.onStart();
        if (!isAuthListenerSet) {
            FirebaseAuth.getInstance().addAuthStateListener(mAuthListener);
            isAuthListenerSet = true;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(mAuthListener);
            isAuthListenerSet = false;
        }
    }


    @Override
    public void onClick(View view) {
        if (view == back_button) {
            Intent intent = new Intent(ProfileActivity.this, Dashboard.class);
            ProfileActivity.this.startActivity(intent);
        }
        //hides keyboard when done editing
        if (view == editToggleButton) {
            if (!editToggleButton.isChecked()) {
                hideSoftKeyboard(view);
            }
        }


    }

    /**
     * Hides the soft keyboard.
     * <p>
     * Called when EditText fields are not in focus.
     *
     * @param v view of the current focus
     */
    private void hideSoftKeyboard(View v) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    private void retrieveUserInfo() {
        userRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Map<String, Object> userDataMap = (Map<String, Object>) dataSnapshot.getValue();

                String uid = (String) userDataMap.get("uid");
                nickname = (String) userDataMap.get("name");
                itemCount = (Long) userDataMap.get("itemCount");

                if (uid.equals(currUserUID)) {
                    nicknameTextView.setText(nickname);
                    nicknameEdit.setText(nickname);
                    numberSubmissionTextView.setText(itemCount.toString());
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void resetName(final String newName) {

        final DatabaseReference currUserRef = userRef.child(currUserUID);
        Log.d("INSIDE RESET", currUserRef.toString());
        final DatabaseReference nameRef = currUserRef.child("name");

        currUserRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                final String key = dataSnapshot.getKey();
                String val = dataSnapshot.getValue().toString();
                if (key.equals("name")) {

                    Log.d("INSIDE RESET", "Before reset- " + key + " " + val);

                    nameRef.setValue(newName);

                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if (!nickname.equals(newName)) {
                    Log.d("INSIDE RESET", "After reset- " + newName);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
        finish();
    }


}
