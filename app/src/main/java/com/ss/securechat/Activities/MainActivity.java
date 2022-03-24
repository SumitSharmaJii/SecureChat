package com.ss.securechat.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.hbb20.CountryCodePicker;
import com.ss.securechat.Adapters.dbContactsAdapter;
import com.ss.securechat.Database.dbContact;
import com.ss.securechat.Database.dbFirstMeet;

import com.ss.securechat.Models.User;
import com.ss.securechat.R;

import com.ss.securechat.databinding.ActivityMainBinding;
import com.ss.securechat.encryption.AES;
import com.ss.securechat.encryption.RSA;
import com.ss.securechat.mvvmUtil.ContactsViewModel;
import com.ss.securechat.mvvmUtil.FirstmeetViewModel;
import com.ss.securechat.mvvmUtil.MessagesViewModel;

import java.util.HashMap;
import java.util.List;

import javax.crypto.SecretKey;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    FirebaseDatabase database;
    ValueEventListener firebaseListner;
    dbContactsAdapter contactsAdapter;
    ProgressDialog progressDialog;
    String myUid = FirebaseAuth.getInstance().getUid();
    private ContactsViewModel contactsViewModel;
    private MessagesViewModel messagesViewModel;
    private FirstmeetViewModel firstmeetViewModel;
    CountryCodePicker ccp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ccp = binding.ccp;

        database = FirebaseDatabase.getInstance();

        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Searching for user");
        progressDialog.setCancelable(false);


        contactsAdapter = new dbContactsAdapter(this);
        //binding.recyclerView.setLayoutManager(new LinearLayoutManager(this)); //done in xml already i.e;app:layoutManager="androidx.recyclerview.widget.LinearLayoutManager"
        binding.recyclerView.setAdapter(contactsAdapter);

        contactsViewModel = new ViewModelProvider(this).get(ContactsViewModel.class);
        messagesViewModel = new ViewModelProvider(this).get(MessagesViewModel.class);
        firstmeetViewModel = new ViewModelProvider(this).get(FirstmeetViewModel.class);

        contactsViewModel.getAllContacts().observe(this, new Observer<List<dbContact>>() {
            @Override
            public void onChanged(@Nullable List<dbContact> contacts) {
                contactsAdapter.setContacts(contacts);
            }
        });

        //swipe to delete contact
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                String userUid = contactsAdapter.getContactUidAt(viewHolder.getAdapterPosition());
                String myUid = FirebaseAuth.getInstance().getUid();
                String chatroom = myUid+userUid;
                contactsViewModel.delete(userUid); //deleting from contact table
                messagesViewModel.deleteChatroomMsgs(chatroom); // deleting all msgs of this user from messgae table
                firstmeetViewModel.delete(userUid); //delete from first meet
                Toast.makeText(MainActivity.this,"Contact and it's data deleted",Toast.LENGTH_SHORT).show();
            }
        }).attachToRecyclerView(binding.recyclerView);


        //add new contact by searching and send aes key using rsa
        binding.searchPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ccp.registerCarrierNumberEditText(binding.inputMobNo);
                String phnNoWithCountryCode =ccp.getFullNumberWithPlus().trim();
                if (!phnNoWithCountryCode.isEmpty()) {
                    binding.inputMobNo.setText("");
                    progressDialog.show();

                    //search for given phone number in firebase
                    Query query = database.getReference().child("Users").orderByChild("phnNo")
                            .equalTo(phnNoWithCountryCode);
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if (!snapshot.exists()) {
                                progressDialog.dismiss();
                                Toast.makeText(MainActivity.this, phnNoWithCountryCode + " is not using app", Toast.LENGTH_SHORT).show();
                            } else {

                                for (DataSnapshot snapshot1 : snapshot.getChildren()) {

                                    User user = snapshot1.getValue(User.class);
                                    String userUid = user.getuId();

                                    //check if contact with given uid is already present or uid is mine
                                    if (!myUid.equals(userUid) && !contactsAdapter.isPresent(userUid) ) {

                                        String RsaPublicKey = user.getPublicKey();
                                        SecretKey aesKey = AES.getSecretKey();
                                        byte[] aesKeyBytes = aesKey.getEncoded();
                                        String aesKeyString = Base64.encodeToString(aesKeyBytes, Base64.DEFAULT);
                                        String agreedKey = RSA.encryptRSAToString(aesKeyString, RsaPublicKey);

                                        dbFirstMeet fM = new dbFirstMeet();
                                        fM.setUserUid(userUid);
                                        fM.setAesKey(aesKeyString);
                                        //async add to firstmeet table
                                        //Fm is passed by ref..if changed later then changes may appear on result
                                        firstmeetViewModel.insert(fM);

                                        dbContact contact = new dbContact();
                                        contact.setPhoneNo(user.getPhnNo());
                                        contact.setContactName(user.getName());
                                        contact.setUid(user.getuId());
                                        contact.setToken(user.getToken());
                                        contact.setProfilePic(user.getProfilePic());
                                        //async
                                        contactsViewModel.insert(contact);

                                        //creating new fM2...
                                        dbFirstMeet fM2 = new dbFirstMeet();
                                        //send firstmeet request to firebase
                                        fM2.setUserUid(myUid); // this could have changed fM value because that"s on separate thread
                                        fM2.setAesKey(agreedKey);
                                        database.getReference()
                                                .child("NewRequests")
                                                .child(userUid)
                                                .push()
                                                .setValue(fM2);

                                        progressDialog.dismiss();
                                        Toast.makeText(MainActivity.this, "Contact added", Toast.LENGTH_SHORT).show();

                                    } else {
                                        progressDialog.dismiss();
                                        Toast.makeText(MainActivity.this, "User already in contact list", Toast.LENGTH_SHORT).show();

                                    }

                                }

                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }

                    });

                } else {
                    Toast.makeText(MainActivity.this, "Phone number cannot be left blank.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();


        //add new users from requests and get aes key
        firebaseListner = database.getReference().child("NewRequests").child(myUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {

                            dbFirstMeet fM = snapshot1.getValue(dbFirstMeet.class);

                            String requestFromUser = fM.getUserUid();
                            String encryptedAesKey = fM.getAesKey();

                            SharedPreferences prefs = getSharedPreferences("My_Details", MODE_PRIVATE);
                            String privateRsaKey = prefs.getString("MyPrivateKey", "");

                            String decryptedAesKey = RSA.decryptRSAToString(encryptedAesKey, privateRsaKey);
                            fM.setAesKey(decryptedAesKey);

                            //async add to first meet table
                            firstmeetViewModel.insert(fM);

                            //add user to sqlite database
                            database.getReference().child("Users").child(requestFromUser).addListenerForSingleValueEvent(new ValueEventListener() {

                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                    User user = snapshot.getValue(User.class);

                                    dbContact contact = new dbContact();
                                    contact.setPhoneNo(user.getPhnNo());
                                    contact.setContactName(user.getName());
                                    contact.setUid(user.getuId());
                                    contact.setToken(user.getToken());
                                    contact.setProfilePic(user.getProfilePic());
                                    //async
                                    contactsViewModel.insert(contact);


                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                            snapshot.getRef().removeValue();

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    @Override
    protected void onStop() {
        super.onStop();
        //removing firebase listner
        if (firebaseListner != null) {
            database.getReference().child("NewRequests").child(myUid).removeEventListener(firebaseListner);
        }
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case R.id.groups:
                Toast.makeText(this, "Feature under development", Toast.LENGTH_SHORT).show();
                break;

            case R.id.settings:
                Intent intent = new Intent(getApplicationContext(),UpdateProfileActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_right_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
}