package com.codepath.apps.restclienttemplate.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;

@Parcel
@Entity
public class User {

    @ColumnInfo
    @PrimaryKey
    public long id;

    @ColumnInfo
    public String name;

    @ColumnInfo
    public String screenName;

    @ColumnInfo
    public String profileImageUrl;

    @ColumnInfo
    public String profileBannerUrl;

    // Empty constructor needed for Parceler library
    public User(){}

    public static User fromJson(JSONObject jsonObject) throws JSONException {
        User user = new User();
        user.id = jsonObject.getLong("id");
        user.name = jsonObject.getString("name");
        user.screenName = jsonObject.getString("screen_name");
        user.profileImageUrl = jsonObject.getString("profile_image_url_https");
        user.profileImageUrl = user.profileImageUrl.replace("_normal", "");
        try {
            user.profileBannerUrl = jsonObject.getString("profile_banner_url");
        } catch (JSONException e) {
            user.profileBannerUrl = "";
        }
        return user;
    }

    public static List<User> fromTweetArray(List<Tweet> tweets) {
        List<User> users = new ArrayList<>();
        for (Tweet tweet : tweets) {
            User user = new User();
            users.add(tweet.user);
        }
        return users;
    }

    public static List<User> fromJsonArray(JSONArray jsonArray) throws JSONException {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            users.add(fromJson(jsonArray.getJSONObject(i)));
        }
        return users;
    }

}
