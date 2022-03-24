package com.ss.securechat.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ss.securechat.Activities.ChatActivity;
import com.ss.securechat.Database.dbContact;
import com.ss.securechat.R;
import com.ss.securechat.databinding.RecentConversationsBinding;
import java.util.ArrayList;
import java.util.List;

public class dbContactsAdapter extends RecyclerView.Adapter<dbContactsAdapter.contactsViewHolder> {

    private Context context;
    private List<dbContact> contacts = new ArrayList<>();
    public dbContactsAdapter( Context context){
        this.context=context;

    }

    @NonNull
    @Override
    public contactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recent_conversations,parent,false);
        return new contactsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull dbContactsAdapter.contactsViewHolder holder, int position) {
        dbContact contact = contacts.get(position);

        holder.binding.contactName.setText(contact.getContactName());

        Glide.with(context).load(contact.getProfilePic())
                .placeholder(R.drawable.ic_round_account)
                .into(holder.binding.contactProfileImg);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("name", contact.getContactName());
                intent.putExtra("uid", contact.getUid());
                intent.putExtra("token", contact.getToken());
                context.startActivity(intent);

            }
        });

    }

    //to get update from LiveData
    public void setContacts(List<dbContact> contacts){
        this.contacts = contacts;
        notifyDataSetChanged();
    }

    public String getContactUidAt(int position){
        return contacts.get(position).getUid();
    }

    public  boolean isPresent(String uid){
        for (dbContact contact: contacts) {
            if(contact.getUid().equals(uid)){
                return true;
            }
        }
        return false;
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public class contactsViewHolder extends RecyclerView.ViewHolder{

        RecentConversationsBinding binding;

        public contactsViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = RecentConversationsBinding.bind(itemView);
        }
    }
}
