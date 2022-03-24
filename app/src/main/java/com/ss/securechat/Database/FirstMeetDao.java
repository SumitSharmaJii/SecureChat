package com.ss.securechat.Database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface FirstMeetDao {

    @Insert(onConflict = 1)
    void InsertDetails(dbFirstMeet...fM);

    @Query("SELECT aes_key FROM dbFirstMeet WHERE user_uid = :param")
    String getAesKey(String param);

    @Query("DELETE FROM dbFirstMeet WHERE user_uid = :param")
    void deleteUser(String param);
}
