package com.ss.securechat.mvvmUtil;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.ss.securechat.Database.dbMessage;

import java.util.List;

public class MessagesViewModel extends AndroidViewModel {
    private MessageRepository repository;
    //private LiveData<List<dbMessage>> allMsgs;

    public MessagesViewModel(@NonNull Application application) {
        super(application);
        repository = new MessageRepository(application);
        //allMsgs = repository.getAllMsgs();
    }

    public void insert(dbMessage msg){
        repository.insert(msg);
    }

    public void delete(dbMessage msg) {repository.delete(msg);}

    public void deleteChatroomMsgs(String chatroom){
        repository.deleteChatroomMsgs(chatroom);
    }

    public LiveData<List<dbMessage>> getAllMsgs(String room){
        return repository.getAllMsgs(room);
    }
}
