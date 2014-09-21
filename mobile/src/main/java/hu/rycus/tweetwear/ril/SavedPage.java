package hu.rycus.tweetwear.ril;

import android.os.Parcel;
import android.os.Parcelable;

import hu.rycus.tweetwear.common.model.Tweet;
import hu.rycus.tweetwear.common.util.TweetData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor(suppressConstructorProperties = true)
public class SavedPage implements Parcelable {

    private final long id;
    private final Tweet tweet;
    private final long timestamp;
    private final boolean archive;

    @Setter
    private boolean read;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeLong(id);
        dest.writeString(TweetData.of(tweet).toJson());
        dest.writeLong(timestamp);
        dest.writeInt(read ? 1 : 0);
        dest.writeInt(archive ? 1 : 0);
    }

    public static final Creator<SavedPage> CREATOR = new Creator<SavedPage>() {
        @Override
        public SavedPage createFromParcel(final Parcel source) {
            final long id = source.readLong();
            final String tweetJson = source.readString();
            final long timestamp = source.readLong();
            final int read = source.readInt();
            final int archive = source.readInt();

            final Tweet tweet = TweetData.parse(tweetJson);
            return new SavedPage(id, tweet, timestamp, archive > 0, read > 0);
        }

        @Override
        public SavedPage[] newArray(final int size) {
            return new SavedPage[size];
        }
    };

}
