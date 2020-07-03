package com.codepath.apps.restclienttemplate;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.databinding.ItemTweetBinding;
import com.codepath.apps.restclienttemplate.models.ComposeDialogFragment;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.User;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.jetbrains.annotations.NotNull;
import org.parceler.Parcels;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import okhttp3.Headers;

public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder> {

    Context context;
    List<Tweet> tweets;
    TwitterClient client;
    ItemTweetBinding binding;
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
        binding = ItemTweetBinding.inflate((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
                , parent, false);
        View view = binding.getRoot();
        return new ViewHolder(view);
    }

    // Bind values based on the position of the element
    // RecyclerView tells us the position of the element
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        // Get the data
        final Tweet bindingTweet = tweets.get(position);
        // Bind the Tweet with the ViewHolder
        holder.bind(bindingTweet);

        final TextView tvRetweetCount = binding.tvRetweetCount;
        final TextView tvFavoriteCount = binding.tvFavoriteCount;
        final TextView tvReplyCount = binding.tvReplyCount;
        final ImageView ivProfileImage  = binding.ivProfileImage;
        final TextView tvScreenName  = binding.tvScreenName;
        final LinearLayout llContent = binding.llContent;
        final TextView tvBody = binding.tvBody;
        final ImageView ivAttachedImage = binding.ivAttachedImage;

        getClickables(holder.tvBody, bindingTweet);

        tvReplyCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Show DialogFragment to create tweet
                int position = holder.getAdapterPosition();
                Tweet tweet = tweets.get(position);
                FragmentManager fm = ((FragmentActivity) context).getSupportFragmentManager();
                ComposeDialogFragment editNameDialogFragment = ComposeDialogFragment.newInstance(tweet);
                editNameDialogFragment.show(fm, "fragment_compose");
            }
        });

        tvRetweetCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Make Request to retweet post
                int position = holder.getAdapterPosition();
                final Tweet tweet = tweets.get(position);
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
                int position = holder.getAdapterPosition();
                final Tweet tweet = tweets.get(position);
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
                int position = holder.getAdapterPosition();
                Tweet tweet = tweets.get(position);
                Log.d(TAG, "going to profile page");
                Intent intent = new Intent(context, ProfileActivity.class);
                intent.putExtra(User.class.getSimpleName(), Parcels.wrap(tweet.user));
                context.startActivity(intent);
            }
        });

        tvScreenName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                Tweet tweet = tweets.get(position);
                Log.d(TAG, "going to profile page");
                Intent intent = new Intent(context, ProfileActivity.class);
                intent.putExtra(User.class.getSimpleName(), Parcels.wrap(tweet.user));
                context.startActivity(intent);
            }
        });

        llContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                Tweet tweet = tweets.get(position);
                Log.d(TAG, "going to details page of tweet at position " + position);
                Intent intent = new Intent(context, TweetDetailActivity.class);
                intent.putExtra(Tweet.class.getSimpleName(), Parcels.wrap(tweet));
                context.startActivity(intent);
            }
        });

        tvBody.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                Tweet tweet = tweets.get(position);
                Log.d(TAG, "going to details page of tweet at position " + position);
                Intent intent = new Intent(context, TweetDetailActivity.class);
                intent.putExtra(Tweet.class.getSimpleName(), Parcels.wrap(tweet));
                context.startActivity(intent);
            }
        });
        ivAttachedImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                Tweet tweet = tweets.get(position);
                Log.d(TAG, "going to details page of tweet at position " + position);
                Intent intent = new Intent(context, TweetDetailActivity.class);
                intent.putExtra(Tweet.class.getSimpleName(), Parcels.wrap(tweet));
                context.startActivity(intent);
            }
        });

    }

    private void getClickables(TextView tvBody, final Tweet tweet) {

        SpannableString ss = new SpannableString(tvBody.getText());
        ClickableSpan clickableSpanAt = new ClickableSpan() {
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
        ClickableSpan clickableSpanTag = new ClickableSpan() {
            @Override
            public void onClick(@NotNull View textView) {
                Log.i(TAG, "clicking #");
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
                    ss.setSpan(clickableSpanAt, indexOfAt, i, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    ss.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.link_blue)),
                            indexOfAt, i, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    indexOfAt = -1;

                } else if (indexOfTag >= 0) {
                    ss.setSpan(clickableSpanTag, indexOfTag, i, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
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
        TextView tvReplyCount;
        TextView tvRetweetCount;
        TextView tvFavoriteCount;
        LinearLayout llContent;

        // itemView is a representation of one row in the RecyclerView (aka one Tweet)
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfileImage = binding.ivProfileImage;
            tvScreenName = binding.tvScreenName;
            tvBody = binding.tvBody;
            cvAttachedImage = binding.cvAttachedImage;
            ivAttachedImage = binding.ivAttachedImage;
            tvReplyCount = binding.tvReplyCount;
            tvRetweetCount = binding.tvRetweetCount;
            tvFavoriteCount = binding.tvFavoriteCount;
            llContent = binding.llContent;
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

        private void addImageFromTweet(@NotNull Tweet tweet) {
            if (tweet.imageUrl.isEmpty()) {
                cvAttachedImage.setVisibility(View.GONE);
                cvAttachedImage.clearAnimation();
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
