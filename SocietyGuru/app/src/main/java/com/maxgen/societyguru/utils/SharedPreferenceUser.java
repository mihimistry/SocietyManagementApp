package com.example.societyguru.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.societyguru.model.UserModel;

import static com.example.societyguru.model.UserModel.UserEnum.USER;
import static com.example.societyguru.model.UserModel.UserEnum.email;
import static com.example.societyguru.model.UserModel.UserEnum.fName;
import static com.example.societyguru.model.UserModel.UserEnum.flatHouseNumber;
import static com.example.societyguru.model.UserModel.UserEnum.lName;
import static com.example.societyguru.model.UserModel.UserEnum.mobile;
import static com.example.societyguru.model.UserModel.UserEnum.password;
import static com.example.societyguru.model.UserModel.UserEnum.searchName;
import static com.example.societyguru.model.UserModel.UserEnum.societyId;
import static com.example.societyguru.model.UserModel.UserEnum.status;
import static com.example.societyguru.model.UserModel.UserEnum.token;
import static com.example.societyguru.model.UserModel.UserEnum.userType;

public class SharedPreferenceUser {

    private static SharedPreferenceUser mInstance;

    private SharedPreferenceUser() {
    }

    public static synchronized SharedPreferenceUser getInstance() {
        if (mInstance == null) mInstance = new SharedPreferenceUser();
        return mInstance;
    }

    public void userLogin(UserModel user, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER.name(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(fName.name(), user.getFName());
        editor.putString(lName.name(), user.getLName());
        editor.putString(mobile.name(), user.getMobile());
        editor.putString(email.name(), user.getEmail());
        editor.putString(password.name(), user.getPassword());
        editor.putString(userType.name(), user.getUserType());
        editor.putString(societyId.name(), user.getSocietyId());
        editor.putString(searchName.name(), user.getSearchName());
        editor.putString(status.name(), user.getStatus());
        editor.putString(token.name(), user.getToken());
        editor.putString(flatHouseNumber.name(), user.getFlatHouseNumber());
        editor.apply();
    }

    public UserModel getUser(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER.name(), Context.MODE_PRIVATE);
        return new UserModel(
                sharedPreferences.getString(fName.name(), ""),
                sharedPreferences.getString(lName.name(), ""),
                sharedPreferences.getString(email.name(), ""),
                sharedPreferences.getString(mobile.name(), ""),
                sharedPreferences.getString(password.name(), ""),
                sharedPreferences.getString(userType.name(), ""),
                sharedPreferences.getString(societyId.name(), ""),
                sharedPreferences.getString(searchName.name(), ""),
                sharedPreferences.getString(status.name(), ""),
                sharedPreferences.getString(token.name(), ""),
                sharedPreferences.getString(flatHouseNumber.name(), "")
        );
    }

    public boolean isLoggedIn(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER.name(), Context.MODE_PRIVATE);
        return sharedPreferences.getString(email.name(), null) != null;
    }

    public void logout(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER.name(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    public void userUpdate(UserModel user, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER.name(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(fName.name(), user.getFName());
        editor.putString(lName.name(), user.getLName());
        editor.putString(mobile.name(), user.getMobile());
        editor.putString(password.name(), user.getPassword());
        editor.putString(flatHouseNumber.name(), user.getFlatHouseNumber());
        editor.apply();
    }

}
