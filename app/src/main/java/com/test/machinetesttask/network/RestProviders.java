package com.test.machinetesttask.network;

import com.test.machinetesttask.model.ContactsListModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RestProviders {
    @GET("users")
    Call<ContactsListModel> groupList(@Query("page") int page);
}
