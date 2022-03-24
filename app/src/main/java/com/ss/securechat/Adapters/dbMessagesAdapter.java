package com.ss.securechat.Adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.ss.securechat.Database.dbMessage;
import com.ss.securechat.R;
import com.ss.securechat.databinding.ReceiveMsgBinding;
import com.ss.securechat.databinding.SentMsgBinding;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class dbMessagesAdapter extends RecyclerView.Adapter {

    private List<dbMessage> msgs = new ArrayList<>();

    final int MSG_SENT = 1;
    final int MSG_RECEIVE = 2;


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        if(viewType == MSG_SENT){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sent_msg, parent, false);
            return new SentMsgViewHolder(view);
        }
        else{
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.receive_msg, parent, false);
            return new ReceivedMsgViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        dbMessage msg = msgs.get(position);
        if(msg.getMsgType()!=null && msg.getMsgType().equals("Sent")){
            return MSG_SENT;
        }
        else {
            return MSG_RECEIVE;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        dbMessage msg = msgs.get(position);
        if(holder.getClass() == SentMsgViewHolder.class){
            SentMsgViewHolder viewHolder = (SentMsgViewHolder)holder;
            viewHolder.binding.sendermsg.setText(msg.getMsg());

            //for time
            long currentTimestamp = msg.getTimestamp();
            DateFormat simpleTime = new SimpleDateFormat("HH:mm");
            Date res = new Date(currentTimestamp);
            String time = simpleTime.format(res);
            viewHolder.binding.sendtime.setText(time);

        }
        else{
            ReceivedMsgViewHolder viewHolder = (ReceivedMsgViewHolder)holder;
            viewHolder.binding.receivermsg.setText(msg.getMsg());

            //for time
            long currentTimestamp = msg.getTimestamp();
            DateFormat simpleTime = new SimpleDateFormat("HH:mm");
            Date res = new Date(currentTimestamp);
            String time = simpleTime.format(res);
            viewHolder.binding.receivetime.setText(time);
        }

    }

    public class SentMsgViewHolder extends RecyclerView.ViewHolder{

        SentMsgBinding binding;
        public SentMsgViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = SentMsgBinding.bind(itemView);
        }
    }

    public class ReceivedMsgViewHolder extends RecyclerView.ViewHolder{

        ReceiveMsgBinding binding;
        public ReceivedMsgViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ReceiveMsgBinding.bind(itemView);
        }
    }


    //to get update from LiveData
    public void setMsgs(List<dbMessage> msgs){
        this.msgs = msgs;
        notifyDataSetChanged();
    }

    public dbMessage getMsgAt(int position){
        return  msgs.get(position);
    }


    @Override
    public int getItemCount()
    {
        try{

            return msgs.size();
        }
        catch (Exception e){

            e.printStackTrace();
            return 0;
        }
    }



}
