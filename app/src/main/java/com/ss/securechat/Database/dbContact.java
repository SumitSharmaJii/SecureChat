package com.ss.securechat.Database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class dbContact {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "uid")
    private String uid;

    @ColumnInfo(name = "contact_name")
    private String contactName;

    @ColumnInfo(name = "phone_no")
    private String phoneNo;

    @ColumnInfo(name = "token")
    private String token;

    @ColumnInfo(name="profile_pic")
    private String profilePic;


    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}


