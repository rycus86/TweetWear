package hu.rycus.tweetwear.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import hu.rycus.tweetwear.ril.ReadItLater;

public class TweetWearDatabase extends SQLiteOpenHelper {

    private static final String DB_NAME = "tweetwear.db";
    private static final int DB_VERSION = 2;

    public TweetWearDatabase(final Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        ReadItLater.onCreateDatabase(db);
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        ReadItLater.onUpgradeDatabase(db, oldVersion);
    }

    public static String getName() {
        return DB_NAME;
    }

}
