package com.ss.securechat.mvvmUtil;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.ss.securechat.Database.AppDatabase;
import com.ss.securechat.Database.ContactDao;

import com.ss.securechat.Database.MessageDao;
import com.ss.securechat.Database.dbContact;


import java.util.List;

public class ContactsRepository {
    private ContactDao contactdao;

    private LiveData<List<dbContact>> allcontact;

    public ContactsRepository(Application application){
        AppDatabase db = AppDatabase.getDBInstance(application);
        contactdao = db.contactDao();
        allcontact = db.contactDao().getAllContacts();
    }

    //Livedata async
    public LiveData<List<dbContact>> getAllcontact(){
        return allcontact;
    }

    public void insert(dbContact contact){
        new InsertContactAsync(contactdao).execute(contact);
    }


    public  void updatePic(String url, String uid){
        new UpdatePicAsync(contactdao, url, uid).start();
    }

    public  void updateName(String name, String uid){
        new UpdateNameAsync(contactdao, name, uid).start();
    }

    public void delete(String uid){
        new DeleteContactAsync(contactdao).execute(uid);
    }


    //insert contact async
    private static class InsertContactAsync extends AsyncTask<dbContact, Void, Void> {
        ContactDao contactDao;

        InsertContactAsync(ContactDao contactDao){
            this.contactDao = contactDao;
        }

        @Override
        protected Void doInBackground(dbContact... contacts) {
            contactDao.insertContact(contacts[0]);
            return null;
        }
    }


    //update profile pic async
    private static class UpdatePicAsync extends Thread{
        private ContactDao contactDao;
        private String url;
        private String uid;

        UpdatePicAsync(ContactDao contactDao,String url,String uid){
            this.contactDao=contactDao;
            this.url=url;
            this.uid=uid;
        }
        public void run(){
            contactDao.updatePic(url,uid);
        }
    }

    //update profile name async
    private static class UpdateNameAsync extends Thread{
        private ContactDao contactDao;
        private String name;
        private String uid;

        UpdateNameAsync(ContactDao contactDao,String name,String uid){
            this.contactDao=contactDao;
            this.name=name;
            this.uid=uid;
        }
        public void run(){
            contactDao.updateName(name,uid);
        }
    }


    //delete contact async
    private static class DeleteContactAsync extends AsyncTask<String, Void, Void> {
        ContactDao contactDao;

        DeleteContactAsync(ContactDao contactDao){
            this.contactDao = contactDao;
        }

        @Override
        protected Void doInBackground(String... strings) {
            contactDao.deleteContact(strings[0]);
            return null;
        }
    }


}
