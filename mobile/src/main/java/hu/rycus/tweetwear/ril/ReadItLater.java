package hu.rycus.tweetwear.ril;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import hu.rycus.tweetwear.common.api.ApiClientHelper;
import hu.rycus.tweetwear.common.api.ApiClientRunnable;
import hu.rycus.tweetwear.common.model.Tweet;
import hu.rycus.tweetwear.common.util.TweetData;
import hu.rycus.tweetwear.database.TweetWearDatabase;

public class ReadItLater {

    private static final String TABLE_NAME = "read_it_later";

    private static final String COLUMN_ID = BaseColumns._ID;
    private static final String COLUMN_LINK = "link";
    private static final String COLUMN_TWEET_JSON = "tweet";
    private static final String COLUMN_READ = "read";
    private static final String COLUMN_TIMESTAMP = "timestamp";

    private static final String CREATE_TABLE_SQL =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_LINK + " TEXT, " +
                    COLUMN_TWEET_JSON + " TEXT, " +
                    COLUMN_READ + " INTEGER, " +
                    COLUMN_TIMESTAMP + " INTEGER)";

    private static final int BOOL_TRUE = 1;
    private static final int BOOL_FALSE = 0;

    public static String getCreateTableSql() {
        return CREATE_TABLE_SQL;
    }

    public static Collection<SavedPage> query(final Context context) {
        return query(context, -1, -1);
    }

    public static Collection<SavedPage> query(
            final Context context, final int limit, final int offset) {
        final SQLiteDatabase db = helper(context).getReadableDatabase();
        try {
            final String[] columns = {
                    COLUMN_ID,
                    COLUMN_LINK,
                    COLUMN_TWEET_JSON,
                    COLUMN_READ,
                    COLUMN_TIMESTAMP };

            final String orderBy = String.format("%s DESC", COLUMN_TIMESTAMP);
            final String limitExpression;
            if (offset > -1 && limit > -1) {
                limitExpression = String.format("%s, %s", offset, limit);
            } else {
                limitExpression = null;
            }

            final Cursor cursor =
                    db.query(TABLE_NAME, columns, null, null, null, null, orderBy, limitExpression);
            try {
                if (cursor.moveToFirst()) {
                    final List<SavedPage> items = new ArrayList<SavedPage>(cursor.getCount());

                    do {
                        final long id = cursor.getLong(0);
                        final String link = cursor.getString(1);
                        final String tweetJson = cursor.getString(2);
                        final boolean read = cursor.getInt(3) == BOOL_TRUE;
                        final long timestamp = cursor.getLong(4);

                        final Tweet tweet = TweetData.parse(tweetJson);
                        items.add(new SavedPage(id, link, tweet, timestamp, read));
                    } while (cursor.moveToNext());

                    return items;
                } else {
                    return Collections.emptyList();
                }
            } finally {
                cursor.close();
            }
        } finally {
            db.close();
        }
    }

    public static int count(final Context context) {
        final SQLiteDatabase db = helper(context).getReadableDatabase();
        try {
            return (int) DatabaseUtils.queryNumEntries(db, TABLE_NAME);
        } finally {
            db.close();
        }
    }

    public static long insert(final Context context, final String link, final Tweet tweet) {
        final SQLiteDatabase db = helper(context).getWritableDatabase();
        try {
            final ContentValues values = new ContentValues(4);
            values.put(COLUMN_LINK, link);
            values.put(COLUMN_TWEET_JSON, TweetData.of(tweet).toJson());
            values.put(COLUMN_READ, BOOL_FALSE);
            values.put(COLUMN_TIMESTAMP, System.currentTimeMillis());
            return db.insert(TABLE_NAME, null, values);
        } finally {
            db.close();
        }
    }

    public static int markAsRead(final Context context, final long id) {
        final SQLiteDatabase db = helper(context).getWritableDatabase();
        try {
            final String[] parameters = { Long.toString(id) };
            final ContentValues values = new ContentValues(1);
            values.put(COLUMN_READ, BOOL_TRUE);
            return db.update(TABLE_NAME, values, COLUMN_ID + " = ?", parameters);
        } finally {
            db.close();
        }
    }

    public static int delete(final Context context, final long id, final Tweet tweet) {
        final SQLiteDatabase db = helper(context).getWritableDatabase();
        try {
            final String[] parameters = {Long.toString(id)};
            final int deleted = db.delete(TABLE_NAME, COLUMN_ID + " = ?", parameters);
            if (deleted > 0) {
                onSavedReadLaterStateDeleted(context, tweet);
            }
            return deleted;
        } finally {
            db.close();
        }
    }

    public static int deleteAll(final Context context) {
        final SQLiteDatabase db = helper(context).getWritableDatabase();
        try {
            final int deleted = db.delete(TABLE_NAME, null, null);
            if (deleted > 0) {
                onAllSavedReadLaterStateDeleted(context);
            }
            return deleted;
        } finally {
            db.close();
        }
    }

    private static void onSavedReadLaterStateDeleted(final Context context, final Tweet tweet) {
        ApiClientHelper.runAsynchronously(context, new ApiClientRunnable() {
            @Override
            protected void run(final Context context,
                               final GoogleApiClient apiClient) throws Exception {
                resetSavedToReadLaterState(tweet, apiClient);
            }
        });
    }

    private static void onAllSavedReadLaterStateDeleted(final Context context) {
        ApiClientHelper.runAsynchronously(context, new ApiClientRunnable() {
            @Override
            protected void run(final Context context, final GoogleApiClient apiClient) throws Exception {
                final Collection<Tweet> tweets = TweetData.loadAll(apiClient);
                for (final Tweet tweet : tweets) {
                    resetSavedToReadLaterState(tweet, apiClient);
                }
            }
        });
    }

    private static void resetSavedToReadLaterState(final Tweet tweet, final GoogleApiClient apiClient) {
        tweet.setSavedToReadLater(false);
        TweetData.of(tweet).sendAsync(apiClient);
    }

    private static TweetWearDatabase helper(final Context context) {
        return new TweetWearDatabase(context);
    }

}
