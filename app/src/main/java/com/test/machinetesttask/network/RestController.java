package com.test.machinetesttask.network;

import static com.test.machinetesttask.network.RestUtils.getRetrofitService;

import com.test.machinetesttask.model.ContactsListModel;

import retrofit2.Call;
import retrofit2.Callback;

public class RestController {
    public void groupList(int page, Callback<ContactsListModel> callback) {
        Call<ContactsListModel> call = getRetrofitService().groupList(page);
        call.enqueue(callback);
    }
}
