package com.test.machinetesttask.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Room;

import com.test.machinetesttask.localdb.UserDatabase;
import com.test.machinetesttask.localdb.Users;
import com.test.machinetesttask.model.ContactsListModel;
import com.test.machinetesttask.network.RestController;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContactsListViewModel extends AndroidViewModel {

    private MutableLiveData<ContactsListModel> liveData = new MutableLiveData<>();
    private Application application;
    private UserDatabase db;

    public ContactsListViewModel(@NonNull Application application) {
        super(application);
        this.application = application;
    }

    public MutableLiveData<ContactsListModel> getConfiguration(int page) {
        initDB();
        getList(page);
        return liveData;
    }

    private void initDB() {
        db = Room.databaseBuilder(application,
                UserDatabase.class, "Users").allowMainThreadQueries().build();

    }

    public void getList(int page) {
        new RestController().groupList(page, new Callback<ContactsListModel>() {
            @Override
            public void onResponse(Call<ContactsListModel> call, Response<ContactsListModel> response) {
                if (response.body() != null) {
                    liveData.setValue(response.body());
                    if (page == 1) {
                        nukeTable();
                    }
                    loadUsersToDB(response.body());
                }
            }

            @Override
            public void onFailure(Call<ContactsListModel> call, Throwable t) {
                liveData.setValue(null);
            }
        });
    }

    private void nukeTable() {
        Users.UserDao userDao = db.userDao();
        userDao.nukeTable();
    }

    public void fetchFromDB() {
        Users.UserDao userDao = db.userDao();
        ContactsListModel contactsListModel = new ContactsListModel();
        contactsListModel.setPage(1);
        contactsListModel.setTotalPages(1);
        List<ContactsListModel.Datum> list = new ArrayList<>();
        for (Users.User user : userDao.getAll())
            list.add(formatToAPI(user));
        contactsListModel.setData(list);
        liveData.setValue(contactsListModel);
    }

    private void loadUsersToDB(ContactsListModel body) {
        Users.UserDao userDao = db.userDao();
        for (ContactsListModel.Datum datum : body.getData())
            userDao.insertAll(formatToDB(datum));
    }

    private Users.User formatToDB(ContactsListModel.Datum body) {
        Users.User user = new Users.User();
        user.firstName = body.getFirstName();
        user.lastName = body.getFirstName();
        user.email = body.getEmail();
        user.id = body.getId();
        user.avatar = body.getAvatar();
        return user;
    }

    private ContactsListModel.Datum formatToAPI(Users.User body) {
        ContactsListModel.Datum user = new ContactsListModel.Datum();
        user.setFirstName(body.firstName);
        user.setLastName(body.lastName);
        user.setEmail(body.email);
        user.setId(body.id);
        user.setAvatar(body.avatar);
        return user;
    }

}
