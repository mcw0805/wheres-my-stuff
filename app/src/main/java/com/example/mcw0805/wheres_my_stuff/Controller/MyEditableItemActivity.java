package com.example.mcw0805.wheres_my_stuff.Controller;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.ViewSwitcher;
import com.example.mcw0805.wheres_my_stuff.Model.FoundItem;
import com.example.mcw0805.wheres_my_stuff.Model.Item;
import com.example.mcw0805.wheres_my_stuff.Model.ItemCategory;
import com.example.mcw0805.wheres_my_stuff.Model.LostItem;
import com.example.mcw0805.wheres_my_stuff.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import java.text.DateFormat;

public class MyEditableItemActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView myItemName;
    private TextView myItemType;
    private TextView myItemDesc;
    private TextView myItemStat;
    private TextView myItemCat;
    private TextView myItemDate;
    private TextView myLostItemReward;

    private EditText itemNameEdit;
    private EditText itemDescEdit;
    private EditText lostItemRewardEdit;

    private Switch itemStatSwitch;

    private Spinner itemCatSpinner;

    private ToggleButton editItemToggleBtn;

    private Button deleteBtn;

    private ViewSwitcher nameViewSwitcher;
    private ViewSwitcher descViewSwitcher;
    private ViewSwitcher catViewSwitcher;
    private ViewSwitcher rewardViewSwitcher;

    private LinearLayout rewardLinLayout;

    private ProgressDialog progressDialog;

    private String itemKey;
    private Item selected;


    /*
    Firebase authorization
 */
    private FirebaseAuth mAuth;
    private FirebaseUser currUser;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private boolean isAuthListenerSet = false;
    private String currUserUID;


    private static final String TAG = "MyEditableItemActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_editable_item);

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

        nameViewSwitcher = (ViewSwitcher) findViewById(R.id.my_item_name_viewSwitcher);
        descViewSwitcher = (ViewSwitcher) findViewById(R.id.my_item_description_viewSwitcher);
        catViewSwitcher = (ViewSwitcher) findViewById(R.id.my_item_category_viewSwitcher);
        rewardViewSwitcher = (ViewSwitcher) findViewById(R.id.my_item_reward_viewSwitcher);

        myItemName = (TextView) findViewById(R.id.my_item_name_text);
        myItemType = (TextView) findViewById(R.id.my_item_type_text);
        myItemDesc = (TextView) findViewById(R.id.my_item_description_text);
        myItemStat = (TextView) findViewById(R.id.my_item_stat_text);
        myItemCat = (TextView) findViewById(R.id.my_item_cat_text);
        myItemDate = (TextView) findViewById(R.id.my_item_post_date_text);

        myLostItemReward = (TextView) findViewById(R.id.my_item_reward_text);

        itemNameEdit = (EditText) findViewById(R.id.my_item_name_edit);
        itemDescEdit = (EditText) findViewById(R.id.my_item_description_edit);
        lostItemRewardEdit = (EditText) findViewById(R.id.my_item_reward_edit);


        itemStatSwitch = (Switch) findViewById(R.id.my_item_status_switch);
        itemStatSwitch.setVisibility(View.INVISIBLE);

        itemCatSpinner = (Spinner) findViewById(R.id.my_item_cat_spinner);
        ArrayAdapter<ItemCategory> categoryAdapter =
                new ArrayAdapter(this, android.R.layout.simple_spinner_item, ItemCategory.values());
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        itemCatSpinner.setAdapter(categoryAdapter);

        editItemToggleBtn = (ToggleButton) findViewById(R.id.edit_item_ToggleBtn);
        editItemToggleBtn.setOnClickListener(this);

        deleteBtn = (Button) findViewById(R.id.deleteBtn);
        deleteBtn.setOnClickListener(this);
        deleteBtn.setVisibility(View.GONE);

        rewardLinLayout = (LinearLayout) findViewById(R.id.reward_lin_layout);

        progressDialog = new ProgressDialog(this);

        Intent intent = getIntent();

        selected = intent.getParcelableExtra("selected");
        itemKey = intent.getStringExtra("userItemPushKey");

        //debug stuff
        boolean x = selected == null;
        Log.d(TAG, x + "");
        boolean yy = itemKey == null;
        Log.d(TAG, itemKey + " MAP");

        if (selected != null) {
            myItemName.setText(selected.getName());
            myItemDesc.setText(selected.getDescription());
            myItemCat.setText(selected.getCategory().toString());
            myItemStat.setText(selected.getStatusString());

            DateFormat df = new java.text.SimpleDateFormat("yyyy MMMM dd hh:mm aaa");
            myItemDate.setText(df.format(selected.getDate()));
            itemStatSwitch.setChecked(selected.getIsOpen());

            if (selected instanceof LostItem) {
                myLostItemReward.setText("$" + ((LostItem) selected).getReward());
                myItemType.setText(((LostItem) selected).getItemType().toString());
            } else if (selected instanceof FoundItem) {
                rewardLinLayout.setVisibility(View.INVISIBLE);
                myItemType.setText(((FoundItem) selected).getItemType().toString());
            }
        }

        editItemToggleBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                final String newItemName = itemNameEdit.getText().toString();
                final String newItemDesc = itemDescEdit.getText().toString();
                final String newItemCat = itemCatSpinner.getSelectedItem().toString();
                final boolean newStat = itemStatSwitch.isChecked();
                //final Integer newItemReward = Integer.parseInt(lostItemRewardEdit.getText().toString());

                if (isChecked) { //edit mode is on
                    nameViewSwitcher.showPrevious();
                    descViewSwitcher.showPrevious();
                    catViewSwitcher.showPrevious();
                    itemStatSwitch.setVisibility(View.VISIBLE);
                    itemCatSpinner.setSelection(ItemCategory.valueOf(myItemCat.getText().toString()).ordinal());
                    deleteBtn.setVisibility(View.VISIBLE);

                } else {
                    nameViewSwitcher.showNext();
                    descViewSwitcher.showNext();
                    catViewSwitcher.showNext();
                    itemStatSwitch.setVisibility(View.INVISIBLE);
                    deleteBtn.setVisibility(View.GONE);
                }

            }
        });


    }


    @Override
    protected void onStart() {
        super.onStart();
        if (!isAuthListenerSet) {
            mAuth.addAuthStateListener(mAuthListener);
            isAuthListenerSet = true;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
            isAuthListenerSet = false;
        }
    }

    @Override
    public void onClick(View v) {

        if (v == deleteBtn) {

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MyEditableItemActivity.this);
            dialogBuilder.setMessage("Are you sure you want to permanently remove your item?");
            dialogBuilder.setCancelable(true);

            dialogBuilder.setPositiveButton(
                    "No",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            dialogBuilder.setNegativeButton(
                    "Yes",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            delete();
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    final Intent mainIntent = new Intent(getApplicationContext(), MyListActivity.class);
                                    mainIntent.putExtra("deleted", "item deleted");
                                    progressDialog.dismiss();
                                    MyEditableItemActivity.this.startActivity(mainIntent);
                                    MyEditableItemActivity.this.finish();
                                }
                            }, 1500);
                        }
                    });

            AlertDialog alert11 = dialogBuilder.create();
            alert11.show();

        }
    }

    /**
     * Deletes the item from the database
     */
    private void delete() {
        DatabaseReference itemsRef = null;
        progressDialog.setMessage("Please wait until your item is removed...");
        progressDialog.show();

        if (selected != null) {
            if (selected instanceof LostItem) {
                itemsRef = LostItem.getLostItemsRef().child(itemKey);
                itemsRef.removeValue();
                return;
            } else if (selected instanceof FoundItem) {
                itemsRef = FoundItem.getFoundItemsRef().child(itemKey);
                itemsRef.removeValue();
                return;
            }
        }
    }

}
