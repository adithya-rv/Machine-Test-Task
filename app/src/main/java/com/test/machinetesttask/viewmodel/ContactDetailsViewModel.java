package com.test.machinetesttask.viewmodel;

import android.app.Application;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.test.machinetesttask.common.Constants;
import com.test.machinetesttask.model.ContactsListModel;

public class ContactDetailsViewModel extends AndroidViewModel {

    private MutableLiveData<ContactsListModel.Datum> liveData = new MutableLiveData<>();

    public ContactDetailsViewModel(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<ContactsListModel.Datum> getConfiguration(Intent intent) {
        getIntentData(intent);
        return liveData;
    }

    private void getIntentData(Intent intent) {
        if (intent.hasExtra(Constants.CONTACT_DETAILS)) {
            Gson gson = new Gson();
            liveData.setValue(gson.fromJson(intent.getStringExtra(Constants.CONTACT_DETAILS), ContactsListModel.Datum.class));
        }
    }
}
