package com.codepath.apps.restclienttemplate;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import okhttp3.Headers;

public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder> {

    Context context;
    List<Tweet> tweets;
    TwitterClient client;
    public static final String TAG = "TweetsAdapter";

    // Pass in the context and list of tweets
    public TweetsAdapter(Context context, List<Tweet> tweets) {
        this.context = context;
        this.tweets = tweets;
        client = TwitterApp.getRestClient(context);
    }

    // For each row, inflate the layout for a tweet
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tweet, parent, false);
        return new ViewHolder(view);
    }

    // Bind values based on the position of the element
    // RecyclerView tells us the position of the element
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the data
        final Tweet tweet = tweets.get(position);
        // Bind the Tweet with the ViewHolder
        holder.bind(tweet);

        TextView tvRetweetCount = holder.tvRetweetCount;
        final TextView tvFavoriteCount = holder.tvFavoriteCount;
        tvRetweetCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Make Request to retweet post
                System.out.println("hello");

                // update locally
            }
        });
        tvFavoriteCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Make Request to favorite post
                client.toggleFavorite(tweet.favorited, tweet.id, new JsonHttpResponseHandler() {
                    @SuppressLint("DefaultLocale")
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        Log.i(TAG, "onSuccess for toggleFavorite: " + json);
                        tweet.favorited = !tweet.favorited;
                        int updatedImage;
                        int updatedCount = tweet.favoriteCount;
                        if (tweet.favorited) {
                            updatedImage = R.drawable.ic_vector_favorited;
                        } else {
                            updatedImage = R.drawable.ic_vector_favorite;
                            updatedCount--;
                        }
                        tvFavoriteCount.setCompoundDrawablesWithIntrinsicBounds(updatedImage, 0, 0, 0);
                        tvFavoriteCount.setText(String.format("%d", updatedCount));
                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.d(TAG, "onFailure for toggleFavorite: " + response, throwable);
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() { return tweets.size(); }

    // Clean all elements of the recycler
    public void clear() {
        tweets.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Tweet> tweetList) {
        tweets.addAll(tweetList);
        notifyDataSetChanged();
    }

    // Define a ViewHolder
    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivProfileImage;
        TextView tvScreenName;
        TextView tvBody;
        CardView cvAttachedImage;
        ImageView ivAttachedImage;
        TextView tvRetweetCount;
        TextView tvFavoriteCount;

        // itemView is a representation of one row in the RecyclerView (aka one Tweet)
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvScreenName = itemView.findViewById(R.id.tvScreenName);
            tvBody = itemView.findViewById(R.id.tvBody);
            cvAttachedImage = itemView.findViewById(R.id.cvAttachedImage);
            ivAttachedImage = itemView.findViewById(R.id.ivAttachedImage);
            tvRetweetCount = itemView.findViewById(R.id.tvRetweetCount);
            tvFavoriteCount = itemView.findViewById(R.id.tvFavoriteCount);
        }

        @SuppressLint("SetTextI18n")
        public void bind(Tweet tweet) {
            Glide.with(context).load(tweet.user.profileImageUrl).into(ivProfileImage);
            tvScreenName.setText(tweet.user.screenName +  " · " + getRelativeTimeAgo(tweet.createdAt));
            tvBody.setText(tweet.body);
            if (tweet.retweeted) {
                tvRetweetCount.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_vector_retweeted, 0, 0, 0);
            }
            if (tweet.favorited) {
                tvFavoriteCount.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_vector_favorited, 0, 0, 0);
            }
            tvRetweetCount.setText(Integer.toString(tweet.retweetCount));
            tvFavoriteCount.setText(Integer.toString(tweet.favoriteCount));
            addImageFromTweet(tweet);
        }

        private void addImageFromTweet(@NotNull Tweet tweet) {
            if (tweet.imageUrl.isEmpty()) {
                cvAttachedImage.setVisibility(View.GONE);
            } else {
                Glide.with(context).load(tweet.imageUrl).into(ivAttachedImage);
            }
        }
    }

    // getRelativeTimeAgo("Mon Apr 01 21:16:23 +0000 2014");
    public String getRelativeTimeAgo(String rawJsonDate) {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        String relativeDate = "";
        try {
            long dateMillis = sf.parse(rawJsonDate).getTime();
            relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
                    System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
        } catch (ParseException e) {
            Log.e(TAG, "getRelativeTimeAgo: " + e.toString());
        }

        return relativeDate;
    }
}
