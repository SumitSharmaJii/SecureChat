package com.ss.securechat.mvvmUtil;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.ss.securechat.Database.dbFirstMeet;

public class FirstmeetViewModel extends AndroidViewModel {
    private FirstmeetRepository firstmeetRepository;

    public FirstmeetViewModel(@NonNull Application application) {
        super(application);
        firstmeetRepository = new FirstmeetRepository(application);
    }

    public void insert(dbFirstMeet firstMeet){
        firstmeetRepository.insert(firstMeet);
    }
    public void delete(String uid){
        firstmeetRepository.delete(uid);
    }
}
