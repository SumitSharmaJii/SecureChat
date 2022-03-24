package com.ss.securechat.Database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface MessageDao {

    @Query("SELECT * FROM dbMessage WHERE chat_room = :room")
    LiveData<List<dbMessage>> getAllMsgs(String room);

    @Insert
    void insertMsg(dbMessage... msg);

    @Delete
    void delMsg(dbMessage...msg);

    @Query("DELETE FROM dbMessage WHERE chat_room = :param")
    void deleteMsgsOfDeletedUser(String  param);

}
