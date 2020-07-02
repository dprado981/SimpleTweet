package com.codepath.apps.restclienttemplate;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.codepath.apps.restclienttemplate.models.ComposeDialogFragment;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.TweetDao;
import com.codepath.apps.restclienttemplate.models.TweetWithUser;
import com.codepath.apps.restclienttemplate.models.User;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.Headers;

public class TimelineActivity extends AppCompatActivity implements ComposeDialogFragment.ComposeDialogListener {

    TweetDao tweetDao;
    TwitterClient client;
    RecyclerView rvTweets;
    List<Tweet> tweets;
    TweetsAdapter adapter;
    SwipeRefreshLayout swipeContainer;
    EndlessRecyclerViewScrollListener scrollListener;
    MenuItem miActionProgressItem;
    private final Context context = this;
    private static final String TAG = "TimelineActivity";
    private final int REQUEST_CODE = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        /* Try to get the name replaced with the icon
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_twitter_logo_blue);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
         */

        client = TwitterApp.getRestClient(context);
        tweetDao = ((TwitterApp) getApplicationContext()).getMyDatabase().tweetDao();

        // Find the RecyclerView
        rvTweets = findViewById(R.id.rvTweets);
        // Initialize the list of Tweets and TweetAdapter
        tweets = new ArrayList<>();
        // RecyclerView setup: layout manager and the adapter
        adapter = new TweetsAdapter(context, tweets);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        rvTweets.setLayoutManager(layoutManager);
        rvTweets.setAdapter(adapter);

        // Implement wipe to refresh
        swipeContainer = findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.i(TAG, "fetching new data");
                populateHomeTimeline();
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        rvTweets.addItemDecoration(new DividerItemDecoration(context, LinearLayout.VERTICAL));

        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                Log.i(TAG, "onLoadMore: " + page);
                loadMoreData();
            }
        };
        // Adds the ScrollListener to the RecyclerView
        rvTweets.addOnScrollListener(scrollListener);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "Showing data from database");
                List<TweetWithUser> tweetWithUsers = tweetDao.recentItems();
                Collections.reverse(tweetWithUsers);
                List<Tweet> tweetsFromDB = TweetWithUser.getTweetList(tweetWithUsers);
                Log.d(TAG, "tweetsFromDB.size(): " + tweetsFromDB.size());
                adapter.clear();
                adapter.addAll(tweetsFromDB);
            }
        });

        populateHomeTimeline();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Store instance of the menu item containing progress
        miActionProgressItem = menu.findItem(R.id.miActionProgress);
        // Return to finish
        return super.onPrepareOptionsMenu(menu);
    }

    public void showProgressBar() {
        // Show progress item
        if (miActionProgressItem != null) {
            miActionProgressItem.setVisible(true);

        }
    }

    public void hideProgressBar() {
        // Hide progress item
        if (miActionProgressItem != null) {
            miActionProgressItem.setVisible(false);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu: add it to the action bar if present
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Compose icon has been selected
        if (item.getItemId() == R.id.compose) {
            // Navigate to the compose activity
            /*Intent intent = new Intent(context, ComposeActivity.class);
            startActivityForResult(intent, REQUEST_CODE);
             */
            // Show DialogFragment to create tweet
            FragmentManager fm = ((FragmentActivity) context).getSupportFragmentManager();
            ComposeDialogFragment editNameDialogFragment = ComposeDialogFragment.newInstance(ComposeDialogFragment.NO_REPLY);
            editNameDialogFragment.show(fm, "fragment_compose");
            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            // Get data from intent (Tweet object)
            Tweet tweet = Parcels.unwrap(data.getParcelableExtra("tweet"));
            // Update RecyclerView with new Tweet
            // Modify data source
            tweets.add(0, tweet);
            // Update adapter
            adapter.notifyItemInserted(0);
            rvTweets.smoothScrollToPosition(0);
        }
    }

    private void loadMoreData() {
        showProgressBar();
        // Send an API request to retrieve appropriate paginated data
        client.getNextPageOfTweets(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "onSuccess for loadMoreData: " + json.toString());
                // Deserialize and construct new model objects from the API response
                JSONArray jsonArray = json.jsonArray;
                try {
                    // Append the new data objects to the existing set of items inside the array of items
                    // Notify the adapter of the new items made with `notifyDataSetChanged()
                    adapter.addAll(Tweet.fromJsonArray(jsonArray));
                } catch (JSONException e ) {
                    Log.e(TAG, "JSON exception");
                }
                hideProgressBar();
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure for loadMoreData: " + response, throwable);
                hideProgressBar();
            }
        }, tweets.get(tweets.size()-1).id);

    }

    private void populateHomeTimeline() {
        showProgressBar();
        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "onSuccess: " + json.toString());
                final JSONArray jsonArray = json.jsonArray;
                try {
                    adapter.clear();
                    final List<Tweet> tweetsFromNetwork = Tweet.fromJsonArray(jsonArray);
                    adapter.addAll(tweetsFromNetwork);
                    // Signals refresh has finished
                    swipeContainer.setRefreshing(false);
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "Saving data into database");
                            // Insert Users
                            List<User> usersFromNetwork = User.fromTweetArray(tweetsFromNetwork);
                            tweetDao.insertModel(usersFromNetwork.toArray(new User[0]));
                            // Insert Tweets
                            tweetDao.insertModel(tweetsFromNetwork.toArray(new Tweet[0]));
                        }
                    });
                } catch (JSONException e)  {
                    Log.e(TAG, "JSON exception", e);
                }
                hideProgressBar();
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure: " + response, throwable);
                hideProgressBar();
            }
        });
    }

    // Post Tweet once 'Tweet' button is clicked
    @Override
    public void onFinishComposeDialog(String inputText) {
        client.publishTweet(inputText, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "onSuccess to publishTweet");
                try {
                    Tweet tweet = Tweet.fromJson(json.jsonObject);
                    tweets.add(0,tweet);
                    adapter.notifyItemInserted(0);
                    rvTweets.smoothScrollToPosition(0);
                    Log.i(TAG, "Published tweet: " + tweet.body);
                    Toast.makeText(context, "Nice Tweet!", Toast.LENGTH_LONG).show();
                    populateHomeTimeline();
                } catch (JSONException e) {
                    Log.e(TAG, "JSON exception");
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure to publishTweet", throwable);
            }
        });
    }

}