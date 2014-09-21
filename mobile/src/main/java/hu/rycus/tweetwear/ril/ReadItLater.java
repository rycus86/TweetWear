package hu.rycus.tweetwear.ril;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

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

    private static final String TAG = ReadItLater.class.getSimpleName();

    private static final String TABLE_NAME_MAIN = "read_it_later";
    private static final String TABLE_NAME_ARCHIVE = "read_it_later_archive";

    private static final String COLUMN_ID = BaseColumns._ID;
    private static final String COLUMN_TWEET_JSON = "tweet";
    private static final String COLUMN_READ = "read";
    private static final String COLUMN_TIMESTAMP = "timestamp";

    private static final String CREATE_TABLE_SQL_MAIN =
            "CREATE TABLE " + TABLE_NAME_MAIN + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TWEET_JSON + " TEXT, " +
                    COLUMN_READ + " INTEGER, " +
                    COLUMN_TIMESTAMP + " INTEGER)";

    private static final String CREATE_TABLE_SQL_ARCHIVE =
            "CREATE TABLE " + TABLE_NAME_ARCHIVE + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TWEET_JSON + " TEXT, " +
                    COLUMN_TIMESTAMP + " INTEGER)";

    private static final int BOOL_TRUE = 1;
    private static final int BOOL_FALSE = 0;

    public static void onCreateDatabase(final SQLiteDatabase database) {
        database.execSQL(CREATE_TABLE_SQL_MAIN);
        database.execSQL(CREATE_TABLE_SQL_ARCHIVE);
    }

    public static void onUpgradeDatabase(final SQLiteDatabase database, final int oldVersion) {
        if (oldVersion < 2) {
            boolean successfullyUpgraded = false;

            // drop link column
            database.beginTransaction();
            try {
                database.execSQL("CREATE TEMPORARY TABLE ril_tmp (tj, r, ts)");
                database.execSQL("INSERT INTO ril_tmp " +
                        "SELECT tweet, read, timestamp FROM " + TABLE_NAME_MAIN);
                database.execSQL("DROP TABLE " + TABLE_NAME_MAIN);
                database.execSQL(CREATE_TABLE_SQL_MAIN);
                database.execSQL("INSERT INTO " + TABLE_NAME_MAIN + " (tweet, read, timestamp) " +
                        "SELECT tj, r, ts FROM ril_tmp");
                database.execSQL("DROP TABLE ril_tmp");

                database.setTransactionSuccessful();

                successfullyUpgraded = true;
            } catch (Exception ex) {
                Log.e(TAG, "Failed to upgrade the database", ex);
            } finally {
                database.endTransaction();
            }

            if (!successfullyUpgraded) {
                Log.w(TAG, "Recreating ReadItLater table");
                database.execSQL("DROP TABLE " + TABLE_NAME_MAIN);
                database.execSQL(CREATE_TABLE_SQL_MAIN);
            }

            // create archive table
            database.execSQL(CREATE_TABLE_SQL_ARCHIVE);
        }
    }

    public static Collection<SavedPage> query(final Context context) {
        return query(context, -1, -1, false);
    }

    public static Collection<SavedPage> queryArchives(final Context context) {
        return query(context, -1, -1, true);
    }

    private static Collection<SavedPage> query(
        final Context context, final int limit, final int offset, boolean archive) {
        final SQLiteDatabase db = helper(context).getReadableDatabase();
        try {
            final String tableName = archive ? TABLE_NAME_ARCHIVE : TABLE_NAME_MAIN;
            final String[] columns = {
                    COLUMN_ID,
                    COLUMN_TWEET_JSON,
                    archive ? Integer.toString(BOOL_TRUE) : COLUMN_READ,
                    COLUMN_TIMESTAMP };

            final String orderBy = String.format("%s DESC", COLUMN_TIMESTAMP);
            final String limitExpression;
            if (offset > -1 && limit > -1) {
                limitExpression = String.format("%s, %s", offset, limit);
            } else {
                limitExpression = null;
            }

            final Cursor cursor =
                    db.query(tableName, columns, null, null, null, null, orderBy, limitExpression);
            try {
                if (cursor.moveToFirst()) {
                    final List<SavedPage> items = new ArrayList<SavedPage>(cursor.getCount());

                    do {
                        final long id = cursor.getLong(0);
                        final String tweetJson = cursor.getString(1);
                        final boolean read = cursor.getInt(2) == BOOL_TRUE;
                        final long timestamp = cursor.getLong(3);

                        final Tweet tweet = TweetData.parse(tweetJson);
                        items.add(new SavedPage(id, tweet, timestamp, archive, read));
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
        return count(context, TABLE_NAME_MAIN);
    }

    public static int countArchives(final Context context) {
        return count(context, TABLE_NAME_ARCHIVE);
    }

    private static int count(final Context context, final String tableName) {
        final SQLiteDatabase db = helper(context).getReadableDatabase();
        try {
            return (int) DatabaseUtils.queryNumEntries(db, tableName);
        } finally {
            db.close();
        }
    }

    public static long insert(final Context context, final Tweet tweet) {
        final SQLiteDatabase db = helper(context).getWritableDatabase();
        try {
            final ContentValues values = new ContentValues(3);
            values.put(COLUMN_TWEET_JSON, TweetData.of(tweet).toJson());
            values.put(COLUMN_READ, BOOL_FALSE);
            values.put(COLUMN_TIMESTAMP, System.currentTimeMillis());
            return db.insert(TABLE_NAME_MAIN, null, values);
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
            return db.update(TABLE_NAME_MAIN, values, COLUMN_ID + " = ?", parameters);
        } finally {
            db.close();
        }
    }

    public static long archive(final Context context, final SavedPage page) {
        final SQLiteDatabase db = helper(context).getWritableDatabase();
        try {
            final ContentValues values = new ContentValues(2);
            values.put(COLUMN_TWEET_JSON, TweetData.of(page.getTweet()).toJson());
            values.put(COLUMN_TIMESTAMP, page.getTimestamp());

            final long insertResult = db.insert(TABLE_NAME_ARCHIVE, null, values);
            if (insertResult > 0) {
                final long deleteResult = delete(context, page);
                return Math.min(insertResult, deleteResult);
            }
        } finally {
            db.close();
        }

        return -1;
    }

    public static int delete(final Context context, final SavedPage page) {
        final SQLiteDatabase db = helper(context).getWritableDatabase();
        try {
            final String tableName =
                    page.isArchive() ? TABLE_NAME_ARCHIVE : TABLE_NAME_MAIN;
            final String[] parameters = {Long.toString(page.getId())};
            final int deleted = db.delete(tableName, COLUMN_ID + " = ?", parameters);
            if (!page.isArchive() && deleted > 0) {
                onSavedReadLaterStateDeleted(context, page.getTweet());
            }
            return deleted;
        } finally {
            db.close();
        }
    }

    public static int deleteAll(final Context context) {
        return deleteAll(context, TABLE_NAME_MAIN);
    }

    public static int deleteAllArchives(final Context context) {
        return deleteAll(context, TABLE_NAME_ARCHIVE);
    }

    private static int deleteAll(final Context context, final String tableName) {
        final SQLiteDatabase db = helper(context).getWritableDatabase();
        try {
            final int deleted = db.delete(tableName, null, null);
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
