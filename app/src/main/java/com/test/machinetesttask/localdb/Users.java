package com.test.machinetesttask.localdb;

import androidx.room.ColumnInfo;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Entity;
import androidx.room.Insert;
import androidx.room.PrimaryKey;
import androidx.room.Query;

import java.util.List;

public class Users {
    @Entity
    public static class User {
        @PrimaryKey(autoGenerate = true)
        public int uid;

        @ColumnInfo(name = "id")
        public int id;

        @ColumnInfo(name = "first_name")
        public String firstName;

        @ColumnInfo(name = "last_name")
        public String lastName;

        @ColumnInfo(name = "email")
        public String email;

        @ColumnInfo(name = "avatar")
        public String avatar;
    }

    @Dao
    public interface UserDao {
        @Query("SELECT * FROM user")
        List<User> getAll();

        @Query("SELECT * FROM user WHERE uid IN (:userIds)")
        List<User> loadAllByIds(int[] userIds);

        @Query("SELECT * FROM user WHERE first_name LIKE :first AND " +
                "last_name LIKE :last LIMIT 1")
        User findByName(String first, String last);

        @Insert
        void insertAll(User... users);

        @Delete
        void delete(User user);

        @Insert
        void insert(User user);

        @Query("DELETE FROM user")
        public void nukeTable();
    }
}
