package com.ss.securechat.Database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class dbFirstMeet {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "user_uid")
    private String userUid;

    @ColumnInfo(name = "aes_key")
    private String aesKey;

    public String getUserUid() {
        return userUid;
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }

    public String getAesKey() {
        return aesKey;
    }

    public void setAesKey(String aesKey) {
        this.aesKey = aesKey;
    }
}

