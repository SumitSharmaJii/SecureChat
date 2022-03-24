package com.ss.securechat.mvvmUtil;

import android.app.Application;
import android.os.AsyncTask;

import com.ss.securechat.Database.AppDatabase;
import com.ss.securechat.Database.FirstMeetDao;
import com.ss.securechat.Database.dbFirstMeet;

public class FirstmeetRepository {
    private FirstMeetDao firstMeetDao;

    FirstmeetRepository(Application application){
        AppDatabase db = AppDatabase.getDBInstance(application);
        firstMeetDao = db.firstMeetDao();
    }

    public void insert(dbFirstMeet firstMeet){
        new InsertFirstMeetAsync(firstMeetDao).execute(firstMeet);
    }

    public void delete(String uid){
        new DeleteFirstMeetAsync(firstMeetDao).execute(uid);
    }

    //insert firstmeet async
    private static class InsertFirstMeetAsync extends AsyncTask<dbFirstMeet, Void, Void> {
        FirstMeetDao firstMeetDao;

        InsertFirstMeetAsync(FirstMeetDao firstMeetDao){
            this.firstMeetDao = firstMeetDao;
        }


        @Override
        protected Void doInBackground(dbFirstMeet... firstMeets) {
            firstMeetDao.InsertDetails(firstMeets[0]);
            return null;
        }
    }

    //delete firstmeet async
    private static class DeleteFirstMeetAsync extends AsyncTask<String, Void, Void> {
        FirstMeetDao firstMeetDao;

        DeleteFirstMeetAsync(FirstMeetDao firstMeetDao){
            this.firstMeetDao = firstMeetDao;
        }

        @Override
        protected Void doInBackground(String... strings) {
            firstMeetDao.deleteUser(strings[0]);
            return null;
        }
    }
}
