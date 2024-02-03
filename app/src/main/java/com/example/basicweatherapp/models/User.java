package com.example.basicweatherapp.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(tableName = "users")
public class User {
    @PrimaryKey(autoGenerate = true)
    private int userID;

    @ColumnInfo(name = "name_and_surname")
    private String name_and_surname;


    @Ignore
    public User(){}

    public User(String name_and_surname) {
        this.name_and_surname = name_and_surname;
    }

    public int getUserID() {
        return userID;
    }
    public String getName_and_surname() {
        return name_and_surname;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public void setName_and_surname(String name_and_surname) {
        this.name_and_surname = name_and_surname;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return userID == user.userID &&
                Objects.equals(getName_and_surname(),
                        user.getName_and_surname());
    }

    @Override
    public int hashCode() {
        return Objects.hash(userID, getName_and_surname());
    }

    @NonNull
    @Override
    public String toString() {
        return "User{" +
                "userID=" + userID +
                ", name_and_surname='" + name_and_surname + '\'' +
                '}';
    }
}
