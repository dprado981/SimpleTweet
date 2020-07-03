package com.codepath.apps.restclienttemplate;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.databinding.ActivityProfileBinding;
import com.codepath.apps.restclienttemplate.models.User;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class ProfileActivity extends AppCompatActivity {

    User user;
    List<User> following;
    List<User> followers;

    TwitterClient client;
    Context context;
    UsersAdapter followingAdapter;
    UsersAdapter followerAdapter;

    ImageView ivBanner;
    ImageView ivProfileImage;
    TextView tvProfileName;
    TextView tvScreenName;
    RecyclerView rvFollowers;
    RecyclerView rvFollowing;

    public static final String TAG = "ProfileActivity";

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityProfileBinding binding = ActivityProfileBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        user = Parcels.unwrap(getIntent().getParcelableExtra(User.class.getSimpleName()));
        following = new ArrayList<>();
        followers = new ArrayList<>();

        context = this;
        client = TwitterApp.getRestClient(context);
        followerAdapter = new UsersAdapter(context, followers);
        followingAdapter = new UsersAdapter(context, following);

        ivBanner = binding.ivBanner;
        ivProfileImage = binding.ivProfileImage;
        tvProfileName = binding.tvProfileName;
        tvScreenName = binding.tvScreenName;
        rvFollowers = binding.rvFollowers;
        rvFollowing = binding.rvFollowing;

        LinearLayoutManager layoutManagerFollower = new LinearLayoutManager(context);
        rvFollowers.setLayoutManager(layoutManagerFollower);
        rvFollowers.setAdapter(followerAdapter);
        LinearLayoutManager layoutManagerFollowing = new LinearLayoutManager(context);
        rvFollowing.setLayoutManager(layoutManagerFollowing);
        rvFollowing.setAdapter(followingAdapter);

        if (!user.profileBannerUrl.isEmpty()) {
            Glide.with(context).load(user.profileBannerUrl).placeholder(R.drawable.ic_twitter_logo_blue).into(ivBanner);
        }

        Glide.with(context).load(user.profileImageUrl).placeholder(R.drawable.ic_twitter_logo_blue).into(ivProfileImage);
        tvProfileName.setText(user.name);
        tvScreenName.setText("@"+user.screenName);

        client.getFollowers(user.id, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "onSuccess from getFollowers: " + json);
                JSONObject jsonObject = json.jsonObject;
                try {
                    JSONArray jsonArray = jsonObject.getJSONArray("users");
                    followerAdapter.clear();
                    final List<User> usersFromNetwork = User.fromJsonArray(jsonArray);
                    followerAdapter.addAll(usersFromNetwork);
                } catch (JSONException e) {
                    Log.i(TAG, "JSON exception", e);
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.i(TAG, "onFailure from getFollowers: " + response, throwable);
            }
        });

        client.getFriends(user.id, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "onSuccess from getFriends: " + json);
                JSONObject jsonObject = json.jsonObject;
                try {
                    JSONArray jsonArray = jsonObject.getJSONArray("users");
                    followingAdapter.clear();
                    final List<User> usersFromNetwork = User.fromJsonArray(jsonArray);
                    followingAdapter.addAll(usersFromNetwork);
                } catch (JSONException e) {
                    Log.i(TAG, "JSON exception", e);
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.i(TAG, "onFailure from getFriends: " + response, throwable);
            }
        });

    }
}