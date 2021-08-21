package com.test.machinetesttask.localdb;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Users.User.class}, version = 1)
public abstract class UserDatabase extends RoomDatabase {
    public abstract Users.UserDao userDao();
}