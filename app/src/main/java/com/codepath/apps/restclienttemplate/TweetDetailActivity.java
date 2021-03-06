package com.codepath.apps.restclienttemplate;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.databinding.ActivityDetailBinding;
import com.codepath.apps.restclienttemplate.models.ComposeDialogFragment;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.User;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.jetbrains.annotations.NotNull;
import org.parceler.Parcels;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import okhttp3.Headers;

public class TweetDetailActivity extends AppCompatActivity {

    Tweet tweet;
    Context context;
    TwitterClient client;
    
    ImageView ivProfileImage;
    TextView tvScreenName;
    TextView tvBody;
    CardView cvAttachedImage;
    ImageView ivAttachedImage;
    TextView tvReplyCount;
    TextView tvRetweetCount;
    TextView tvFavoriteCount;

    public static final String TAG = "TweetDetailActivity";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityDetailBinding binding = ActivityDetailBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        tweet = Parcels.unwrap(getIntent().getParcelableExtra(Tweet.class.getSimpleName()));
        context = this;
        client = TwitterApp.getRestClient(context);

        ivProfileImage = binding.ivProfileImage;
        tvScreenName = binding.tvScreenName;
        tvBody = binding.tvBody;
        cvAttachedImage = binding.cvAttachedImage;
        ivAttachedImage = binding.ivAttachedImage;
        tvReplyCount = binding.tvReplyCount;
        tvRetweetCount = binding.tvRetweetCount;
        tvFavoriteCount = binding.tvFavoriteCount;

        bind(tweet);

        getClickables(tvBody);

        tvReplyCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Show DialogFragment to create tweet
                FragmentManager fm = ((FragmentActivity) context).getSupportFragmentManager();
                ComposeDialogFragment editNameDialogFragment = ComposeDialogFragment.newInstance(tweet);
                editNameDialogFragment.show(fm, "fragment_compose");
            }
        });

        tvRetweetCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Make Request to retweet post
                client.toggleRetweet(tweet.retweeted, tweet.id, new JsonHttpResponseHandler() {
                    @SuppressLint("DefaultLocale")
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        Log.i(TAG, "onSuccess for toggleRetweet: " + json);
                        tweet.retweeted = !tweet.retweeted;
                        int updatedImage;
                        int updatedCount = tweet.retweetCount;
                        if (tweet.retweeted) {
                            updatedImage = R.drawable.ic_vector_retweeted;
                            updatedCount++;
                        } else {
                            updatedImage = R.drawable.ic_vector_retweet;
                            updatedCount--;
                        }
                        tvRetweetCount.setCompoundDrawablesWithIntrinsicBounds(updatedImage, 0, 0, 0);
                        tvRetweetCount.setText(String.format("%d", updatedCount));
                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.d(TAG, "onFailure for toggleRetweet: " + response, throwable);
                    }
                });
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
                        Log.i(TAG, "onSuccess for toggleFavorite: " + json.toString());
                        tweet.favorited = !tweet.favorited;
                        int updatedImage;
                        int updatedCount = tweet.favoriteCount;
                        if (tweet.favorited) {
                            updatedImage = R.drawable.ic_vector_favorited;
                            updatedCount++;
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

        ivProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ProfileActivity.class);
                intent.putExtra(User.class.getSimpleName(), Parcels.wrap(tweet.user));
                context.startActivity(intent);
            }
        });

        tvScreenName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ProfileActivity.class);
                intent.putExtra(User.class.getSimpleName(), Parcels.wrap(tweet.user));
                context.startActivity(intent);
            }
        });

    }

    @SuppressLint("SetTextI18n")
    public void bind(Tweet tweet) {
        Glide.with(context).load(tweet.user.profileImageUrl).into(ivProfileImage);
        tvScreenName.setText("@"+tweet.user.screenName +  " · " + getRelativeTimeAgo(tweet.createdAt));
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

    private void getClickables(TextView tvBody) {

        SpannableString ss = new SpannableString(tvBody.getText());
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NotNull View textView) {
                Log.i(TAG, "clicking @");
            }
            @Override
            public void updateDrawState(@NotNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        };

        String body = tvBody.getText().toString() + " ";

        int indexOfAt = -1;
        int indexOfTag = -1;
        for (int i = 0; i < body.length(); i++) {
            if (body.charAt(i) == ' ') {
                if (indexOfAt >= 0) {
                    ss.setSpan(clickableSpan, indexOfAt, i, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    ss.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.link_blue)),
                            indexOfAt, i, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    indexOfAt = -1;
                }
                if (indexOfTag >= 0) {
                    ss.setSpan(clickableSpan, indexOfTag, i, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    ss.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.link_blue)),
                            indexOfTag, i, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    indexOfTag = -1;
                }
            } else if (body.charAt(i) == '@') {
                indexOfAt = i;
            } else if (body.charAt(i) == '#') {
                indexOfTag = i;
            }
        }

        tvBody.setText(ss);
        tvBody.setMovementMethod(LinkMovementMethod.getInstance());
        tvBody.setHighlightColor(Color.TRANSPARENT);
    }

    private void addImageFromTweet(@NotNull Tweet tweet) {
        if (tweet.imageUrl.isEmpty()) {
            cvAttachedImage.setVisibility(View.GONE);
            cvAttachedImage.clearAnimation();
        } else {
            Glide.with(context).load(tweet.imageUrl)
                    .placeholder(R.drawable.ic_twitter_logo_blue).into(ivAttachedImage);
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