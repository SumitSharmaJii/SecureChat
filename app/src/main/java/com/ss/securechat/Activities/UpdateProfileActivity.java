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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ss.securechat.Models.User;
import com.ss.securechat.databinding.ActivityGetMobNumberBinding;
import com.ss.securechat.databinding.ActivityUpdateDetailsBinding;
import com.ss.securechat.encryption.RSA;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

public class UpdateProfileActivity extends AppCompatActivity {

    ActivityUpdateDetailsBinding binding;
    ProgressDialog progressDialog;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    Uri selectedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUpdateDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait, updating details...");
        progressDialog.setCancelable(false);


        binding.profilePic.setOnClickListener(new View.OnClickListener() {
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

                //fetch stored details & save new details
                database.getReference().child("Users").child(auth.getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                progressDialog.show();

                                User user = snapshot.getValue(User.class);
                                String uid = user.getuId();
                                String phone = user.getPhnNo();
                                String publicKey = user.getPublicKey();
                                String token = user.getToken();
                                String name= binding.entername.getText().toString();

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
                                                        User user = new User(uid , name ,phone, publicKey ,imageURL);
                                                        user.setToken(token);

                                                        database.getReference()
                                                                .child("Users")
                                                                .child(uid)
                                                                .setValue(user)
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void unused) {
                                                                        SharedPreferences.Editor editor = getSharedPreferences("My_Details", MODE_PRIVATE).edit();
                                                                        editor.putString("MyName",name);
                                                                        editor.apply(); //apply is asynchronous while commit is synchronous
                                                                        progressDialog.dismiss();
                                                                        finish();

                                                                    }
                                                                });

                                                    }
                                                });

                                        }
                                    });


                                }
                                else{

                                    User userTemp = new User(uid , name ,phone, publicKey ,"No Image");
                                    userTemp.setToken(token);

                                    database.getReference()
                                            .child("Users")
                                            .child(uid)
                                            .setValue(userTemp)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    SharedPreferences.Editor editor = getSharedPreferences("My_Details", MODE_PRIVATE).edit();
                                                    editor.putString("MyName",name);
                                                    editor.apply(); //apply is asynchronous while commit is synchronous

                                                    progressDialog.dismiss();
                                                    finish();

                                                }
                                            });

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

            }
        });

        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if(data!= null){
            if(data.getData()!=null)
                binding.profilePic.setImageURI(data.getData());
            selectedImage=data.getData();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}
