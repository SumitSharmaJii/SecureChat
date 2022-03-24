package com.ss.securechat.mvvmUtil;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.ss.securechat.Database.AppDatabase;
import com.ss.securechat.Database.MessageDao;
import com.ss.securechat.Database.dbMessage;
import java.util.List;


public class MessageRepository {
    private MessageDao msgdao;
    //private LiveData<List<dbMessage>> allMsgs;


    public MessageRepository(Application application) {
        AppDatabase db = AppDatabase.getDBInstance(application);
        msgdao = db.messageDao();
        //allMsgs = msgdao.getAllMsgs();
    }


    public void insert(dbMessage msg) {
        new InsertMsgAsyncTask(msgdao).execute(msg);
    }

    public void delete(dbMessage msg){
        new DeleteMsgAsyncTask(msgdao).execute(msg);
    }

    public void deleteChatroomMsgs(String chatRoom) {
        new DeleteAllChatMsgAsyncTask(msgdao).execute(chatRoom);
    }

    //Livedata
    public LiveData<List<dbMessage>> getAllMsgs(String room) {
        return msgdao.getAllMsgs(room);
    }


    //Insert in db async
    private static class InsertMsgAsyncTask extends AsyncTask<dbMessage, Void, Void> {
        private MessageDao msgDao;

        private InsertMsgAsyncTask(MessageDao msgDao) {
            this.msgDao = msgDao;
        }

        @Override
        protected Void doInBackground(dbMessage... dbMessages) {

            msgDao.insertMsg(dbMessages[0]);
            return null;
        }

    }

    //delete a msg from db async
    private static class DeleteMsgAsyncTask extends AsyncTask<dbMessage, Void, Void> {
        private MessageDao msgDao;

        private DeleteMsgAsyncTask(MessageDao msgDao) {
            this.msgDao = msgDao;
        }

        @Override
        protected Void doInBackground(dbMessage... dbMessages) {

            msgDao.delMsg(dbMessages[0]);
            return null;
        }

    }

    //delete all msgs of a chatroom in db async
    private static class DeleteAllChatMsgAsyncTask extends AsyncTask<String, Void, Void> {
        private MessageDao msgDao;

        private DeleteAllChatMsgAsyncTask(MessageDao msgDao) {
            this.msgDao = msgDao;
        }

        @Override
        protected Void doInBackground(String... chatRoom) {
            try {
                if (!isCancelled())
                    msgDao.deleteMsgsOfDeletedUser(chatRoom[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }


}
