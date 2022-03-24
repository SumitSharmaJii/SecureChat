package com.ss.securechat.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestCoordinator;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ss.securechat.Adapters.dbMessagesAdapter;

import com.ss.securechat.Database.AppDatabase;
import com.ss.securechat.Database.dbMessage;
import com.ss.securechat.R;
import com.ss.securechat.databinding.ActivityChatBinding;
import com.ss.securechat.encryption.AES;
import com.ss.securechat.mvvmUtil.ContactsViewModel;
import com.ss.securechat.mvvmUtil.MessagesViewModel;
import com.ss.securechat.util.SerializeUtil;

import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ChatActivity extends AppCompatActivity {
    private MessagesViewModel messagesViewModel;
    private ContactsViewModel contactsViewModel;

    FirebaseDatabase database;
    ValueEventListener firebaseListner;

    ActivityChatBinding binding;
    dbMessagesAdapter msgsAdapter;

    AppDatabase db;
    String senderRoom, receiverRoom;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String receiverUId = intent.getStringExtra("uid");
        String token = intent.getStringExtra("token");
        String senderUId = FirebaseAuth.getInstance().getUid();


        senderRoom = senderUId + receiverUId;
        receiverRoom = receiverUId + senderUId;

        database = FirebaseDatabase.getInstance();
        db = AppDatabase.getDBInstance(getApplicationContext());

        msgsAdapter = new dbMessagesAdapter();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        binding.recyclerView.setLayoutManager(linearLayoutManager);
        binding.recyclerView.setAdapter(msgsAdapter);

        messagesViewModel = new ViewModelProvider(this).get(MessagesViewModel.class);
        messagesViewModel.getAllMsgs(senderRoom).observe(this, new Observer<List<dbMessage>>() {
            @Override
            public void onChanged(@Nullable List<dbMessage> dbMessages) {
                msgsAdapter.setMsgs(dbMessages);
                //scroll to last
                try {
                    linearLayoutManager.setStackFromEnd(true);
                    binding.recyclerView.smoothScrollToPosition(msgsAdapter.getItemCount() - 1);
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        });

        //update updated profile pic
        contactsViewModel = new ViewModelProvider(this).get(ContactsViewModel.class);
        database.getReference().child("Users").child(receiverUId).child("profilePic")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String url = snapshot.getValue(String.class);
                        contactsViewModel.updatepic(url,receiverUId);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        //update updated name
        contactsViewModel = new ViewModelProvider(this).get(ContactsViewModel.class);
        database.getReference().child("Users").child(receiverUId).child("name")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String name = snapshot.getValue(String.class);
                        contactsViewModel.updateName(name,receiverUId);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


        //swipe to delete msg
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                messagesViewModel.delete(msgsAdapter.getMsgAt(viewHolder.getAdapterPosition())); // deleting  msg
                Toast.makeText(ChatActivity.this,"Message deleted",Toast.LENGTH_SHORT).show();
            }
        }).attachToRecyclerView(binding.recyclerView);


        // message sending
        binding.sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msgTxt = binding.typeMessage.getText().toString().trim();

                if (msgTxt.equals(""))
                    Toast.makeText(ChatActivity.this, "Message can't be left blank", Toast.LENGTH_SHORT).show();

                else {
                    //setting values to msg
                    Date currDate = new Date();
                    dbMessage msg = new dbMessage();
                    msg.setMsgType("Sent");
                    msg.setMsg(msgTxt);
                    msg.setSenderID(senderUId);
                    msg.setChatRoom(senderRoom);
                    msg.setTimestamp(currDate.getTime());
                    //async add msg to database
                    messagesViewModel.insert(msg);

                    binding.typeMessage.setText(""); //clear text message

                    //msg to byte array
                    byte[] msgBytes = null;
                    try {
                        msgBytes = SerializeUtil.serialize(msg);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    //encrypting
                    String aesKey = db.firstMeetDao().getAesKey(receiverUId);
                    String encryptedMsg = "";
                    try {
                        encryptedMsg = AES.encrypt(msgBytes, Base64.decode(aesKey, Base64.DEFAULT));
                    } catch (Exception e) {
                        Toast.makeText(ChatActivity.this, "error encrypting", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }

                    //encrypted msg to firebase
                    database.getReference().child("chats")
                            .child(senderRoom)
                            .child("messages")
                            .push()
                            .setValue(encryptedMsg).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            //send cloud notification
                            SharedPreferences prefs = getSharedPreferences("My_Details", MODE_PRIVATE);
                            String myName = prefs.getString("MyName", "");
                            sendNotification(myName,msgTxt,token);
                        }
                    });


                }
            }
        });

        binding.addAttatchment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(ChatActivity.this, "Feature is under development", Toast.LENGTH_SHORT).show();
            }
        });
        binding.clickPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ChatActivity.this, "Feature is under development", Toast.LENGTH_SHORT).show();
            }
        });


        getSupportActionBar().setTitle(name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = getIntent();
        String receiverUId = intent.getStringExtra("uid");

        //Message receiving
        firebaseListner = database.getReference().child("chats")
                .child(receiverRoom)
                .child("messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {


                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            String encryptedMsg = snapshot1.getValue(String.class);

                            //decrypt
                            String aesKey = db.firstMeetDao().getAesKey(receiverUId); //getting agreedkey to decrypt

                            byte[] decryptedMsg = null;
                            try {
                                decryptedMsg = AES.decrypt(encryptedMsg.getBytes("UTF-8"), Base64.decode(aesKey, Base64.DEFAULT));
                            } catch (Exception e) {
                                Toast.makeText(ChatActivity.this, "error decrypting", Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }

                            //deserialise decrypted data to msg class object
                            dbMessage msg = null;
                            try {
                                msg = SerializeUtil.deserialize(decryptedMsg);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }


                            try{
                            msg.setMsgType("Received");
                            msg.setChatRoom(senderRoom);
                            //async add msg to database
                            messagesViewModel.insert(msg);
                            } catch (Exception e){
                                Toast.makeText(ChatActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }

                        }
                        snapshot.getRef().removeValue();// remove these msgs from firebase

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseListner != null) {
            database.getReference().child("chats")
                    .child(receiverRoom)
                    .child("messages").removeEventListener(firebaseListner);
        }
    }

    //using volley, sending requests
    void sendNotification(String name, String message, String token) {
        try {
            RequestQueue queue = Volley.newRequestQueue(this);
            String url = "https://fcm.googleapis.com/fcm/send";
            JSONObject data = new JSONObject();
            data.put("title", name);
            data.put("body", message);
            //data.put("sound","default");
            JSONObject notificationData = new JSONObject();
            notificationData.put("notification",data);
            notificationData.put("to",token);
            JsonObjectRequest request = new JsonObjectRequest(url, notificationData
                    , new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> map = new HashMap<>();
                    //this key is from firebase cloud messaging- server key
                    //insert 'Key=' in front of key
                    String key = "Key=AAAAM6qKQX0:APA91bG6zk9N4-tq1a-PsUsb22lBBMniyXXT_cWZ3awkrxYlLGCsITqmq4zJd2vP_4SLBzfiywT7rBa2z3FgTGF8p--KdMEsQw_6yY3LBhdK8EO559-aT6_kOK4lXVnj1N9LBrNt0FOz";
                    map.put("Authorization",key);
                    map.put("Content-Type","application/json");
                    return map;
                }
            };
            queue.add(request);


        } catch (Exception e){
            e.printStackTrace();
        }


    }


    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}