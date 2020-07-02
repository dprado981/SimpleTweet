package com.codepath.apps.restclienttemplate.models;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TweetDao {

    @Query("SELECT Tweet.imageUrl as tweet_imageUrl, Tweet.body AS tweet_body, Tweet.createdAt AS tweet_createdAt, " +
            "Tweet.id AS tweet_id, Tweet.retweeted as tweet_retweeted, Tweet.favorited as tweet_favorited, " +
            "Tweet.retweetCount as tweet_retweetCount, Tweet.favoriteCount as tweet_favoriteCount, User.*" +
            "FROM Tweet INNER JOIN User ON Tweet.userID = User.id ORDER BY Tweet.createdAt ASC LIMIT 25")
    List<TweetWithUser> recentItems();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertModel(Tweet... tweets);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertModel(User... users);
}

