package com.ss.securechat.Database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;


import java.util.List;

@Dao
public interface ContactDao {

    @Query("SELECT * FROM dbContact ")
    LiveData<List<dbContact>> getAllContacts();

    @Query("delete from dbContact WHERE uid = :param")
    void deleteContact(String param);

    @Query("UPDATE dbContact SET profile_pic = :url WHERE uid =:uid")
    void updatePic(String url,String uid);

    @Query("UPDATE dbContact SET contact_name = :name WHERE uid =:uid")
    void updateName(String name,String uid);

    @Insert(onConflict = 1)
    void insertContact(dbContact... contact);




}
