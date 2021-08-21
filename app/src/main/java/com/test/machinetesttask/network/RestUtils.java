package com.test.machinetesttask.network;

import com.test.machinetesttask.common.Constants;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RestUtils {
    public static RestProviders getRetrofitService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BaseURL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(RestProviders.class);
    }
}
