package com.ss.securechat.Database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {dbMessage.class, dbContact.class, dbFirstMeet.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    public abstract MessageDao messageDao();
    public abstract ContactDao contactDao();
    public abstract FirstMeetDao firstMeetDao();
    private static AppDatabase INSTANCE;
    public static synchronized AppDatabase getDBInstance(Context context){

        if(INSTANCE == null){
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),AppDatabase.class, "securechat_database")
                .allowMainThreadQueries()
                .build();

        }
        return INSTANCE;
    }
}
