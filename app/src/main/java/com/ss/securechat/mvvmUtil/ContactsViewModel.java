package com.ss.securechat.mvvmUtil;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.ss.securechat.Database.dbContact;

import java.util.List;

public class ContactsViewModel extends AndroidViewModel {
    private ContactsRepository contactsRepository;
    private LiveData<List<dbContact>> allContacts;

    public ContactsViewModel(@NonNull Application application) {
        super(application);
        contactsRepository = new ContactsRepository(application);
        allContacts = contactsRepository.getAllcontact();

    }

    public void insert(dbContact contact){
        contactsRepository.insert(contact);
    }
    public void updatepic(String url, String uid){
        contactsRepository.updatePic(url, uid);
    }
    public void updateName(String name, String uid){
        contactsRepository.updateName(name, uid);
    }
    public void delete(String uid){
        contactsRepository.delete(uid);
    }
    public LiveData<List<dbContact>> getAllContacts() {
        return allContacts;
    }
}
