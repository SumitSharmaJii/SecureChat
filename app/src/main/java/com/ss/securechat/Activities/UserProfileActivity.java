package com.ss.securechat.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ss.securechat.Models.User;

import com.ss.securechat.databinding.ActivityUserProfileBinding;
import com.ss.securechat.encryption.RSA;


import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;


public class UserProfileActivity extends AppCompatActivity {
    ActivityUserProfileBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    Uri selectedImage;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait, saving details");
        progressDialog.setCancelable(false);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        binding.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 45);
            }
        });

        binding.buttonSaveUsrDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = binding.entername.getText().toString();
                if(name.isEmpty()) {
                    binding.entername.setError("Name cannot be left blank.");
                    return;
                }

                progressDialog.show();
                if(selectedImage != null){
                    StorageReference reference = storage.getReference().child("Profiles").child(auth.getUid());
                    reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful())
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String imageURL = uri.toString();
                                        String uid =auth.getUid();
                                        String phone = auth.getCurrentUser().getPhoneNumber();
                                        String name= binding.entername.getText().toString();

                                        KeyPair kp = RSA.getKeyPair();

                                        PublicKey publicKey = kp.getPublic();
                                        byte[] publicKeyBytes = publicKey.getEncoded();
                                        String publicKeyBytesBase64 = new String(Base64.encode(publicKeyBytes, Base64.DEFAULT));

                                        PrivateKey privateKey = kp.getPrivate();
                                        byte[] privateKeyBytes = privateKey.getEncoded();
                                        String privateKeyBytesBase64 = new String(Base64.encode(privateKeyBytes, Base64.DEFAULT));

                                        SharedPreferences.Editor editor = getSharedPreferences("My_Details", MODE_PRIVATE).edit();
                                        editor.putString("MyPrivateKey" , privateKeyBytesBase64);
                                        editor.putString("MyName",name);
                                        editor.apply(); //apply is asynchronous while commit is synchronous



                                        User user = new User(uid , name ,phone, publicKeyBytesBase64 ,imageURL);
                                        //generate token for user and send data to firebase
                                        FirebaseMessaging.getInstance().getToken()
                                                .addOnSuccessListener(new OnSuccessListener<String>() {
                                                    @Override
                                                    public void onSuccess(String token) {
                                                        user.setToken(token);

                                                        database.getReference()
                                                                .child("Users")
                                                                .child(uid)
                                                                .setValue(user)
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void unused) {

                                                                        progressDialog.dismiss();
                                                                        Intent intent = new Intent(UserProfileActivity.this, MainActivity.class);
                                                                        startActivity(intent);
                                                                        finish();

                                                                    }
                                                                });
                                                    }
                                                });

                                    }
                                });

                        }
                    });


                }
                else{

                    String uid =auth.getUid();
                    String phone = auth.getCurrentUser().getPhoneNumber();

                    KeyPair kp = RSA.getKeyPair();

                    PublicKey publicKey = kp.getPublic();
                    byte[] publicKeyBytes = publicKey.getEncoded();
                    String publicKeyBytesBase64 = new String(Base64.encode(publicKeyBytes, Base64.DEFAULT));

                    PrivateKey privateKey = kp.getPrivate();
                    byte[] privateKeyBytes = privateKey.getEncoded();
                    String privateKeyBytesBase64 = new String(Base64.encode(privateKeyBytes, Base64.DEFAULT));
                    SharedPreferences.Editor editor = getSharedPreferences("My_Details", MODE_PRIVATE).edit();
                    editor.putString("MyPrivateKey" , privateKeyBytesBase64);
                    editor.putString("MyName",name);
                    editor.apply(); //apply is asynchronous while commit is synchronous

                    User user = new User(uid , name ,phone, publicKeyBytesBase64 ,"No Image");

                    //generate token for user and send data to firebase
                    FirebaseMessaging.getInstance().getToken()
                            .addOnSuccessListener(new OnSuccessListener<String>() {
                                @Override
                                public void onSuccess(String token) {
                                    user.setToken(token);

                                    database.getReference()
                                            .child("Users")
                                            .child(uid)
                                            .setValue(user)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {

                                                    progressDialog.dismiss();
                                                    Intent intent = new Intent(UserProfileActivity.this, MainActivity.class);
                                                    startActivity(intent);
                                                    finish();

                                                }
                                            });
                                }
                            });

                }
            }
        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if(data!= null){
            if(data.getData()!=null)
                binding.imageView.setImageURI(data.getData());
                selectedImage=data.getData();
        }
    }


}